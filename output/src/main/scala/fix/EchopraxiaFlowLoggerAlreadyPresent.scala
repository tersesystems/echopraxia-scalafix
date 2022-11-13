package fix

import com.tersesystems.echopraxia.plusscala.flow._
import com.tersesystems.echopraxia.api.Value.ObjectValue

object EchopraxiaFlowLoggerAlreadyPresent_Test {

  private val flowLogger = FlowLoggerFactory.getLogger(getClass)

  final def someMethod: Unit = flowLogger.trace {
    println("hello")
  }
}
