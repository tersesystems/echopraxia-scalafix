/*
rule = EchopraxiaWrapMethodWithLogger
EchopraxiaWrapMethodWithLogger.loggerName = logger
EchopraxiaWrapMethodWithLogger.loggerMethod = debug
*/
package fix

import com.tersesystems.echopraxia.plusscala.flow._

object EchopraxiaWrapFlowLoggerName_Test {

  private val logger = FlowLoggerFactory.getLogger(getClass)

  final def someMethod: Unit = {
    println("hello")
  }
}
