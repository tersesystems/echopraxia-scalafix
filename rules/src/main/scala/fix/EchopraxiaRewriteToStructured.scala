package fix

import metaconfig.{ConfDecoder, Configured}
import metaconfig.generic.Surface
import scalafix.lint.RuleDiagnostic
import scalafix.v1._

import scala.meta._

class EchopraxiaRewriteToStructured(
    config: EchopraxiaRewriteToStructured.Config
) extends SemanticRule("EchopraxiaRewriteToStructured") {

  private val loggerClass: String = config.loggerClass
  private val fieldBuilderMethod: String = config.fieldBuilderMethod

  def this() = this(EchopraxiaRewriteToStructured.Config())

  override def withConfiguration(config: Configuration): Configured[Rule] =
    config.conf
      .getOrElse("EchopraxiaRewriteToStructured")(this.config)
      .map { newConfig => new EchopraxiaRewriteToStructured(newConfig) }

  override def fix(implicit doc: SemanticDocument): Patch = {
    doc.tree.collect {
      case logger @ Term.Apply(
            Term.Select(loggerName, methodName),
            List(Term.Interpolate(Term.Name("s"), parts, args))
          ) if matchesType(loggerName) =>
        // logger.error(s"$one")
        Patch.replaceTree(logger, rewrite(loggerName, methodName, parts, args))

      case loggerWithArg @ Term.Apply(
            Term.Select(loggerName, methodName),
            List(Term.Interpolate(Term.Name("s"), parts, args), argumentTerm)
          ) if matchesException(argumentTerm) =>
        // logger.error(s"{} $one", e)
        // we're going to need to parse {} and keep track of the ordering.
        val orderedArgs = reorderArguments(parts, args, argumentTerm)
        Patch.replaceTree(
          loggerWithArg,
          rewrite(loggerName, methodName, parts, orderedArgs)
        )

      // don't try to do logger.info(s"$one {}", _.keyValue("foo", "bar"))
      // and also don't try logger.info("{}" + bar + "") string concatenation.
    }.asPatch.atomic // "atomic" means "respect scalafix:off"
  }

  private def matchesException(arg: Term)(implicit doc: SemanticDocument): Boolean =
    arg.symbol.info.exists(info => isThrowable(info.signature))

  private def reorderArguments(parts: List[Lit], args: List[Term], argumentTerm: Term): List[Term] = {
    val index = parts.zipWithIndex.collectFirst {
      case (el: Lit.String, i) if el.value.contains("{}") => i
    }.getOrElse(-1)

    index match {
      case -1 =>
        args :+ argumentTerm
      case 0 =>
        argumentTerm +: args
      case i =>
        (args.slice(0, i) :+ argumentTerm) ++ args.slice(i, args.length)
    }
  }

  private def matchesType(
      qual: Term
  )(implicit doc: SemanticDocument): Boolean = {
    val loggerSymbolMatcher = SymbolMatcher.normalized(loggerClass)
    val info: SymbolInformation = qual.symbol.info.get
    info.signature match {
      case MethodSignature(_, _, TypeRef(_, symbol, _)) =>
        loggerSymbolMatcher.matches(symbol)
      case other =>
        // println("other symbol = " + other.structure)
        false
    }
  }

  def isThrowable(signature: Signature): Boolean = {
    def toFqn(symbol: Symbol): String = symbol.value
      .replaceAll("/", ".")
      .replaceAll("\\.$", "\\$")
      .stripSuffix("#")
      .stripPrefix("_root_.")

    signature match {
      case ValueSignature(TypeRef(_, symbol, _)) =>
        val cl = this.getClass.getClassLoader
        try {
          classOf[Throwable].isAssignableFrom(cl.loadClass(toFqn(symbol)))
        } catch {
          case e: Exception =>
            false
        }
      case _ =>
        false
    }
  }

  private def rewrite(
      loggerTerm: Term,
      methodTerm: Term,
      parts: List[Lit],
      args: List[Term]
  )(implicit doc: SemanticDocument): String = {
    if (args.isEmpty) {
      val template = parts.map(_.value.toString).mkString("{}")
      s"""$loggerTerm.$methodTerm("$template")"""
    } else {
      val template = parts.map(_.value.toString).mkString("{}")
      val values = args.map {
        case arg: Term.Name =>
          if (isThrowable(arg.symbol.info.get.signature)) {
            s"""fb.exception($arg)"""
          } else {
            s"""fb.$fieldBuilderMethod("$arg", $arg)"""
          }
        case other =>
          // XXX I don't think this is possible?
          s"""fb.$fieldBuilderMethod("$other", $other)"""
      }
      val body =
        if (values.size == 1) values.head
        else s"""fb.list(${values.mkString(", ")})"""
      s"""$loggerTerm.$methodTerm("$template", fb => $body)"""
    }
  }
}

object EchopraxiaRewriteToStructured {
  case class Config(
      loggerClass: String = "com.tersesystems.echopraxia.plusscala.Logger",
      fieldBuilderMethod: String = "value"
  )

  object Config {
    val default: Config = Config()

    implicit val surface: Surface[Config] =
      metaconfig.generic.deriveSurface[Config]
    implicit val decoder: ConfDecoder[Config] =
      metaconfig.generic.deriveDecoder(default)
  }
}
