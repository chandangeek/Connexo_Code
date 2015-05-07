package com.elster.jupiter.util.logging;


import com.elster.jupiter.util.Pair;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class LoggingContext implements AutoCloseable {

    private static ThreadLocal<LoggingContext> context = ThreadLocal.withInitial(LoggingContext::new);

    private final List<Pair<String, String>> parameters = new ArrayList<>();

    private final LoggingContext parent;

    private LoggingContext() {
        this.parent = null;
    }

    private LoggingContext(LoggingContext parent) {
        this.parent = parent;
    }

    public static LoggingContext get() {
        LoggingContext context = LoggingContext.context.get();
        if (context == null) {
            context = new LoggingContext();
            LoggingContext.context.set(context);
        }
        return context;
    }

    public LoggingContext with(String key, String value) {
        LoggingContext loggingContext = new LoggingContext(this);
        loggingContext.parameters.add(Pair.of('{' + key + '}', value));
        context.set(loggingContext);
        return loggingContext;
    }

    public LoggingContext with(Map<String, String> params) {
        LoggingContext loggingContext = new LoggingContext(this);
        params.forEach((key, value) -> loggingContext.parameters.add(Pair.of('{' + key + '}', value)));
        context.set(loggingContext);
        return loggingContext;
    }

    @Override
    public void close() {
        context.set(parent);
    }

    public void log(Level level, Object logger, String message, Throwable throwable, Object... args) {
        Logger theLogger = null;
        if (logger instanceof Logger) {
            theLogger = (Logger) logger;
        } else {
            theLogger = Logger.getLogger(logger.getClass().getName());
        }
        StringBuilder messageBuilder = new StringBuilder(message);
        fillContext(messageBuilder);

        String resolvedMessage = MessageFormat.format(messageBuilder.toString(), args);

        theLogger.log(level, resolvedMessage, throwable);
    }

    public void severe(Object logger, String message, Throwable throwable, Object... args) {
        log(Level.SEVERE, logger, message, throwable, args);
    }

    public void severe(Object logger, Throwable throwable) {
        String message = throwable.getMessage() == null ? throwable.toString() : throwable.getMessage();
        severe(logger, message, throwable);
    }

    public void severe(Object logger, String message, Object... args) {
        log(Level.SEVERE, logger, message, null, args);
    }

    public void warning(Object logger, String message, Object... args) {
        log(Level.WARNING, logger, message, null, args);
    }

    public void info(Object logger, String message, Object... args) {
        log(Level.INFO, logger, message, null, args);
    }

    public void fine(Object logger, String message, Object... args) {
        log(Level.FINE, logger, message, null, args);
    }

    public void finer(Object logger, String message, Object... args) {
        log(Level.FINE, logger, message, null, args);
    }

    public void finest(Object logger, String message, Object... args) {
        log(Level.FINE, logger, message, null, args);
    }

    private void fillContext(StringBuilder message) {
        parameters.stream()
                .forEach(pair -> {
                    String replacement = pair.getLast() == null ? "?" : pair.getLast();
                    String newString = message.toString().replace(pair.getFirst(), replacement);
                    message.setLength(0);
                    message.append(newString);
                });
        if (parent != null) {
            parent.fillContext(message);
        }
    }

}

