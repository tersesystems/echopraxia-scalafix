package fix

import scalafix.v1._
import scala.meta._

class EchopraxiaScalafix extends SemanticRule("EchopraxiaScalafix") {

  override def fix(implicit doc: SemanticDocument): Patch = {

    doc.tree.collect {
      case Defn.Def(mods, name, tparams, paramss, decltpe, body) =>
        body match {
          case Term.Apply(Term.Select(Term.Name("flowLogger"), Term.Name("trace")), _) =>
            Patch.empty
          case other =>
            println("body.structure: " + other.structure)
            Patch.addLeft (body, "flowLogger.trace ")
        }

    }.asPatch
  }

}
