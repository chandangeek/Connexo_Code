package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.TransactionRequired;
import com.energyict.mdc.device.config.DeviceConfigurationService;

/**
 * Models the different event types that are produced by this "device type and configurations bundle".
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (15:44)
 */
public enum EventType {

    DEVICETYPE_CREATED("devicetype/CREATED"),
    DEVICETYPE_UPDATED("devicetype/UPDATED"),
    DEVICETYPE_DELETED("devicetype/DELETED"),
    DEVICECONFIGURATION_CREATED("deviceconfiguration/CREATED"),
    DEVICECONFIGURATION_UPDATED("deviceconfiguration/UPDATED"),
    DEVICECONFIGURATION_DELETED("deviceconfiguration/DELETED"),
    DEVICECONFIGURATION_VALIDATEDEACTIVATE("deviceconfiguration/VALIDATEDEACTIVATE"),
    COMTASKENABLEMENT_CREATED("comtaskenablement/CREATED"),
    COMTASKENABLEMENT_UPDATED("comtaskenablement/UPDATED"),
    COMTASKENABLEMENT_DELETED("comtaskenablement/DELETED"),
    COMTASKENABLEMENT_VALIDATEDELETE("comtaskenablement/VALIDATEDELETE"),
    COMTASKENABLEMENT_SUSPEND("comtaskenablement/SUSPEND"),
    COMTASKENABLEMENT_RESUME("comtaskenablement/RESUME"),
    COMTASKENABLEMENT_PRIORITY_UPDATED("comtaskenablement/PRIORITY_UPDATED") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            eventTypeBuilder.withProperty("comTaskEnablementId", ValueType.LONG, "comTaskEnablementId");
            eventTypeBuilder.withProperty("oldPriority", ValueType.INTEGER, "oldPriority");
            eventTypeBuilder.withProperty("newPriority", ValueType.INTEGER, "newPriority");
            return eventTypeBuilder;
        }
    },
    COMTASKENABLEMENT_SWITCH_ON_DEFAULT("comtaskenablement/SWITCH_ON_DEFAULT") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            eventTypeBuilder.withProperty("comTaskEnablementId", ValueType.LONG, "comTaskEnablementId");
            return eventTypeBuilder;
        }
    },
    COMTASKENABLEMENT_SWITCH_OFF_DEFAULT("comtaskenablement/SWITCH_OFF_DEFAULT") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            eventTypeBuilder.withProperty("comTaskEnablementId", ValueType.LONG, "comTaskEnablementId");
            return eventTypeBuilder;
        }
    },
    COMTASKENABLEMENT_SWITCH_FROM_DEFAULT_TO_TASK("comtaskenablement/SWITCH_FROM_DEFAULT_TO_TASK") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            eventTypeBuilder.withProperty("comTaskEnablementId", ValueType.LONG, "comTaskEnablementId");
            eventTypeBuilder.withProperty("partialConnectionTaskId", ValueType.LONG, "partialConnectionTaskId");
            return eventTypeBuilder;
        }
    },
    COMTASKENABLEMENT_SWITCH_FROM_TASK_TO_DEFAULT("comtaskenablement/SWITCH_FROM_TASK_TO_DEFAULT") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            eventTypeBuilder.withProperty("comTaskEnablementId", ValueType.LONG, "comTaskEnablementId");
            eventTypeBuilder.withProperty("partialConnectionTaskId", ValueType.LONG, "partialConnectionTaskId");
            return eventTypeBuilder;
        }
    },
    COMTASKENABLEMENT_SWITCH_BETWEEN_TASKS("comtaskenablement/SWITCH_BETWEEN_TASKS") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            eventTypeBuilder.withProperty("comTaskEnablementId", ValueType.LONG, "comTaskEnablementId");
            eventTypeBuilder.withProperty("oldPartialConnectionTaskId", ValueType.LONG, "oldPartialConnectionTaskId");
            eventTypeBuilder.withProperty("newPartialConnectionTaskId", ValueType.LONG, "newPartialConnectionTaskId");
            return eventTypeBuilder;
        }
    },
    COMTASKENABLEMENT_START_USING_TASK("comtaskenablement/USE_TASK") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            eventTypeBuilder.withProperty("comTaskEnablementId", ValueType.LONG, "comTaskEnablementId");
            return eventTypeBuilder;
        }
    },
    COMTASKENABLEMENT_REMOVE_TASK("comtaskenablement/REMOVE_TASK") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            eventTypeBuilder.withProperty("partialConnectionTaskId", ValueType.LONG, "partialConnectionTaskId");
            return eventTypeBuilder;
        }
    },
    LOGBOOKSPEC_CREATED("logbookspec/CREATED"),
    LOGBOOKSPEC_UPDATED("logbookspec/UPDATED"),
    LOGBOOKSPEC_DELETED("logbookspec/DELETED"),
    LOADPROFILESPEC_CREATED("loadprofilespec/CREATED"),
    LOADPROFILESPEC_UPDATED("loadprofilespec/UPDATED"),
    LOADPROFILESPEC_DELETED("loadprofilespec/DELETED"),
    CHANNELSPEC_CREATED("channelspec/CREATED"),
    CHANNELSPEC_UPDATED("channelspec/UPDATED"),
    CHANNELSPEC_DELETED("channelspec/DELETED"),
    REGISTERSPEC_CREATED("registerspec/CREATED"),
    REGISTERSPEC_UPDATED("registerspec/UPDATED"),
    REGISTERSPEC_DELETED("registerspec/DELETED"),
    PROTOCOLCONFIGURATIONPROPS_CREATED("protocolconfigurationprops/CREATED"),
    PROTOCOLCONFIGURATIONPROPS_UPDATED("protocolconfigurationprops/UPDATED"),
    PROTOCOLCONFIGURATIONPROPS_DELETED("protocolconfigurationprops/DELETED"),
    PROTOCOLCONFIGURATIONPROPS_VALIDATEDELETE("protocolconfigurationprops/VALIDATEDELETE"),
    PARTIAL_INBOUND_CONNECTION_TASK_CREATED("partialinboundconnectiontask/CREATED"),
    PARTIAL_INBOUND_CONNECTION_TASK_UPDATED("partialinboundconnectiontask/UPDATED"),
    PARTIAL_INBOUND_CONNECTION_TASK_VALIDATE_DELETE("partialinboundconnectiontask/VALIDATE_DELETE"),
    PARTIAL_INBOUND_CONNECTION_TASK_DELETED("partialinboundconnectiontask/DELETED"),
    PARTIAL_SCHEDULED_CONNECTION_TASK_CREATED("partialscheduledconnectiontask/CREATED"),
    PARTIAL_SCHEDULED_CONNECTION_TASK_UPDATED("partialscheduledconnectiontask/UPDATED"),
    PARTIAL_SCHEDULED_CONNECTION_TASK_VALIDATE_DELETE("partialscheduledconnectiontask/VALIDATE_DELETE"),
    PARTIAL_SCHEDULED_CONNECTION_TASK_DELETED("partialscheduledconnectiontask/DELETED"),
    PARTIAL_CONNECTION_INITIATION_TASK_CREATED("partialconnectioninitiationtask/CREATED"),
    PARTIAL_CONNECTION_INITIATION_TASK_UPDATED("partialconnectioninitiationtask/UPDATED"),
    PARTIAL_CONNECTION_INITIATION_TASK_DELETED("partialconnectioninitiationtask/DELETED"),
    PARTIAL_CONNECTION_INITIATION_TASK_VALIDATE_DELETE("partialconnectioninitiationtask/VALIDATE_DELETE"),
    DEVICE_COMMUNICATION_CONFIGURATION_CREATED("devicecommunicationconfiguration/CREATED"),
    DEVICE_COMMUNICATION_CONFIGURATION_UPDATED("devicecommunicationconfiguration/UPDATED"),
    DEVICE_COMMUNICATION_CONFIGURATION_DELETED("devicecommunicationconfiguration/DELETED"),
    SECURITY_PROPERTY_SET_CREATED("securitypropertyset/CREATED"),
    SECURITY_PROPERTY_SET_UPDATED("securitypropertyset/UPDATED"),
    SECURITY_PROPERTY_SET_DELETED("securitypropertyset/DELETED");

    private static final String NAMESPACE = "com/energyict/mdc/device/config/";
    private final String topic;

    EventType(String topic) {
        this.topic = topic;
    }

    public String topic() {
        return NAMESPACE + topic;
    }

    @TransactionRequired
    void install(EventService eventService) {
        EventTypeBuilder builder = eventService.buildEventTypeWithTopic(topic())
                .name(name())
                .component(DeviceConfigurationService.COMPONENTNAME)
                .category("Crud")
                .scope("System")
                .shouldPublish();
        this.addCustomProperties(builder).create().save();
    }

    protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
        eventTypeBuilder.withProperty("id", ValueType.LONG, "id");
        return eventTypeBuilder;
    }

}