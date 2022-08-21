/*
rule = EchopraxiaWrapWithLogger
EchopraxiaWrapWithLogger.loggerName = flowLogger
EchopraxiaWrapWithLogger.loggerMethod = trace
*/
package fix
import com.tersesystems.echopraxia.plusscala.flow._
import com.tersesystems.echopraxia.api.Value.ObjectValue

object EchopraxiaWrapFlowLogger_Test {
  object FieldBuilderWithUnit extends DefaultFlowFieldBuilder {
    implicit val unitToValue: ToValue[Unit] = _ => ObjectValue.EMPTY
  }

  private val flowLogger = FlowLoggerFactory.getLogger(getClass).withFieldBuilder(FieldBuilderWithUnit)

  final def someMethod: Unit = {
    println("hello")
  }
}
