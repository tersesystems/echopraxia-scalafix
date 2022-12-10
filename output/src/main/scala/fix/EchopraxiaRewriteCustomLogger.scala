package fix

import com.tersesystems.echopraxia.api.{Caller, CoreLogger, CoreLoggerFactory, FieldBuilderResult, Utilities}
import com.tersesystems.echopraxia.plusscala.{DefaultLoggerMethods, LoggerMethods}
import com.tersesystems.echopraxia.plusscala.api.{Condition, DefaultMethodsSupport, FieldBuilder, LoggerSupport}

object EchopraxiaRewriteCustomLogger {

  private val logger: MyLogger[FieldBuilder] = MyLoggerFactory.getLogger

  final def someMethod: Unit = {
    val world = "world"
    val count = 3
    // format: off
    logger.info("hello {} there are {} statements", fb => fb.list(fb.value("world", world), fb.value("count", count)))
    // format: on
  }

}

trait MyLogger[FB] extends LoggerMethods[FB] with LoggerSupport[FB, MyLogger] with DefaultMethodsSupport[FB]

object MyLogger {
  def apply[FB](core: CoreLogger, fieldBuilder: FB): MyLogger[FB] =
    new Impl[FB](core, fieldBuilder)

  class Impl[FB](val core: CoreLogger, val fieldBuilder: FB) extends MyLogger[FB] with DefaultLoggerMethods[FB] {
    override def name: String = core.getName

    override def withCondition(condition: Condition): MyLogger[FB] = {
      condition match {
        case Condition.always =>
          this
        case Condition.never =>
          newLogger(core.withCondition(Condition.never.asJava), fieldBuilder)
        case other =>
          newLogger(newCoreLogger = core.withCondition(other.asJava))
      }
    }

    override def withFields(f: FB => FieldBuilderResult): MyLogger[FB] = {
      import scala.compat.java8.FunctionConverters.enrichAsJavaFunction
      newLogger(newCoreLogger = core.withFields(f.asJava, fieldBuilder))
    }

    override def withThreadContext: MyLogger[FB] = {
      newLogger(
        newCoreLogger = core.withThreadContext(Utilities.threadContext())
      )
    }

    override def withFieldBuilder[NEWFB](
        newFieldBuilder: NEWFB
    ): MyLogger[NEWFB] = {
      newLogger(newFieldBuilder = newFieldBuilder)
    }

    @inline
    private def newLogger[T](
        newCoreLogger: CoreLogger = core,
        newFieldBuilder: T = fieldBuilder
    ): MyLogger[T] =
      new Impl[T](newCoreLogger, newFieldBuilder)
  }
}

object MyLoggerFactory {
  val FQCN: String = classOf[DefaultLoggerMethods[_]].getName

  val fieldBuilder: FieldBuilder = FieldBuilder

  def getLogger(name: String): MyLogger[FieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    MyLogger(core, fieldBuilder)
  }

  def getLogger(clazz: Class[_]): MyLogger[FieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    MyLogger(core, fieldBuilder)
  }

  def getLogger: MyLogger[FieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, Caller.resolveClassName)
    MyLogger(core, fieldBuilder)
  }

}
