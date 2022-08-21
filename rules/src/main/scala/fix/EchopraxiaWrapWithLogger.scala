package fix

import metaconfig.generic.Surface
import metaconfig.{ConfDecoder, Configured}
import scalafix.v1._

import scala.meta._

class EchopraxiaWrapWithLogger(config: EchopraxiaWrapWithLogger.Config) extends SemanticRule("EchopraxiaWrapWithLogger") {

  private val loggerType = SymbolMatcher.normalized(config.loggerType)
  private val loggerName = config.loggerName
  private val loggerMethod = config.loggerMethod

  def this() = this(EchopraxiaWrapWithLogger.Config())

  override def withConfiguration(config: Configuration): Configured[Rule] =
    config.conf.getOrElse("EchopraxiaWrapWithLogger")(this.config)
      .map { newConfig => new EchopraxiaWrapWithLogger(newConfig) }


  override def fix(implicit doc: SemanticDocument): Patch = {
    doc.tree.collect {
      case Defn.Def(mods, methodName, tparams, paramss, decltpe, body) =>
        body match {
          case Term.Apply(Term.Select(name, _), _) if matchesType(name) =>
            // Is "flowLogger.trace" already existing?  Then don't add it again.
            // Technically this looks for a matching symbol so you can have a different name on the logger.
            Patch.empty
          case other =>
            val add = s"$loggerName.$loggerMethod "
            println(other.pos.formatMessage("info",
              s"Adding $add to `${methodName}`"))
            Patch.addLeft(body, add)
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

object EchopraxiaWrapWithLogger {
  case class Config(loggerName: String = "flowLogger",
                    loggerMethod: String = "trace",
                    loggerType: String = "com.tersesystems.echopraxia.plusscala.flow.FlowLogger")

  object Config {
    val default = Config()

    implicit val surface: Surface[Config] = metaconfig.generic.deriveSurface[Config]
    implicit val decoder: ConfDecoder[Config] = metaconfig.generic.deriveDecoder(default)
  }
}