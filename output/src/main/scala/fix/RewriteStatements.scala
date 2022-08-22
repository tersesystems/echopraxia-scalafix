package fix

import com.tersesystems.echopraxia.plusscala.LoggerFactory

import java.time.Instant

object RewriteStatements {

  private final val logger = LoggerFactory.getLogger

  val date = Instant.now.toString
  logger.info(s"the date today is {}", fb => fb.value("date", date))

}
