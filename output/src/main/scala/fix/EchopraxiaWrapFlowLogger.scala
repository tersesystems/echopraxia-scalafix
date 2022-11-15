package fix
import com.tersesystems.echopraxia.plusscala.flow._

object EchopraxiaWrapMethodWithLogger_Test {

  private val flowLogger = FlowLoggerFactory.getLogger(getClass)

  final def someMethod: Unit = flowLogger.trace {
    println("hello")
  }

  def methodWithoutBlock = flowLogger.trace { println("hello") }

  private def privateMethod = println("hello")

  // scalafix:off EchopraxiaWrapMethodWithLogger
  def scalafixOff = println("hello")
  // scalafix:on
}
