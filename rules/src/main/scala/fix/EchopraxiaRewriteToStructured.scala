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

  private def rewrite(loggerTerm: Term, methodTerm: Term, parts: List[Lit], args: List[Term])(implicit doc: SemanticDocument): String = {
    if (args.isEmpty) {
      val template = parts.map(_.value.toString).mkString("{}")
      s"""$loggerTerm.$methodTerm("$template")"""
    } else {
      val template = parts.map(_.value.toString).mkString("{}")
      val values = args.map { arg =>
         // if the term name references an exception, I want 
         // fb.exception($arg)
         arg match {
           case argName: Term =>
            val info = argName.symbol.info.get            
            println("signature " + info.signature)
         }
         s"""fb.value("$arg", $arg)"""
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
