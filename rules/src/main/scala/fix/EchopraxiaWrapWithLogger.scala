package fix

import metaconfig.generic.Surface
import metaconfig.{ConfDecoder, Configured}
import scalafix.v1._

import scala.meta._

class EchopraxiaWrapMethodWithLogger(
    config: EchopraxiaWrapMethodWithLogger.Config
) extends SemanticRule("EchopraxiaWrapMethodWithLogger") {

  private val loggerName = config.loggerName
  private val loggerMethod = config.loggerMethod
  private val methodAccess = config.methodAccess

  def this() = this(EchopraxiaWrapMethodWithLogger.Config())

  override def withConfiguration(config: Configuration): Configured[Rule] =
    config.conf
      .getOrElse("EchopraxiaWrapMethodWithLogger")(this.config)
      .map { newConfig => new EchopraxiaWrapMethodWithLogger(newConfig) }

  override def fix(implicit doc: SemanticDocument): Patch = {
    val add = s"$loggerName.$loggerMethod"
    doc.tree.collect {
      case defn @ Defn.Def(mods, methodName, tparams, paramss, decltpe, body) =>
        if (isValidAccessModifier(mods)) {
          body match {
            case Term.Apply(Term.Select(name, _), _) if matchesType(name) =>
              // Is "flowLogger.trace" already existing?  Then don't add it again.
              // Technically this looks for a matching symbol so you can have a different name on the logger.
              Patch.empty
            case block @ Term.Block(statements) =>
              println(
                methodName.pos.formatMessage(
                  "info",
                  s"Adding $add to `${methodName}`"
                )
              )
              Patch.addLeft(block, s"$add ").atomic
            case other =>
              println(
                methodName.pos.formatMessage(
                  "info",
                  s"Adding $add block to `${methodName}`"
                )
              )
              Patch.addAround(body, s"$add { ", " }").atomic
          }
        } else {
          Patch.empty
        }
    }.asPatch
  }

  private def matchesType(
      name: Term
  )(implicit doc: SemanticDocument): Boolean = {
    name match {
      case Term.Name(n) if n == loggerName =>
        true
      case _ =>
        false
    }
  }

  private def isValidAccessModifier(mods: List[Mod]): Boolean = {
    methodAccess match {
      case "public" =>
        // if we find Protected or Private anything, it's not public.
        !mods.exists(m =>
          m.isInstanceOf[Mod.Private] || m.isInstanceOf[Mod.Protected]
        )
      case "protected" =>
        !mods.exists(m => m.isInstanceOf[Mod.Private])
      case "private" =>
        true
    }
  }
}

object EchopraxiaWrapMethodWithLogger {
  case class Config(
      loggerName: String = "flowLogger",
      loggerMethod: String = "trace",
      loggerType: String =
        "com.tersesystems.echopraxia.plusscala.flow.FlowLogger",
      methodAccess: String = "public"
  )

  object Config {
    val default = Config()

    implicit val surface: Surface[Config] =
      metaconfig.generic.deriveSurface[Config]
    implicit val decoder: ConfDecoder[Config] =
      metaconfig.generic.deriveDecoder(default)
  }
}
