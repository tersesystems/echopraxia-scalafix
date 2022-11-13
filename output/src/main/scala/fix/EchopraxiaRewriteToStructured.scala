package fix

import com.tersesystems.echopraxia.plusscala.LoggerFactory

object EchopraxiaRewriteToStructured_Test {

  private val logger = LoggerFactory.getLogger(getClass)

  final def someMethod: Unit = {
    val world = "world"
    val count = 3
    logger.info("hello {} there are {} statements", fb => fb.list(fb.value("world", world), fb.value("count", count)))
  }

  final def someException: Unit = {    
    val e = new RuntimeException()
    logger.info(s"exception {}", fb => fb.exception(e))
  }

  final def noSubstitute: Unit = {
    logger.debug("hello world there are count statements")
  }

  final def directMethod: Unit = {
    logger.debug("{}", fb => fb.value("System.currentTimeMillis", System.currentTimeMillis))
  }

  final def already: Unit = {
    val world = "world"
    val count = 3
    logger.info("hello {} there are {} statements", fb => fb.list(fb.value("world", world), fb.value("count", count)))
  }
}
