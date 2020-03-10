package net.stzups.tanks;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LoggerHandler {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Tanks.class.getName());

    static Logger setLogger() {
        Handler consoleHandler = new ConsoleHandler();
        logger.setUseParentHandlers(false);
        logger.addHandler(consoleHandler);
        consoleHandler.setLevel(Level.ALL);
        consoleHandler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                return "["
                        + new SimpleDateFormat("HH:mm:ss").format(new Timestamp(System.currentTimeMillis()))
                        + "] ["
                        + record.getLevel()
                        + "] "
                        + record.getMessage()
                        + System.lineSeparator();
            }
        });
        return logger;
    }
}
