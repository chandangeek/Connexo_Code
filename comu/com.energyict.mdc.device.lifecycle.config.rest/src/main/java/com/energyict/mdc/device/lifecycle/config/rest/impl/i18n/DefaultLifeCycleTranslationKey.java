package com.energyict.mdc.device.lifecycle.config.rest.impl.i18n;

import com.elster.jupiter.nls.TranslationKey;

import com.energyict.mdc.device.lifecycle.config.DefaultCustomStateTransitionEventType;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

/**
 * Copyrights EnergyICT
 * Date: 4/06/2015
 * Time: 15:41
 */
public enum DefaultLifeCycleTranslationKey implements TranslationKey {
    IN_STOCK(DefaultState.IN_STOCK.getKey(), "In stock"),
    COMMISSIONING(DefaultState.COMMISSIONING.getKey(), "Commissioning"),
    ACTIVE(DefaultState.ACTIVE.getKey(), "Active"),
    INACTIVE(DefaultState.INACTIVE.getKey(), "Inactive"),
    DECOMMISSIONED(DefaultState.DECOMMISSIONED.getKey(), "Decommissioned"),
    REMOVED(DefaultState.REMOVED.getKey(), "Removed"),
    STATE_TRANSITION_EVENT_TYPE_START_COMMISSIONING(DefaultCustomStateTransitionEventType.COMMISSIONING.getSymbol(), "Start commissioning"),
    STATE_TRANSITION_EVENT_TYPE_ACTIVATE(DefaultCustomStateTransitionEventType.ACTIVATED.getSymbol(), "Activate"),
    STATE_TRANSITION_EVENT_TYPE_DEACTIVATE(DefaultCustomStateTransitionEventType.DEACTIVATED.getSymbol(), "Deactivate"),
    STATE_TRANSITION_EVENT_TYPE_DECOMMISSION(DefaultCustomStateTransitionEventType.DECOMMISSIONED.getSymbol(), "Decommission"),
    STATE_TRANSITION_EVENT_TYPE_REMOVE(DefaultCustomStateTransitionEventType.DELETED.getSymbol(), "Remove"),

    TRANSITION_COMTASK_CREATED("com/energyict/mdc/device/data/comtaskexecution/CREATED", "Creation of a communication task on a device"),
    TRANSITION_COMTASK_UPDATED("com/energyict/mdc/device/data/comtaskexecution/UPDATED", "Change a communication taks of a device"),
    TRANSITION_COMTASK_DELETED("com/energyict/mdc/device/data/comtaskexecution/DELETED", "Deletion of a communication task on a device"),
    TRANSITION_CONNECTION_TASK_CREATED("com/energyict/mdc/device/data/connectiontask/CREATED", "Creation of a connection method on a device"),
    TRANSITION_CONNECTION_TASK_UPDATED("com/energyict/mdc/device/data/connectiontask/UPDATED", "Change of a connection method of a device"),
    TRANSITION_CONNECTION_TASK_DELETED("com/energyict/mdc/device/data/connectiontask/DELETED", "Deletion of a connection method on a device"),
    TRANSITION_CONNECTION_TASK_SET_AS_DEFAULT("com/energyict/mdc/device/data/connectiontask/SETASDEFAULT", "Mark a connection method as the default for a device"),
    TRANSITION_CONNECTION_TASK_CLEAR_DEFAULT("com/energyict/mdc/device/data/connectiontask/CLEARDEFAULT", "Unmark a connection method as the default for a device"),
    TRANSITION_CONNECTION_TASK_FAILURE("com/energyict/mdc/connectiontask/FAILURE", "Failure to setup a connection with a device"),
    TRANSITION_CONNECTION_TASK_COMPLETION("com/energyict/mdc/connectiontask/COMPLETION", "Successful completion of a connection with a device"),
    TRANSITION_COMMUNICATION_TOPOLOGY_CHANGED("com/energyict/mdc/outboundcommunication/DEVICETOPOLOGYCHANGED", "Topology of a gateway device changed"),
    TRANSITION_DEVICE_CREATED("com/energyict/mdc/device/data/device/CREATED", "Creation of a device"),
    TRANSITION_DEVICE_UPDATED("com/energyict/mdc/device/data/device/UPDATED", "Change a device"),
    TRANSITION_DEVICE_DELETED("com/energyict/mdc/device/data/device/DELETED", "Deletion of a device"),
    TRANSITION_DEVICE_MESSAGE_CREATED("com/energyict/mdc/device/data/deviceMessage/CREATED", "Creation of a command on a device"),
    TRANSITION_DEVICE_MESSAGE_UPDATED("com/energyict/mdc/device/data/deviceMessage/UPDATED", "Change of a command on a device"),
    TRANSITION_DEVICE_MESSAGE_DELETED("com/energyict/mdc/device/data/deviceMessage/DELETED", "Deletion of a command on a device"),
    TRANSITION_PROTOCOL_DIALECT_CREATED("com/energyict/mdc/device/data/protocoldialectproperties/CREATED", "Creation of protocol dialect properties on a device"),
    TRANSITION_PROTOCOL_DIALECT_UPDATED("com/energyict/mdc/device/data/protocoldialectproperties/UPDATED", "Change of protocol dialect properties on a device"),
    TRANSITION_PROTOCOL_DIALECT_DELETED("com/energyict/mdc/device/data/protocoldialectproperties/DELETED", "Deletion of protocol dialect properties on a device"),

    PRIVILEGE_LEVEL_1("privilege.level.ONE", "Level 1"),
    PRIVILEGE_LEVEL_2("privilege.level.TWO", "Level 2"),
    PRIVILEGE_LEVEL_3("privilege.level.THREE", "Level 3"),
    PRIVILEGE_LEVEL_4("privilege.level.FOUR", "Level 4");

    private final String key;
    private final String defaultFormat;

    DefaultLifeCycleTranslationKey(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

}
