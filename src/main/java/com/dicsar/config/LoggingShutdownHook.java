package com.dicsar.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ch.qos.logback.classic.LoggerContext;

@Component
public class LoggingShutdownHook {

    private static final Logger logger = LoggerFactory.getLogger(LoggingShutdownHook.class);
    private volatile boolean flushed;

    public LoggingShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::flushLogs, "dicsar-log-flush-shutdown-hook"));
    }

    private synchronized void flushLogs() {
        if (flushed) {
            return;
        }
        flushed = true;
        try {
            logger.warn("Aplicacion cerrandose: forzando flush de logs antes de terminar.");
            System.out.flush();
            System.err.flush();

            Object factory = LoggerFactory.getILoggerFactory();
            if (factory instanceof LoggerContext loggerContext) {
                loggerContext.stop();
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            System.err.flush();
        }
    }
}
