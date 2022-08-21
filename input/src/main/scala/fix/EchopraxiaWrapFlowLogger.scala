/*
rule = EchopraxiaWrapMethodWithLogger
*/
package fix
import com.tersesystems.echopraxia.plusscala.flow._
import com.tersesystems.echopraxia.api.Value.ObjectValue

object EchopraxiaWrapMethodWithLogger_Test {
  object FieldBuilderWithUnit extends DefaultFlowFieldBuilder {
    implicit val unitToValue: ToValue[Unit] = _ => ObjectValue.EMPTY
  }

  private val flowLogger = FlowLoggerFactory.getLogger(getClass).withFieldBuilder(FieldBuilderWithUnit)

  final def someMethod: Unit = {
    println("hello")
  }

  def methodWithoutBlock = println("hello")

  private def privateMethod = println("hello")

  // scalafix:off EchopraxiaWrapMethodWithLogger
  def scalafixOff = println("hello")
  // scalafix:on
}
