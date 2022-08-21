/*
rule = EchopraxiaWrapWithLogger
*/
package fix

import com.tersesystems.echopraxia.plusscala.flow._
import com.tersesystems.echopraxia.api.Value.ObjectValue

object EchopraxiaFlowLoggerAlreadyPresent_Test {
  object FieldBuilderWithUnit extends DefaultFlowFieldBuilder {
    implicit val unitToValue: ToValue[Unit] = _ => ObjectValue.EMPTY
  }

  private val flowLogger = FlowLoggerFactory.getLogger(getClass).withFieldBuilder(FieldBuilderWithUnit)

  final def someMethod: Unit = flowLogger.trace {
    println("hello")
  }
}
