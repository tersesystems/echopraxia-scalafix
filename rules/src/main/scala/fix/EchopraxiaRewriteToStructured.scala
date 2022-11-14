package fix

import metaconfig.ConfDecoder
import metaconfig.generic.Surface
import scalafix.v1._

import scala.meta._

class EchopraxiaRewriteToStructured(config: EchopraxiaRewriteToStructured.Config) extends SemanticRule("EchopraxiaRewriteToStructured") {

  private val loggerName = config.loggerName

  def this() = this(EchopraxiaRewriteToStructured.Config())

  override def fix(implicit doc: SemanticDocument): Patch = {
    doc.tree.collect {
      case logger@Term.Apply(Term.Select(loggerName, methodName), List(Term.Interpolate(Term.Name("s"), parts, args))) if matchesType(loggerName) =>
        Patch.replaceTree(logger, rewrite(loggerName, methodName, parts, args))
    }.asPatch
  }

  private def matchesType(name: Term)(implicit doc: SemanticDocument): Boolean = {
    name match {
      case Term.Name(n) if n == loggerName =>
        true
      case _ =>
        false
    }
  }

  def isThrowable(signature: Signature): Boolean = {
    signature match {
      case ValueSignature(tpe) =>
        tpe match {
          case TypeRef(prefix, symbol, typeArguments) =>
            // Check that it has throwable at the root?
            val className = toFqn(symbol)
            val cl = this.getClass.getClassLoader
            classOf[Throwable].isAssignableFrom(cl.loadClass(className))
          case other =>
            false
        }
      case other =>
        false
    }
  }

  def toFqn(symbol: Symbol): String = symbol.value.replaceAll("/", ".").replaceAll("\\.$", "\\$").stripSuffix("#").stripPrefix("_root_.")

  private def rewrite(loggerTerm: Term, methodTerm: Term, parts: List[Lit], args: List[Term])(implicit doc: SemanticDocument): String = {
    if (args.isEmpty) {
      val template = parts.map(_.value.toString).mkString("{}")
      s"""$loggerTerm.$methodTerm("$template")"""
    } else {
      val template = parts.map(_.value.toString).mkString("{}")
      val values = args.map {
        case arg@(argName: Term) =>
          if (isThrowable(argName.symbol.info.get.signature)) {
            s"""fb.exception($arg)"""
          } else {
            s"""fb.value("$arg", $arg)"""
          }
        case other =>
          // XXX how do I log an error?
          println("WAT")
      }
      val body = if (values.size == 1) values.head else s"""fb.list(${values.mkString(", ")})"""
      s"""$loggerTerm.$methodTerm("$template", fb => $body)"""
    }
  }
}


object EchopraxiaRewriteToStructured {
  case class Config(loggerName: String = "logger")

  object Config {
    val default = Config()

    implicit val surface: Surface[Config] = metaconfig.generic.deriveSurface[Config]
    implicit val decoder: ConfDecoder[Config] = metaconfig.generic.deriveDecoder(default)
  }
}
