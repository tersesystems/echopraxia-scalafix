package fix

import scalafix.v1._

import scala.meta._

class EchopraxiaRewriteStatements extends SemanticRule("EchopraxiaRewriteStatements") {

  override def fix(implicit doc: SemanticDocument): Patch = {
    doc.tree.collect {
      // first, find a logger... then find the logging statement (info/debug/trace/warn/error)
      case apply @ Term.Apply(Term.Select(logger, loggerMethod), args) if matchesType(logger) && matchesMethod(loggerMethod)  =>
        // then, see if the interpolation refers to outside variables that are already declared.
        // if they refer to a field or variable, then we can just use the variable name as the key.
        // if not, we'll generate a fresh name for them.

        // first arg is either a literal string, or an interpolation (we don't handle strings l-values)
        val string = args(0)

        // there may be existing arguments!
        // If the existing argument is an exception, we know what to do.
        // if the second argument is a field builder exception, we're going to have to rewrite it.
        // that could be kind of iffy, so we'll just abort right now and ask the user to do it manually.
        println(args.structure)
        Patch.empty
      case s =>
        Patch.empty
    }.asPatch
  }

  private val loggerType = SymbolMatcher.normalized("com.tersesystems.echopraxia.plusscala.Logger")

  private def matchesType(name: Term)(implicit doc: SemanticDocument): Boolean = {
    doc.info(name.symbol).exists { info =>
      info.signature match {
        case MethodSignature(typeParameters, parameterLists, returnType) =>
          returnType match {
            case TypeRef(prefix: SemanticType, symbol: Symbol, typeArguments: List[SemanticType]) =>
              loggerType.matches(sym = symbol)
            case _ =>
              false
          }
        case _ =>
          false
      }
    }
  }

  private def matchesMethod(term: Term)(implicit doc: SemanticDocument): Boolean = {
    term match {
      case Term.Name(name) =>
        name == "info" || name == "warn" || name == "error" || name == "debug" || name == "trace"
      case _ =>
        false
    }
  }

}
