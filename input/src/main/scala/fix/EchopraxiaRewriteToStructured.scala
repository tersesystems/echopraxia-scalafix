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

  final def suppression: Unit = {
    val world = "world"
    val count = 3
    // format: off
    logger.info(s"hello $world there are $count statements") // scalafix:ok
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

  final def rawStringInterpolation: Unit = {
    val world = "world"
    val count = 3
    val string = s"hello $world there are $count statements"
  }

  final def exceptionFirst: Unit = {
    val e = new RuntimeException()
    val count = 3
    logger.error(s"{} $count", e)
  }

  final def exceptionMiddle: Unit = {
    val e = new RuntimeException()
    val count1 = 1
    val count2 = 2
    logger.error(s"$count1 {} $count2", e)
  }

  final def exceptionLast: Unit = {
    val e = new RuntimeException()
    val count = 3
    logger.error(s"$count {}", e)
  }

  final def stringWithExistingArguments: Unit = {
    val world = "world"
    val count = 3
    logger.info(
      s"hello {} there are $count statements",
      _.keyValue("world", world)
    )
  }

  final def stringWithExistingArgumentsFull: Unit = {
    val three = "three"
    logger.info(
      s"{} {} $three",
      fb => fb.list(
        fb.keyValue("one", "one"),
        fb.keyValue("two", "two")
      ))
  }
}
