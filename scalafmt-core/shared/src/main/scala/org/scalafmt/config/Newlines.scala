package org.scalafmt.config

import org.scalafmt.config.Newlines.AfterInfix

import metaconfig._
import metaconfig.generic.Surface

/**
  * @param penalizeSingleSelectMultiArgList
  *   If true, adds a penalty to newlines before a dot starting a select
  *   chain of length one and argument list. The penalty matches the number
  *   of arguments to the select chain application.
  *   {{{
  *     // If true, favor
  *     logger.elem(a,
  *                 b,
  *                 c)
  *     // instead of
  *     logger
  *       .elem(a, b, c)
  *
  *     // penalty is proportional to argument count, example:
  *     logger.elem(a, b, c)    // penalty 2
  *     logger.elem(a, b, c, d) // penalty 3, etc.
  *   }}}
  *
  *   If false, matches pre-v0.5 behavior. Note. this option may be
  *   removed in a future release.
  * @param neverBeforeJsNative If true, a newline will never be placed in
  *                                 front of js.native.
  * @param sometimesBeforeColonInMethodReturnType If true, scalafmt
  *                                               may choose to put a newline
  *                                               before colon : at defs.
  * @param alwaysBeforeCurlyBraceLambdaParams
  *   If true, puts a newline after the open brace
  *   and the parameters list of an anonymous function.
  *   For example
  *   something.map {
  *     n =>
  *       consume(n)
  *   }
  * @param afterCurlyLambda
  *   If `never` (default), it will remove any extra lines below curly lambdas
  *   {{{
  *   something.map { x =>
  *
  *     f(x)
  *   }
  *   }}}
  *   will become
  *   {{{
  *   something.map { x =>
  *     f(x)
  *   }
  *   }}}
  *
  *   If `always`, it will always add one empty line (opposite of `never`).
  *   If `preserve`, and there isn't an empty line, it will keep it as it is.
  *   If there is one or more empty lines, it will place a single empty line.
  *
  *   If `squash`, it will try to squash lambda body in one line:
  *
  *   {{{
  *     xs.map { x =>
  *       x + 1
  *     }
  *   }}}
  *   will become
  *   {{{
  *     xs.map { x => x + 1 }
  *   }}}
  * @param alwaysBeforeElseAfterCurlyIf if true, add a new line between the end of a curly if and the following else.
  *   For example
  *   if(someCond) {
  *     // ...
  *   }
  *   else //...
  * @param alwaysBeforeMultilineDef
  *   If true, add a newline before the body of a multiline def without
  *   curly braces. See #1126 for discussion.
  *   For example,
  *   {{{
  *     // newlines.alwaysBeforeMultilineDef = false
  *     def foo(bar: Bar): Foo = bar
  *       .flatMap(f)
  *       .map(g)
  *
  *     // newlines.alwaysBeforeMultilineDef = true
  *     def foo(bar: Bar): Foo =
  *       bar
  *         .flatMap(f)
  *         .map(g)
  *   }}}
  * @param avoidAfterYield
  *   If false (legacy behavior), inserts unconditional line break after `yield`
  *   if the yield body doesn't fit on a single line.
  *   For example,
  *   {{{
  *     // newlines.avoidAfterYield = true (default)
  *     for (a <- as)
  *     yield Future {
  *       ...
  *     }
  *
  *     // newlines.avoidAfterYield = false (default before v2).
  *     for (a <- as)
  *     yield
  *       Future {
  *         ...
  *       }
  *   }}}
  * @param source
  *   Controls how line breaks in the input source are handled
  *   If `classic` (default), the old mixed behaviour will be used
  *   If `keep`, try to keep source newlines
  *   If `fold`, ignore source and try to remove line breaks
  *   If `unfold`, ignore source and try to break lines
  * @param afterInfix
  *   Controls how line breaks around operations are handled
  *   If `keep` (default for source=classic,keep), preserve existing
  *   If `some` (default for source=fold), break after some infix ops
  *   If `many` (default for source=unfold), break after many infix ops
  * @param afterInfixBreakOnNested
  *   Force breaks around nested (enclosed in parentheses) expressions
  * @param afterInfixMaxCountPerFile
  *   Switch to `keep` for a given file if the total number of infix
  *   operations in that file exceeds this value
  * @param afterInfixMaxCountPerExprForSome
  *   Switch to `many` for a given expression (possibly nested) if the
  *   number of operations in that expression exceeds this value AND
  *   `afterInfix` had been set to `some`.
  * @param topLevelStatements
  *   Forces a blank line before and/or after a top-level statement.
  * @param topLevelStatementsMinBreaks
  *   Minimum span (number of line breaks between first and last line)
  *   to start forcing blank lines.
  * @param avoidForSimpleOverflow
  *   - punct: don't force break if overflow is only due to trailing punctuation
  *   - tooLong: don't force break if overflow is due to tokens which are too long
  *     and would likely overflow even after a break
  */
case class Newlines(
    source: Newlines.SourceHints = Newlines.classic,
    neverInResultType: Boolean = false,
    neverBeforeJsNative: Boolean = false,
    sometimesBeforeColonInMethodReturnType: Boolean = true,
    penalizeSingleSelectMultiArgList: Boolean = true,
    alwaysBeforeCurlyBraceLambdaParams: Boolean = false,
    topLevelStatementsMinBreaks: Int = 1,
    topLevelStatements: Seq[Newlines.BeforeAfter] = Seq.empty,
    @annotation.DeprecatedName(
      "alwaysBeforeTopLevelStatements",
      "Use newlines.topLevelStatements instead",
      "2.5.0"
    )
    alwaysBeforeTopLevelStatements: Boolean = false,
    afterCurlyLambda: NewlineCurlyLambda = NewlineCurlyLambda.never,
    implicitParamListModifierForce: Seq[Newlines.BeforeAfter] = Seq.empty,
    implicitParamListModifierPrefer: Option[Newlines.BeforeAfter] = None,
    alwaysBeforeElseAfterCurlyIf: Boolean = false,
    alwaysBeforeMultilineDef: Boolean = true,
    afterInfix: Option[AfterInfix] = None,
    afterInfixBreakOnNested: Boolean = false,
    afterInfixMaxCountPerExprForSome: Int = 10,
    afterInfixMaxCountPerFile: Int = 500,
    avoidForSimpleOverflow: Seq[Newlines.AvoidForSimpleOverflow] = Seq.empty,
    avoidAfterYield: Boolean = true
) {
  if (
    implicitParamListModifierForce.nonEmpty &&
    implicitParamListModifierPrefer.nonEmpty
  ) {
    throw new ScalafmtConfigException(
      "can't specify both " +
        "implicitParamListModifierForce and implicitParamListModifierPrefer"
    )
  }

  val reader: ConfDecoder[Newlines] = generic.deriveDecoder(this).noTypos
  if (source != Newlines.classic) Newlines.warnSourceIsExperimental

  @inline
  def sourceIs(hint: Newlines.SourceHints): Boolean =
    hint eq source

  @inline
  def sourceIn(hints: Newlines.SourceHints*): Boolean =
    hints.contains(source)

  val sourceIgnored: Boolean =
    sourceIn(Newlines.fold, Newlines.unfold)

  val breakAfterInfix: AfterInfix =
    afterInfix.getOrElse {
      source match {
        case Newlines.unfold => AfterInfix.many
        case Newlines.fold => AfterInfix.some
        case Newlines.keep => AfterInfix.keep
        case Newlines.classic => AfterInfix.keep
      }
    }
  val formatInfix: Boolean = breakAfterInfix ne AfterInfix.keep

  def checkInfixConfig(infixCount: Int): Newlines = {
    val needAfterInfix: AfterInfix =
      if (infixCount > afterInfixMaxCountPerFile) AfterInfix.keep
      else breakAfterInfix
    if (needAfterInfix == breakAfterInfix) this
    else copy(afterInfix = Some(needAfterInfix))
  }

  lazy val forceBeforeImplicitParamListModifier: Boolean =
    implicitParamListModifierForce.contains(Newlines.before)
  lazy val forceAfterImplicitParamListModifier: Boolean =
    implicitParamListModifierForce.contains(Newlines.after)

  private def preferBeforeImplicitParamListModifier: Boolean =
    implicitParamListModifierPrefer.contains(Newlines.before)
  lazy val notPreferAfterImplicitParamListModifier: Boolean =
    implicitParamListModifierForce.nonEmpty ||
      preferBeforeImplicitParamListModifier
  lazy val notBeforeImplicitParamListModifier: Boolean =
    if (implicitParamListModifierForce.isEmpty)
      !preferBeforeImplicitParamListModifier
    else
      !forceBeforeImplicitParamListModifier

  lazy val forceBlankBeforeMultilineTopLevelStmt: Boolean =
    topLevelStatements.contains(Newlines.before) ||
      alwaysBeforeTopLevelStatements
  lazy val forceBlankAfterMultilineTopLevelStmt: Boolean =
    topLevelStatements.contains(Newlines.after)

  lazy val avoidForSimpleOverflowPunct: Boolean =
    avoidForSimpleOverflow.contains(Newlines.AvoidForSimpleOverflow.punct)
  lazy val avoidForSimpleOverflowTooLong: Boolean =
    avoidForSimpleOverflow.contains(Newlines.AvoidForSimpleOverflow.tooLong)
}

object Newlines {
  implicit lazy val surface: Surface[Newlines] = generic.deriveSurface
  implicit lazy val encoder: ConfEncoder[Newlines] = generic.deriveEncoder

  sealed abstract class SourceHints
  // the classic handler of source newlines
  case object classic extends SourceHints
  // try to keep newlines
  case object keep extends SourceHints
  // try to fold newlines into spaces (but not semicolons)
  case object fold extends SourceHints
  // try to turn spaces and semicolons into newlines
  case object unfold extends SourceHints

  implicit val sourceHintsReader: ConfCodec[SourceHints] =
    ReaderUtil.oneOf[SourceHints](keep, fold, unfold)

  private lazy val warnSourceIsExperimental: Unit =
    Console.err.println(
      "newlines.source is experimental, will change ignoring edition setting"
    )

  sealed abstract class AfterInfix
  object AfterInfix {
    case object keep extends AfterInfix
    case object some extends AfterInfix
    case object many extends AfterInfix

    implicit val reader: ConfCodec[AfterInfix] =
      ReaderUtil.oneOf[AfterInfix](keep, some, many)
  }

  sealed abstract class BeforeAfter
  case object before extends BeforeAfter
  case object after extends BeforeAfter

  implicit val beforeAfterReader: ConfCodec[BeforeAfter] =
    ReaderUtil.oneOf[BeforeAfter](before, after)

  sealed abstract class AvoidForSimpleOverflow
  object AvoidForSimpleOverflow {
    case object punct extends AvoidForSimpleOverflow
    case object tooLong extends AvoidForSimpleOverflow
    implicit val codec: ConfCodec[AvoidForSimpleOverflow] =
      ReaderUtil.oneOf[AvoidForSimpleOverflow](punct, tooLong)
  }

}

sealed abstract class NewlineCurlyLambda

object NewlineCurlyLambda {

  case object preserve extends NewlineCurlyLambda
  case object always extends NewlineCurlyLambda
  case object never extends NewlineCurlyLambda
  case object squash extends NewlineCurlyLambda

  implicit val newlineCurlyLambdaReader: ConfCodec[NewlineCurlyLambda] =
    ReaderUtil.oneOf[NewlineCurlyLambda](preserve, always, never, squash)
}
