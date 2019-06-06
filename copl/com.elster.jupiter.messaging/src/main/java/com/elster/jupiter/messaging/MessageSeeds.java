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
    RETRY_DELAY_OUT_OF_RANGE(1007, Keys.RETRY_DELAY_OUT_OF_RANGE_KEY, "The retry delay of a queue should be between {min} and {max} seconds"),
    EMPTY_QUEUE_NAME(1008, Keys.EMPTY_QUEUE_NAME, "Queue name is missing or contains forbidden characters."),
    EMPTY_QUEUE_TYPE_NAME(1009, Keys.EMPTY_QUEUE_TYPE_NAME, "Queue type is missing from request."),
    DUPLICATE_QUEUE(1010, Keys.DUPLICATE_QUEUE, "Queue is already defined"),
    QUEUE_NAME_TOO_LONG(1011, Keys.QUEUE_NAME_TOO_LONG, "Queue name is too long."),
    TASKS_NOT_EMPTY(1012, Keys.TASKS_NOT_EMPTY, "Before deleting the queue please remove the corresponding tasks"),
    ACTIVE_SUBSCRIBER_DEFINED_FOR_QUEUE(1013, Keys.ACTIVE_SUBSCRIBER_DEFINED_FOR_QUEUE, "Before deleting the queue please delete the corresponding message service"),
    SERVICE_CALL_TYPES_NOT_EMPTY(1014, Keys.SERVICE_CALL_TYPES_NOT_EMPTY, "Before deleting the queue please remove the corresponding service call types.");

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

    public interface Keys {
        String MAX_NUMBER_OF_RETRIES_OUT_OF_RANGE_KEY = "queue.retries.outofrange";
        String RETRY_DELAY_OUT_OF_RANGE_KEY = "queue.retrydelay.outofrange";
        String EMPTY_QUEUE_NAME = "queue.name.empty";
        String EMPTY_QUEUE_TYPE_NAME = "queue.type.empty";
        String DUPLICATE_QUEUE = "queue.duplicate.name";
        String QUEUE_NAME_TOO_LONG = "queue.name.too.long";
        String TASKS_NOT_EMPTY = "queue.delete.tasks.not.empty";
        String ACTIVE_SUBSCRIBER_DEFINED_FOR_QUEUE = "queue.subscribers.active";
        String SERVICE_CALL_TYPES_NOT_EMPTY = "queue.delete.service.call.types.not.empty";
    }

}
