/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum MessageSeeds implements MessageSeed {
    CANNOT_SUBSCRIBE_ON_INACTIVE_DESTINATION(1001, "destination.inactive.cannotsubscribe", "DestinationSpec with name {0} was inactive when attempting to create a subscription with name {1}"),
    DUPLICATE_SUBSCRIBER_NAME(1002, "subscriber.duplicatename", "A subscriber with name {0} already exists."),
    MULTIPLE_SUBSCRIBER_ON_QUEUE(1003, "queue.multiplesubscriber", "Cannot register multiple subscribers on a queue, there is already a subscriber on queue {0}"),
    UNDERLYING_JMS_EXCEPTION(1004, "jms.failed", "Underlying JMS Exception"),
    UNDERLYING_AQ_EXCEPTION(1005, "aq.failed", "Underlying AQ Exception"),
    MAX_NUMBER_OF_RETRIES_OUT_OF_RANGE(1006, Keys.MAX_NUMBER_OF_RETRIES_OUT_OF_RANGE_KEY, "The number of retries of a queue should be between {min} and {max}"),
    RETRY_DELAY_OUT_OF_RANGE(1007, Keys.RETRY_DELAY_OUT_OF_RANGE_KEY, "The retry delay of a queue should be between {min} and {max} seconds")
    ;

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat) {
        this(number, key, defaultFormat, Level.SEVERE);
    }

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
    }

    @Override
    public String getModule() {
        return MessageService.COMPONENTNAME;
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

    public static interface Keys {
        String MAX_NUMBER_OF_RETRIES_OUT_OF_RANGE_KEY = "queue.retries.outofrange";
        String RETRY_DELAY_OUT_OF_RANGE_KEY = "queue.retrydelay.outofrange";
    }
}
