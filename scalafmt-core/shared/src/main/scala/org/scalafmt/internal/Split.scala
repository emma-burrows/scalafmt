package org.scalafmt.internal

import scala.meta.tokens.Token

import org.scalafmt.internal.Policy.NoPolicy
import org.scalafmt.util.TokenOps

case class OptimalToken(token: Token, killOnFail: Boolean = false) {
  override def toString: String = s"$token:${token.end}"
}

/**
  * A Split is the whitespace between two non-whitespace tokens.
  *
  * Consider a split to be an edge in a search graph and [[FormatToken]]
  * are the nodes.
  *
  * NB: there's a historical inconsistency in how splits are sorted; when
  * they are initially considered, cost is the primary factor (and hence,
  * because of stable sort, earlier split with the same cost will take
  * precedence). However, when a search state is added into the priority
  * queue, preference is given to states with lower cost, further token
  * and, unlike above, a LATER line defining the split.
  *
  * A possible reason for the latter is to give those "secondary" splits
  * a chance to move through the BestFirstSearch algorithm, as otherwise
  * a sequence of primary splits might end up as the winning solution
  * even if it exceeds the maxColumn margins, because a secondary split
  * was deemed unlikely to win and moved to a backup priority queue.
  *
  * @param modExt whitespace and indents
  * @param cost How good is this output? Lower is better.
  * @param policy How does this split affect other later splits?
  * @param line For debugging, to retrace from which case in [[Router]]
  *             this split originates.
  *
  */
case class Split(
    modExt: ModExt,
    cost: Int,
    tag: SplitTag = SplitTag.Active,
    activeTag: SplitTag = SplitTag.Active,
    policy: Policy = NoPolicy,
    optimalAt: Option[OptimalToken] = None
)(implicit val line: sourcecode.Line) {
  import TokenOps._

  def adapt(formatToken: FormatToken): Split =
    modExt.mod match {
      case n: NewlineT if !n.noIndent && rhsIsCommentedOut(formatToken) =>
        copy(modExt = modExt.copy(mod = NewlineT(n.isDouble, noIndent = true)))
      case _ => this
    }

  @inline
  def indentation: String = modExt.indentation

  @inline
  def isNL: Boolean = modExt.mod.isNewline

  @inline
  def length: Int = modExt.mod.length

  @inline
  def isIgnored: Boolean = tag eq SplitTag.Ignored

  @inline
  def isActive: Boolean = tag eq activeTag

  @inline
  def notIf(flag: Boolean): Split = onlyIf(!flag)

  def onlyIf(flag: Boolean): Split =
    if (flag || isIgnored) this else copy(tag = SplitTag.Ignored)

  def onlyFor(tag: SplitTag, ignore: Boolean = false): Split =
    if (isIgnored || ignore || (this.tag eq tag)) this
    else if (isActive) copy(tag = tag)
    else throw new UnsupportedOperationException("Multiple tags unsupported")

  def activateFor(tag: SplitTag): Split =
    if (isIgnored || (this.activeTag eq tag)) this else copy(activeTag = tag)

  def withOptimalTokenOpt(
      token: => Option[Token],
      killOnFail: Boolean = false
  ): Split =
    withOptimalAt(token.map(OptimalToken(_, killOnFail)))

  def withOptimalToken(token: => Token, killOnFail: Boolean = false): Split =
    withOptimalAt(Some(OptimalToken(token, killOnFail)))

  def withOptimalAt(optimalAt: => Option[OptimalToken]): Split = {
    require(this.optimalAt.isEmpty)
    if (isIgnored) this else copy(optimalAt = optimalAt)
  }

  def withPolicy(
      newPolicy: => Policy,
      ignore: Boolean = false
  )(implicit line: sourcecode.Line): Split = {
    if (policy != NoPolicy)
      throw new UnsupportedOperationException("Can't have two policies yet.")
    if (isIgnored || ignore) this else copy(policy = newPolicy)
  }

  def withSingleLine(
      expire: Token,
      exclude: => Set[Range] = Set.empty,
      noSyntaxNL: Boolean = false,
      killOnFail: Boolean = false
  )(implicit line: sourcecode.Line): Split =
    withSingleLineAndOptimal(expire, expire, exclude, noSyntaxNL, killOnFail)

  def withSingleLineOpt(
      expire: Option[Token],
      exclude: => Set[Range] = Set.empty,
      noSyntaxNL: Boolean = false,
      killOnFail: Boolean = false
  )(implicit line: sourcecode.Line): Split =
    expire.fold(this)(withSingleLine(_, exclude, noSyntaxNL, killOnFail))

  def withSingleLineAndOptimal(
      expire: Token,
      optimal: Token,
      exclude: => Set[Range] = Set.empty,
      noSyntaxNL: Boolean = false,
      killOnFail: Boolean = false
  )(implicit line: sourcecode.Line): Split =
    withOptimalToken(optimal, killOnFail)
      .withSingleLineNoOptimal(expire, exclude, noSyntaxNL)

  def withSingleLineNoOptimal(
      expire: Token,
      exclude: => Set[Range] = Set.empty,
      noSyntaxNL: Boolean = false
  )(implicit line: sourcecode.Line): Split =
    withPolicy(
      SingleLineBlock(
        expire,
        exclude,
        penaliseNewlinesInsideTokens = noSyntaxNL
      )
    )

  def withPolicyOpt(
      newPolicy: => Option[Policy]
  )(implicit line: sourcecode.Line): Split =
    if (isIgnored) this else newPolicy.fold(this)(withPolicy(_))

  def orElsePolicy(newPolicy: Policy): Split =
    if (isIgnored || newPolicy.isEmpty) this
    else if (policy.isEmpty) copy(policy = newPolicy)
    else copy(policy = policy.orElse(newPolicy))

  def andThenPolicy(newPolicy: Policy): Split =
    if (isIgnored || newPolicy.isEmpty) this
    else if (policy.isEmpty) copy(policy = newPolicy)
    else copy(policy = policy.andThen(newPolicy))

  def andThenPolicy(newPolicy: => Policy, ignore: Boolean): Split =
    if (ignore) this else andThenPolicy(newPolicy)

  def andThenPolicyOpt(newPolicy: => Option[Policy]): Split =
    if (isIgnored) this
    else newPolicy.fold(this)(andThenPolicy)

  def withPenalty(penalty: Int): Split =
    if (isIgnored || penalty <= 0) this else copy(cost = cost + penalty)

  def withIndent(length: => Length, expire: => Token, when: ExpiresOn): Split =
    withMod(modExt.withIndent(length, expire, when))

  def withIndentOpt(
      length: => Length,
      expire: Option[Token],
      when: ExpiresOn
  ): Split =
    withMod(modExt.withIndentOpt(length, expire, when))

  def withIndent(indent: => Indent): Split =
    withMod(modExt.withIndent(indent))

  def withIndentOpt(indent: => Option[Indent]): Split =
    withMod(modExt.withIndentOpt(indent))

  def withIndents(indents: Seq[Indent]): Split =
    withMod(modExt.withIndents(indents))

  def switch(switchObject: AnyRef): Split =
    withMod(modExt.switch(switchObject))

  def withMod(mod: Modification): Split =
    if (this.modExt.mod eq mod) this else withMod(modExt.copy(mod = mod))

  def withMod(modExtByName: => ModExt): Split =
    if (isIgnored) this
    else {
      val modExt = modExtByName
      if (this.modExt eq modExt) this else copy(modExt = modExt)(line = line)
    }

  override def toString = {
    val prefix = tag match {
      case SplitTag.Ignored => "!"
      case SplitTag.Active => ""
      case _ => s"[$tag]"
    }
    val opt = optimalAt.fold("")(", opt=" + _)
    s"""$prefix${modExt.mod}:${line.value}(cost=$cost, indents=$indentation, $policy$opt)"""
  }
}

object Split {

  def ignored(implicit line: sourcecode.Line) =
    Split(ModExt(NoSplit), 0, tag = SplitTag.Ignored)

}
