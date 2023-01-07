package finance.tegro.api

import mu.KLogging
import org.ton.logger.Logger

class TonLogger(override var level: Logger.Level = Logger.Level.DEBUG) : Logger {
    override fun log(level: Logger.Level, message: () -> String) {
        when (level) {
            Logger.Level.DEBUG -> {
                logger.debug(message)
            }

            Logger.Level.FATAL -> {
                logger.error(message)
            }

            Logger.Level.INFO -> {
                logger.info(message)
            }

            Logger.Level.WARN -> {
                logger.warn(message)
            }
        }
    }

    companion object : KLogging()
}

