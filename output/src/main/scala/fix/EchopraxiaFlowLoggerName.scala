package fix

import com.tersesystems.echopraxia.plusscala.flow._
import com.tersesystems.echopraxia.api.Value.ObjectValue

object EchopraxiaWrapFlowLoggerName_Test {
  object FieldBuilderWithUnit extends DefaultFlowFieldBuilder {
    implicit val unitToValue: ToValue[Unit] = _ => ObjectValue.EMPTY
  }

  private val logger = FlowLoggerFactory.getLogger(getClass).withFieldBuilder(FieldBuilderWithUnit)

  final def someMethod: Unit = logger.debug {
    println("hello")
  }
}
