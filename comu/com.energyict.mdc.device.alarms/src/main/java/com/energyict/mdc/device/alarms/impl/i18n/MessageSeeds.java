package com.energyict.mdc.device.alarms.impl.i18n;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.alarms.DeviceAlarmService;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    EVENT_BAD_DATA_NO_DEVICE(1, "EventBadDataNoDevice", "Unable to process alarm creation event because target device (id = {0}) wasn't found", Level.SEVERE),
    EVENT_BAD_DATA_NO_KORE_DEVICE(2, "EventBadDataNoEndDevice", "Unable to process alarm creation event because target kore device (amrId = {0}) wasn't found", Level.SEVERE),
    EVENT_BAD_DATA_NO_TIMESTAMP(3, "EventBadDataNoTimestamp", "Unable to process alarm creation event because event timestamp cannot be obtained", Level.SEVERE),
    UNABLE_TO_CREATE_EVENT(4, "UnableToCreateEvent", "Unable to create event", Level.SEVERE),
    ACTION_ALARM_WAS_ASSIGNED_USER_AND_WORKGROUP(5, "action.alarm.was.assigned.user.workgorup", "Alarm was assigned to user {0} and workgroup {1}", Level.INFO),
    ACTION_ALARM_WAS_ASSIGNED_USER(6, "action.alarm.was.assigned.user", "Alarm was assigned to user {0}", Level.INFO),
    ACTION_ALARM_WAS_ASSIGNED_WORKGROUP(7, "action.alarm.was.assigned.workgorup", "Alarm was assigned to workgroup {0}", Level.INFO),
    ACTION_ALARM_WAS_UNASSIGNED(8, "action.alarm.was.unassigned", "Alarm was unassigned", Level.INFO)
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
        return DeviceAlarmService.COMPONENT_NAME;
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
}