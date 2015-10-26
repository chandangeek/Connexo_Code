package com.elster.jupiter.appserver;


import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum MessageSeeds implements MessageSeed {

    SERVER_MESSAGE_QUEUE_MISSING(1001, "server.messagequeue.missing", "Server's message queue with name \"{0}\" not found", Level.SEVERE),
    APPSERVER_NAME_UNKNOWN(1002, "appserver.name.unknown", "AppServer with name \"{0}\" is unknown", Level.SEVERE),
    APPSERVER_STARTED_ANONYMOUSLY(2001, "appserver.started.anonymously", "AppServer started anonymously.", Level.WARNING),
    THREAD_UNCAUGHT_EXCEPTION(2002, "thread.uncaught.exception", "Uncaught exception occurred on thread {0}", Level.SEVERE),
    MESSAGEHANDLER_FAILED(2003, "messagehandler.failed", "Message handler failed", Level.SEVERE),
    FIELD_CAN_NOT_BE_EMPTY(3001, Keys.FIELD_CAN_NOT_BE_EMPTY, "This field can not be empty", Level.SEVERE),
    APPSERVER_NAME_INVALID_CHARS(3002, Keys.APPSERVER_NAME_INVALID_CHARS, "The name of a application server should comply with the hostname standard (rfc 952) and can therefore only contain a-z, A-Z, 0-9 and - characters", Level.SEVERE),
    APPSERVER_NAME_MUST_BE_UNIQUE(3003, Keys.NAME_MUST_BE_UNIQUE, "Name must be unique", Level.SEVERE),
    FIELD_SIZE_BETWEEN_1_AND_14(3004, Keys.FIELD_SIZE_BETWEEN_1_AND_14, "Field's text length should be between 1 and 14 characters", Level.SEVERE),
    FIELD_SIZE_BETWEEN_1_AND_80(3005, Keys.FIELD_SIZE_BETWEEN_1_AND_80, "Field's text length should be between 1 and 80 symbols", Level.SEVERE),
    ;

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
    }

    @Override
    public String getModule() {
        return AppService.COMPONENT_NAME;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    @Override
    public Level getLevel() {
        return level;
    }

    public void log(Logger logger, Thesaurus thesaurus, Object... args) {
        NlsMessageFormat format = thesaurus.getFormat(this);
        logger.log(getLevel(), format.format(args));
    }

    public void log(Logger logger, Thesaurus thesaurus, Throwable t, Object... args) {
        NlsMessageFormat format = thesaurus.getFormat(this);
        logger.log(getLevel(), format.format(args), t);
    }

    public static final class Keys {

        public static final String FIELD_CAN_NOT_BE_EMPTY = "can.not.be.empty";
        public static final String FIELD_SIZE_BETWEEN_1_AND_14 = "field.size.between.1and14";
        public static final String FIELD_SIZE_BETWEEN_1_AND_80 = "field.size.between.1and80";
        public static final String APPSERVER_NAME_INVALID_CHARS = "invalid.chars";
        public static final String NAME_MUST_BE_UNIQUE = "must.be.unique";
    }
}
