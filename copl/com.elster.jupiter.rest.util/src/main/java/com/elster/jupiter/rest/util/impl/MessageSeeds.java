/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    INVALID_VALUE(1, "RUT.InvalidValue", "Invalid value", Level.SEVERE),
    FIELD_CAN_NOT_BE_EMPTY(2, "RUT.FieldCanNotBeEmpty", "This field is required", Level.SEVERE),
    FIELD_SHOULD_HAVE_BEEN_ARRAY(3, "RUT.ExpectedArray", "Expected list of values", Level.SEVERE),
    OPTIMISTIC_LOCK_FAILED(4, "OptimisticLockFailed", "Another user or process modified this resource at the same time, please try again later", Level.SEVERE),
    INVALID_RANGE_FROM_AFTER_TO(5, "FromAfterTo", "Invalid range: from-date should be before to-date", Level.SEVERE),
    CONCURRENT_EDIT_TITLE(5, "ConcurrentModificationEditTitle", "Failed to save ''{0}''", Level.SEVERE),
    CONCURRENT_EDIT_BODY(6, "ConcurrentModificationEditMessage", "{0} has changed since the page was last updated.", Level.SEVERE),
    CONCURRENT_DELETE_TITLE(7, "ConcurrentModificationRemoveTitle", "Failed to remove ''{0}''", Level.SEVERE),
    CONCURRENT_DELETE_BODY(8, "ConcurrentModificationRemoveMessage", "{0} has changed since the page was last updated.", Level.SEVERE),
    INVALID_RANGE(9, "InvalidRange", "Invalid range", Level.SEVERE),
    INTERNAL_CONNEXO_ERROR(10, "internalConnexoError", "Connexo has encountered an error, please contact your system administrator", Level.SEVERE),
    MAC_ERROR(11, "MacFailure", "Message authentication check failed. Please contact your system administrator.", Level.SEVERE),
    PERSISTENCE_ERROR(12, "persistenceError", "Persistence layer error occurred. Probable cause: wrong request, database inconsistencies or connectivity", Level.SEVERE),
    INVALID_INPUT_VALUE(13, "RUT.InvalidInputValue", "Input contains forbidden characters", Level.FINER);

    public static final String COMPONENT_NAME = "RUT";

    private final int number;
    private final String key;
    private final String format;
    private final Level level;

    MessageSeeds(int number, String key, String format, Level level) {
        this.number = number;
        this.key = key;
        this.format = format;
        this.level = level;
    }

    @Override
    public String getModule() {
        return COMPONENT_NAME;
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
        return format;
    }

    @Override
    public Level getLevel() {
        return level;
    }

}
