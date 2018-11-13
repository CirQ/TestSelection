package utils;

import org.apache.maven.plugin.logging.Log;

public class LoggingHandler {
    private static Log logger = null;

    private LoggingHandler(){}

    public static void initializeLogger(Log logger){
        LoggingHandler.logger = logger;
    }

    public static void debug(CharSequence content){
        LoggingHandler.logger.debug(content);
    }

    public static void info(CharSequence content){
        LoggingHandler.logger.info(content);
    }

    public static void warn(CharSequence content){
        LoggingHandler.logger.warn(content);
    }

    public static void error(CharSequence content){
        LoggingHandler.logger.error(content);
    }
}
