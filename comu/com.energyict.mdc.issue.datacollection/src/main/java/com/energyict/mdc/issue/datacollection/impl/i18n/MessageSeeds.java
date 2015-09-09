package com.energyict.mdc.issue.datacollection.impl.i18n;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    // Events 701 - 999
    EVENT_BAD_DATA_NO_DEVICE(701, "EventBadDataNoDevice", "Unable to process issue creation event because target device (id = {0}) wasn't found", Level.SEVERE),
    EVENT_BAD_DATA_NO_KORE_DEVICE(702, "EventBadDataNoEndDevice", "Unable to process issue creation event because target kore device (amrId = {0}) wasn't found", Level.SEVERE),
    EVENT_TITLE_UNKNOWN_INBOUND_DEVICE(703, "EventTitleUnknownInboundDevice", "Unknown inbound device", Level.INFO),
    EVENT_TITLE_UNKNOWN_OUTBOUND_DEVICE(704, "EventTitleUnknownOutboundDevice", "Unknown outbound device", Level.INFO),
    EVENT_TITLE_DEVICE_COMMUNICATION_FAILURE(706, "EventTitleDeviceCommunicationFailure", "Device communication failure", Level.INFO),
    EVENT_TITLE_UNABLE_TO_CONNECT(706, "EventTitleUnableToConnect", "Unable to connect", Level.INFO),
    EVENT_TITLE_CONNECTION_LOST(707, "EventTitleConnectionLost", "Connection lost", Level.INFO),

    // Validation 1101 - 1499
    FIELD_CAN_NOT_BE_EMPTY (1101, Keys.FIELD_CAN_NOT_BE_EMPTY, "Field can't be empty", Level.SEVERE),
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
        return IssueService.COMPONENT_NAME;
    }

    @Override
    public int getNumber() {
        return this.number;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    @Override
    public Level getLevel() {
        return this.level;
    }

    public static class Keys {
        private Keys() {}

        public static final String FIELD_CAN_NOT_BE_EMPTY = "FieldCanNotBeEmpty";
    }

}