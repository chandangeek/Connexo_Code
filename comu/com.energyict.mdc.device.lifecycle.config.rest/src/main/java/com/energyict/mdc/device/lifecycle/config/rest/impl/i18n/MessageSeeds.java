package com.energyict.mdc.device.lifecycle.config.rest.impl.i18n;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.config.MicroCategory;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.config.rest.impl.DeviceLifeCycleConfigApplication;
import com.energyict.mdc.device.lifecycle.config.rest.info.MicroActionAndCheckInfoFactory;

import java.text.MessageFormat;
import java.util.logging.Level;


public enum MessageSeeds implements MessageSeed, TranslationKey {

    DEVICE_LIFECYCLE_NOT_FOUND(1, "device.lifecycle.not.found", "Device lifecycle with id '{0}' doesn't exist", Level.SEVERE),
    DEVICE_LIFECYCLE_STATE_NOT_FOUND(2, "device.lifecycle.state.not.found", "Device lifecycle state with id '{0}' doesn't exist", Level.SEVERE),
    DEVICE_LIFECYCLE_AUTH_ACTION_NOT_FOUND(3, "device.lifecycle.auth.action.not.found", "Authorized action with id '{0}' doesn't exist", Level.SEVERE),
    DEVICE_LIFECYCLE_EVENT_TYPE_NOT_FOUND(4, "device.lifecycle.event.type.not.found", "Event type with symbol '{0}' doesn't exist", Level.SEVERE),
    FIELD_CAN_NOT_BE_EMPTY(5, "field.cn.not.be.empty", "This field is required", Level.SEVERE),
    DEVICE_LIFECYCLE_STATE_IS_STILL_USED_BY_TRANSITIONS(6, "unable.to.remove.state.with.transitions", "This state cannot be removed from this device life cycle because it is used on transitions: {0}", Level.SEVERE),
    DEVICE_LIFECYCLE_STATE_IS_THE_LATEST_STATE(7, "unable.to.remove.latest.state", "This state cannot be removed from this device life cycle because it's the latest state. Add another state first.", Level.SEVERE),
    DEVICE_LIFECYCLE_STATE_IS_THE_INITIAL_STATE(8, "unable.to.remove.initial.state", "This state cannot be removed from this device life cycle because it's the initial state. Set another state as initial state first", Level.SEVERE),
    DEVICE_LIFECYCLE_IS_USED_BY_DEVICE_TYPE(9, "device.lifecycle.is.used.by.device.type", "This operation cannot be performed for this device life cycle because one or more devices types use this device life cycle.", Level.SEVERE),

    TRANSITION_COMMISSIONING(1001, "#commissioning", "Commission a device", Level.INFO),
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

    MICRO_ACTION_NAME_SET_LAST_READING(3001, Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.SET_LAST_READING, "Input last meter reading", Level.INFO),
    MICRO_ACTION_NAME_ENABLE_VALIDATION(3002, Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.ENABLE_VALIDATION, "Activate validation", Level.INFO),
    MICRO_ACTION_NAME_DISABLE_VALIDATION(3003, Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.DISABLE_VALIDATION, "Deactivate validation", Level.INFO),
    MICRO_ACTION_NAME_ACTIVATE_CONNECTION_TASKS(3004, Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.ACTIVATE_CONNECTION_TASKS, "Activate connections in use", Level.INFO),
    MICRO_ACTION_NAME_START_COMMUNICATION(3005, Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.START_COMMUNICATION, "Trigger all communication", Level.INFO),
    MICRO_ACTION_NAME_DISABLE_COMMUNICATION(3006, Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.DISABLE_COMMUNICATION, "Deactivate communication", Level.INFO),
    MICRO_ACTION_NAME_CREATE_METER_ACTIVATION(3007, Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.CREATE_METER_ACTIVATION, "Create new meter activation", Level.INFO),
    MICRO_ACTION_NAME_CLOSE_METER_ACTIVATION(3008, Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.CLOSE_METER_ACTIVATION, "Stop meter activation", Level.INFO),
    MICRO_ACTION_NAME_REMOVE_DEVICE_FROM_STATIC_GROUPS(3009, Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.REMOVE_DEVICE_FROM_STATIC_GROUPS, "Remove device from static groups", Level.INFO),
    MICRO_ACTION_NAME_DETACH_SLAVE_FROM_MASTER(3010, Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.DETACH_SLAVE_FROM_MASTER, "Disconnect slave from master", Level.INFO),

    MICRO_ACTION_DESCRIPTION_SET_LAST_READING(4001, Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.SET_LAST_READING, "Provide the last meter reading manually based on one or more register group.", Level.INFO),
    MICRO_ACTION_DESCRIPTION_ENABLE_VALIDATION(4002, Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.ENABLE_VALIDATION, "Activate the data validation on this device. This auto action is effective immediately.", Level.INFO),
    MICRO_ACTION_DESCRIPTION_DISABLE_VALIDATION(4003, Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.DISABLE_VALIDATION, "Stop the data validation on this device.", Level.INFO),
    MICRO_ACTION_DESCRIPTION_ACTIVATE_CONNECTION_TASKS(4004, Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.ACTIVATE_CONNECTION_TASKS, "Activate inactive connections used in scheduled communication tasks.", Level.INFO),
    MICRO_ACTION_DESCRIPTION_START_COMMUNICATION(4005, Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.START_COMMUNICATION, "Trigger all the recurring and non-recurring communication tasks and their connections on the date of the transition.", Level.INFO),
    MICRO_ACTION_DESCRIPTION_DISABLE_COMMUNICATION(4006, Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.DISABLE_COMMUNICATION, "Deactivate the connections and communication tasks on this device.", Level.INFO),
    MICRO_ACTION_DESCRIPTION_CREATE_METER_ACTIVATION(4007, Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.CREATE_METER_ACTIVATION, "Create a new meter activation on the transition date.", Level.INFO),
    MICRO_ACTION_DESCRIPTION_CLOSE_METER_ACTIVATION(4008, Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.CLOSE_METER_ACTIVATION, "Stop the meter activation of this device and unlink the device from its usage point.", Level.INFO),
    MICRO_ACTION_DESCRIPTION_REMOVE_DEVICE_FROM_STATIC_GROUPS(4009, Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.REMOVE_DEVICE_FROM_STATIC_GROUPS, "Remove this device from the static device groups.", Level.INFO),
    MICRO_ACTION_DESCRIPTION_DETACH_SLAVE_FROM_MASTER(4010, Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.DETACH_SLAVE_FROM_MASTER, "If this device is a slave, disconnect it from his master.", Level.INFO),

    TRANSITION_ACTION_CHECK_CATEGORY_COMMUNICATION(5001, Keys.TRANSITION_ACTION_CHECK_CATEGORY_KEY + MicroCategory.COMMUNICATION, "Communication", Level.INFO),
    TRANSITION_ACTION_CHECK_CATEGORY_VALIDATION_AND_ESTIMATION(5002, Keys.TRANSITION_ACTION_CHECK_CATEGORY_KEY + MicroCategory.VALIDATION_AND_ESTIMATION, "Validation and estimation", Level.INFO),
    TRANSITION_ACTION_CHECK_CATEGORY_DATA_COLLECTION(5003, Keys.TRANSITION_ACTION_CHECK_CATEGORY_KEY + MicroCategory.DATA_COLLECTION, "Data collection", Level.INFO),
    TRANSITION_ACTION_CHECK_CATEGORY_TOPOLOGY(5004, Keys.TRANSITION_ACTION_CHECK_CATEGORY_KEY + MicroCategory.TOPOLOGY, "Topology", Level.INFO),
    TRANSITION_ACTION_SUB_CATEGORY_VALIDATION(5005, "subcategory_validation", "Toggle data validation", Level.INFO),
    TRANSITION_ACTION_SUB_CATEGORY_COMMUNICATION(5006, "subcategory_communication", "Toggle communication", Level.INFO),
    TRANSITION_ACTION_CHECK_CATEGORY_ISSUES_AND_ALARMS(5007, Keys.TRANSITION_ACTION_CHECK_CATEGORY_KEY + MicroCategory.ISSUES_AND_ALARMS, "Issues and alarms", Level.INFO),
    TRANSITION_ACTION_CHECK_CATEGORY_VALIDATION(5008, Keys.TRANSITION_ACTION_CHECK_CATEGORY_KEY + MicroCategory.INSTALLATION, "Installation", Level.INFO),
    TRANSITION_ACTION_CHECK_SUB_CATEGORY_COMMUNICATION(5009, Keys.TRANSITION_ACTION_SUB_CATEGORY_DESCRIPTION_KEY + "subcategory_validation", "Activate or deactivate the data validation on this device. This auto action is effective immediately.", Level.INFO),
    TRANSITION_ACTION_CHECK_SUB_CATEGORY_INSTALLATION(5010, Keys.TRANSITION_ACTION_SUB_CATEGORY_DESCRIPTION_KEY + "subcategory_communication", "Activate or deactivate the connections and (recurring) communication tasks on this device.", Level.INFO),
    TRANSITION_ACTION_SUB_CATEGORY_ESTIMATION(5011, "subcategory_estimation", "Toggle data estimation", Level.INFO),
    TRANSITION_ACTION_CHECK_SUB_CATEGORY_ESTIMATION(5010, Keys.TRANSITION_ACTION_SUB_CATEGORY_DESCRIPTION_KEY + "subcategory_estimation", "Activate or deactivate the data estimation on this device. This auto action is effective immediately.", Level.INFO),

    MICRO_CHECK_NAME_DEFAULT_CONNECTION_AVAILABLE(6001, Keys.MICRO_CHECK_NAME_TRANSLATE_KEY + MicroCheck.DEFAULT_CONNECTION_AVAILABLE, "Default connection available", Level.INFO),
    MICRO_CHECK_NAME_AT_LEAST_ONE_MANUALLY_SCHEDULED_COMMUNICATION_TASK_AVAILABLE(6002, Keys.MICRO_CHECK_NAME_TRANSLATE_KEY + MicroCheck.AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE, "At least one scheduled communication task", Level.INFO),
    MICRO_CHECK_NAME_AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE(6003, Keys.MICRO_CHECK_NAME_TRANSLATE_KEY + MicroCheck.AT_LEAST_ONE_SHARED_SCHEDULED_COMMUNICATION_TASK_AVAILABLE, "At least one shared communication schedule", Level.INFO),
    MICRO_CHECK_NAME_ALL_DATA_COLLECTED(6004, Keys.MICRO_CHECK_NAME_TRANSLATE_KEY + MicroCheck.ALL_DATA_COLLECTED, "All data collected", Level.INFO),
    MICRO_CHECK_NAME_ALL_DATA_VALID(6005, Keys.MICRO_CHECK_NAME_TRANSLATE_KEY + MicroCheck.ALL_DATA_VALID, "All data valid", Level.INFO),
    MICRO_CHECK_NAME_SLAVE_DEVICE_HAS_GATEWAY(6006, Keys.MICRO_CHECK_NAME_TRANSLATE_KEY + MicroCheck.SLAVE_DEVICE_HAS_GATEWAY, "Check for master device", Level.INFO),
    MICRO_CHECK_NAME_LINKED_WITH_USAGE_POINT(6007, Keys.MICRO_CHECK_NAME_TRANSLATE_KEY + MicroCheck.LINKED_WITH_USAGE_POINT, "Connected to usage point", Level.INFO),
    MICRO_CHECK_NAME_ALL_ISSUES_AND_ALARMS_ARE_CLOSED(6008, Keys.MICRO_CHECK_NAME_TRANSLATE_KEY + MicroCheck.ALL_ISSUES_AND_ALARMS_ARE_CLOSED, "All issues closed",Level.INFO),
    MICRO_CHECK_NAME_MANDATORY_COMMUNICATION_ATTRIBUTES_AVAILABLE(6009, Keys.MICRO_CHECK_NAME_TRANSLATE_KEY + MicroActionAndCheckInfoFactory.CONSOLIDATED_MICRO_CHECKS_KEY, "Mandatory communication attributes available", Level.INFO),

    MICRO_CHECK_DESCRIPTION_DEFAULT_CONNECTION_AVAILABLE(7001, Keys.MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY + MicroCheck.DEFAULT_CONNECTION_AVAILABLE, "Check if a default connection is available on the device.", Level.INFO),
    MICRO_CHECK_DESCRIPTION_AT_LEAST_ONE_MANUALLY_SCHEDULED_COMMUNICATION_TASK_AVAILABLE(7002, Keys.MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY + MicroCheck.AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE, "Check if at least one communication task has been scheduled.", Level.INFO),
    MICRO_CHECK_DESCRIPTION_AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE(7003, Keys.MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY + MicroCheck.AT_LEAST_ONE_SHARED_SCHEDULED_COMMUNICATION_TASK_AVAILABLE, "Check if at least one shared communication schedule has been added to the device.", Level.INFO),
    MICRO_CHECK_DESCRIPTION_ALL_DATA_COLLECTED(7004, Keys.MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY + MicroCheck.ALL_DATA_COLLECTED, "Check if all the data of this device has been collected.", Level.INFO),
    MICRO_CHECK_DESCRIPTION_ALL_DATA_VALID(7005, Keys.MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY + MicroCheck.ALL_DATA_VALID, "Check if all the collected data is valid.", Level.INFO),
    MICRO_CHECK_DESCRIPTION_SLAVE_DEVICE_HAS_GATEWAY(7006, Keys.MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY + MicroCheck.SLAVE_DEVICE_HAS_GATEWAY, "If this device is a slave, check if the device has been linked to a master device.", Level.INFO),
    MICRO_CHECK_DESCRIPTION_LINKED_WITH_USAGE_POINT(7007, Keys.MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY + MicroCheck.LINKED_WITH_USAGE_POINT, "Check if this device is connected to a usage point.", Level.INFO),
    MICRO_CHECK_DESCRIPTION_ALL_ISSUES_AND_ALARMS_ARE_CLOSED(7008, Keys.MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY + MicroCheck.ALL_ISSUES_AND_ALARMS_ARE_CLOSED, "Check if all the issues on this device are closed.", Level.INFO),
    MICRO_CHECK_DESCRIPTION_MANDATORY_COMMUNICATION_ATTRIBUTES_AVAILABLE(7009, Keys.MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY + MicroActionAndCheckInfoFactory.CONSOLIDATED_MICRO_CHECKS_KEY, "Check if the mandatory communication attributes are available on the device: protocol dialect attributes, security setting attributes, connection attributes, general attributes.", Level.INFO),
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

    public static String getString(MessageSeed messageSeed, Thesaurus thesaurus, Object... args) {
        String text = thesaurus.getString(messageSeed.getKey(), messageSeed.getDefaultFormat());
        return MessageFormat.format(text, args);
    }

    public static class Keys {
        private Keys() {
        }

        public static final String PRIVILEGE_LEVEL_TRANSLATE_KEY = "privilege.level.";
        public static final String MICRO_ACTION_NAME_TRANSLATE_KEY = "transition.microaction.name.";
        public static final String MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY = "transition.microaction.description.";
        public static final String TRANSITION_ACTION_CHECK_CATEGORY_KEY = "transition.category.";
        public static final String MICRO_CHECK_NAME_TRANSLATE_KEY = "transition.microcheck.name.";
        public static final String MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY = "transition.microcheck.description.";
        public static final String TRANSITION_ACTION_SUB_CATEGORY_DESCRIPTION_KEY = "transition.microaction.conflict.description.";
    }
}
