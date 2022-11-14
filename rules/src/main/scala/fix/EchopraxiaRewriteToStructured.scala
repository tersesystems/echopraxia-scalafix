package fix

import metaconfig.ConfDecoder
import metaconfig.generic.Surface
import scalafix.v1._

import scala.meta._

class EchopraxiaRewriteToStructured(config: EchopraxiaRewriteToStructured.Config) extends SemanticRule("EchopraxiaRewriteToStructured") {

  private val loggerClass: String = config.loggerClass
  private val fieldBuilderMethod: String = config.fieldBuilderMethod

  def this() = this(EchopraxiaRewriteToStructured.Config())

  override def fix(implicit doc: SemanticDocument): Patch = {
    doc.tree.collect {
      case logger@Term.Apply(Term.Select(loggerName, methodName), List(Term.Interpolate(Term.Name("s"), parts, args))) if matchesType(loggerName) =>
        Patch.replaceTree(logger, rewrite(loggerName, methodName, parts, args))
    }.asPatch
  }

  private def matchesType(qual: Term)(implicit doc: SemanticDocument): Boolean = {
    val logger = SymbolMatcher.normalized(loggerClass)
    val info: SymbolInformation = qual.symbol.info.get
    info.signature match {
      case MethodSignature(_, _, TypeRef(_, symbol, _)) =>
        //println("symbol = " + symbol.structure)
        logger.matches(symbol)
      case other =>
        //println("other symbol = " + other.structure)
        false
    }
  }

  def isThrowable(signature: Signature): Boolean = {
    def toFqn(symbol: Symbol): String = symbol
      .value
      .replaceAll("/", ".")
      .replaceAll("\\.$", "\\$")
      .stripSuffix("#")
      .stripPrefix("_root_.")

    signature match {
      case ValueSignature(TypeRef(_, symbol, _)) =>
        val cl = this.getClass.getClassLoader
        classOf[Throwable].isAssignableFrom(cl.loadClass(toFqn(symbol)))
      case _ =>
        false
    }
  }

  private def rewrite(loggerTerm: Term, methodTerm: Term, parts: List[Lit], args: List[Term])(implicit doc: SemanticDocument): String = {
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
      val body = if (values.size == 1) values.head else s"""fb.list(${values.mkString(", ")})"""
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
    val default = Config()

    implicit val surface: Surface[Config] = metaconfig.generic.deriveSurface[Config]
    implicit val decoder: ConfDecoder[Config] = metaconfig.generic.deriveDecoder(default)
  }
}
