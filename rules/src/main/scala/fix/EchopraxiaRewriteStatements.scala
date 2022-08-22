package fix

import scalafix.v1._

import scala.meta._

class EchopraxiaRewriteStatements extends SemanticRule("EchopraxiaRewriteStatements") {

  override def fix(implicit doc: SemanticDocument): Patch = {
    doc.tree.collect {
      // first, find a logger... then find the logging statement (info/debug/trace/warn/error)
      case apply @ Term.Apply(Term.Select(logger, loggerMethod), args) if  =>
        // then, see if the interpolation refers to outside variables that are already declared.
        // if they refer to a field or variable, then we can just use the variable name as the key.
        // if not, we'll generate a fresh name for them.

        println(apply.structure)
      case s =>
        //println(s.structure)
    }
    Patch.empty
  }

  private val loggerType = SymbolMatcher.normalized("com.tersesystems.echopraxia.plusscala.Logger")

  private def matchesType(name: Term)(implicit doc: SemanticDocument): Boolean = {
    false
    //    doc.info(name.symbol).collect { info =>
    //      info.signature match {
    //        case  loggerType.matches(symbol) =>
    //          true
    //      }
    //    }.getOrElse(false)
  }
}
