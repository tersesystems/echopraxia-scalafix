package fix

import scalafix.v1._
import scala.meta._

class EchopraxiaScalafix extends SemanticRule("EchopraxiaScalafix") {

  private val loggerType = SymbolMatcher.normalized("com.tersesystems.echopraxia.plusscala.flow.FlowLogger")
  private val loggerName = "flowLogger"
  private val loggerMethod = "trace"

  override def fix(implicit doc: SemanticDocument): Patch = {

    doc.tree.collect {
      case Defn.Def(_, _, _, _, _, body) =>
        body match {
          case Term.Apply(Term.Select(name, _), _) if matchesType(name) =>
            // Is "flowLogger.trace" already existing?  Then don't add it again.
            // Technically this looks for a matching symbol so you can have a different name on the logger.
            Patch.empty
          case other =>
            Patch.addLeft (body, s"$loggerName.$loggerMethod ")
        }

    }.asPatch
  }

  private def matchesType(name: Term)(implicit doc: SemanticDocument): Boolean = {
    doc.info(name.symbol).collect { info =>
      info.signature match {
        case MethodSignature(_, _, TypeRef(_, symbol, _)) if loggerType.matches(symbol) =>
          true
      }
    }.getOrElse(false)
  }


}
