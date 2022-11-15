package fix

import com.tersesystems.echopraxia.plusscala.flow._

object EchopraxiaFlowLoggerAlreadyPresent_Test {

  private val flowLogger = FlowLoggerFactory.getLogger(getClass)

  final def someMethod: Unit = flowLogger.trace {
    println("hello")
  }
}
