/*
rule = EchopraxiaRewriteToStructured
 */
package fix

import com.tersesystems.echopraxia.plusscala.LoggerFactory

object EchopraxiaRewriteToStructured_Test {

  private val logger = LoggerFactory.getLogger(getClass)

  private val slf4jLogger = org.slf4j.LoggerFactory.getLogger(getClass)

  final def someMethod: Unit = {
    val world = "world"
    val count = 3
    // format: off
    logger.info(s"hello $world there are $count statements")
    // format: on
  }

  final def someException: Unit = {
    val e = new RuntimeException()
    logger.info(s"exception $e")
  }

  final def exceptionArgument: Unit = {
    val world = "world"
    val e = new RuntimeException()
    logger.info(s"exception ${world}", e)
  }

  final def noSubstitute: Unit = {
    logger.debug(s"hello world there are count statements")
  }

  final def directMethod: Unit = {
    // format: off
    logger.debug(s"${System.currentTimeMillis}")
    // format: on
  }

  final def already: Unit = {
    val world = "world"
    val count = 3
    logger.info(
      "hello {} there are {} statements",
      fb => fb.list(fb.value("world", world), fb.value("count", count))
    )
  }

  final def notEchopraxiaLogger: Unit = {
    val world = "world"
    val count = 3
    slf4jLogger.info(s"hello $world there are $count statements")
  }
}
