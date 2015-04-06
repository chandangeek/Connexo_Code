package com.energyict.mdc.device.lifecycle.config.rest.impl.i18n;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.lifecycle.config.rest.impl.DeviceLifeCycleConfigApplication;

import java.text.MessageFormat;
import java.util.logging.Level;


public enum MessageSeeds implements MessageSeed, TranslationKey {

    DEVICE_LIFECYCLE_NOT_FOUND(1, "device.lifecycle.not.found", "Device lifecycle with id '{0}' doesn't exist", Level.SEVERE),
    DEVICE_LIFECYCLE_STATE_NOT_FOUND(2, "device.lifecycle.state.not.found", "Device lifecycle state with id '{0}' doesn't exist", Level.SEVERE),
    DEVICE_LIFECYCLE_AUTH_ACTION_NOT_FOUND(3, "device.lifecycle.auth.action.not.found", "Authorized action with id '{0}' doesn't exist", Level.SEVERE),
    DEVICE_LIFECYCLE_EVENT_TYPE_NOT_FOUND(4, "device.lifecycle.event.type.not.found", "Event type with symbol '{0}' doesn't exist", Level.SEVERE),

    TRANSITION_COMMISSIONED(1001, "#commissioned", "Commission a device", Level.INFO),
    TRANSITION_ACTIVATED(1002, "#activated", "Activate a device", Level.INFO),
    TRANSITION_DEACTIVATED(1003, "#deactivated", "Deactivate a device", Level.INFO),
    TRANSITION_DECOMMISSIONED(1004, "#decommissioned", "Decommission a device", Level.INFO),
    TRANSITION_DELETED(1005, "#deleted", "Deletion of a device", Level.INFO),
    TRANSITION_COMTASK_CREATED(1006, "com/energyict/mdc/device/data/comtaskexecution/CREATED", "Creation of a communication task on a device", Level.INFO),
    TRANSITION_COMTASK_UPDATED(1007, "com/energyict/mdc/device/data/comtaskexecution/UPDATED", "Change a communication taks of a device", Level.INFO),
    TRANSITION_COMTASK_DELETED(1008, "com/energyict/mdc/device/data/comtaskexecution/DELETED", "Deletion of a communication task on a device", Level.INFO),
    TRANSITION_CONNECTION_TASK_CREATED(1009, "com/energyict/mdc/device/data/connectiontask/CREATED", "Creation of a connection method on a device", Level.INFO),
    TRANSITION_CONNECTION_TASK_UPDATED(1010, "com/energyict/mdc/device/data/connectiontask/UPDATED", "Change of a connection method of a device", Level.INFO),
    TRANSITION_CONNECTION_TASK_DELETED(1011, "com/energyict/mdc/device/data/connectiontask/DELETED", "Deletion of a connection method on a device", Level.INFO),
    TRANSITION_CONNECTION_TASK_SET_AS_DEFAULT(1012, "com/energyict/mdc/device/data/connectiontask/SETASDEFAULT", "Mark a connection method as the default for a device", Level.INFO),
    TRANSITION_CONNECTION_TASK_CLEAR_DEFAULT(1013, "com/energyict/mdc/device/data/connectiontask/CLEARDEFAULT", "Unmark a connection method as the default for a device", Level.INFO),
    TRANSITION_CONNECTION_TASK_FAILURE(1014, "com/energyict/mdc/connectiontask/FAILURE", "Failure to setup a connection with a device", Level.INFO),
    TRANSITION_CONNECTION_TASK_COMPLETION(1015, "com/energyict/mdc/connectiontask/COMPLETION", "Successful completion of a connection with a device", Level.INFO),
    TRANSITION_COMMUNICATION_TOPOLOGY_CHANGED(1016, "com/energyict/mdc/outboundcommunication/DEVICETOPOLOGYCHANGED", "Topology of a gateway device changed", Level.INFO),
    TRANSITION_DEVICE_CREATED(1017, "com/energyict/mdc/device/data/device/CREATED", "Creation of a device", Level.INFO),
    TRANSITION_DEVICE_UPDATED(1018, "com/energyict/mdc/device/data/device/UPDATED", "Change a device", Level.INFO),
    TRANSITION_DEVICE_DELETED(1019, "com/energyict/mdc/device/data/device/DELETED", "Deletion of a device", Level.INFO),
    TRANSITION_DEVICE_MESSAGE_CREATED(1020, "com/energyict/mdc/device/data/deviceMessage/CREATED", "Creation of a command on a device", Level.INFO),
    TRANSITION_DEVICE_MESSAGE_UPDATED(1021, "com/energyict/mdc/device/data/deviceMessage/UPDATED", "Change of a command on a device", Level.INFO),
    TRANSITION_DEVICE_MESSAGE_DELETED(1022, "com/energyict/mdc/device/data/deviceMessage/DELETED", "Deletion of a command on a device", Level.INFO),
    TRANSITION_PROTOCOL_DIALECT_CREATED(1023, "com/energyict/mdc/device/data/protocoldialectproperties/CREATED", "Creation of protocol dialect properties on a device", Level.INFO),
    TRANSITION_PROTOCOL_DIALECT_UPDATED(1024, "com/energyict/mdc/device/data/protocoldialectproperties/UPDATED", "Change of protocol dialect properties on a device", Level.INFO),
    TRANSITION_PROTOCOL_DIALECT_DELETED(1025, "com/energyict/mdc/device/data/protocoldialectproperties/DELETED", "Deletion of protocol dialect properties on a device", Level.INFO),
    TRANSITION_RECYCLED(1026, "#recycled", "Recycle a device", Level.INFO),
    TRANSITION_REVOKED(1027, "#revoked", "Revoke a device", Level.INFO),

    PRIVILEGE_LEVEL_1(2001, Keys.PRIVILEGE_LEVEL_TRANSLATE_KEY + "ONE", "Level 1", Level.INFO),
    PRIVILEGE_LEVEL_2(2002, Keys.PRIVILEGE_LEVEL_TRANSLATE_KEY + "TWO", "Level 2", Level.INFO),
    PRIVILEGE_LEVEL_3(2003, Keys.PRIVILEGE_LEVEL_TRANSLATE_KEY + "THREE", "Level 3", Level.INFO),
    PRIVILEGE_LEVEL_4(2004, Keys.PRIVILEGE_LEVEL_TRANSLATE_KEY + "FOUR", "Level 4", Level.INFO),

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
        return DeviceLifeCycleConfigApplication.DEVICE_CONFIG_LIFECYCLE_COMPONENT;
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

    public String getFormated(Object... args){
        return MessageFormat.format(this.getDefaultFormat(), args);
    }

    public static String getString(MessageSeed messageSeed, Thesaurus thesaurus, Object... args){
        String text = thesaurus.getString(messageSeed.getKey(), messageSeed.getDefaultFormat());
        return MessageFormat.format(text, args);
    }


    public static MessageSeeds getByKey(String key) {
        if (key != null) {
            for (MessageSeeds column : MessageSeeds.values()) {
                if (column.getKey().equals(key)) {
                    return column;
                }
            }
        }
        return null;
    }

    public static class Keys {
        private Keys() {}

        public static final String PRIVILEGE_LEVEL_TRANSLATE_KEY = "privilege.level.";
    }
}
