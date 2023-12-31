/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.config;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.TransactionRequired;

/**
 * Models the different event types that are produced by this "device type and configurations bundle".
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (15:44)
 */
public enum EventType {

    DEVICETYPE_CREATED("devicetype/CREATED") {
        @Override
        EventTypeBuilder shouldPublish(EventTypeBuilder eventTypeBuilder) {
            return eventTypeBuilder.shouldPublish();
        }
    },
    DEVICETYPE_UPDATED("devicetype/UPDATED") {
        @Override
        EventTypeBuilder shouldPublish(EventTypeBuilder eventTypeBuilder) {
            return eventTypeBuilder.shouldPublish();
        }
    },
    DEVICETYPE_VALIDATE_DELETE("devicetype/VALIDATEDELETE"),
    DEVICETYPE_DELETED("devicetype/DELETED") {
        @Override
        EventTypeBuilder shouldPublish(EventTypeBuilder eventTypeBuilder) {
            return eventTypeBuilder.shouldPublish();
        }
    },
    DEVICELIFECYCLE_UPDATED("devicetype/dlc/UPDATED") {
        @Override
        EventTypeBuilder shouldPublish(EventTypeBuilder eventTypeBuilder) {
            return eventTypeBuilder.shouldPublish();
        }
    },
    DEVICECONFIGURATION_CREATED("deviceconfiguration/CREATED"),
    DEVICECONFIGURATION_UPDATED("deviceconfiguration/UPDATED"),
    DEVICECONFIGURATION_DELETED("deviceconfiguration/DELETED"),
    DEVICECONFIGURATION_ACTIVATED("deviceconfiguration/ACTIVATED"),
    DEVICECONFIGURATION_DEACTIVATED("deviceconfiguration/DEACTIVATED"),
    DEVICECONFIGURATION_VALIDATEDEACTIVATE("deviceconfiguration/VALIDATEDEACTIVATE"),
    COMTASKENABLEMENT_CREATED("comtaskenablement/CREATED"),
    COMTASKENABLEMENT_UPDATED("comtaskenablement/UPDATED"),
    COMTASKENABLEMENT_DELETED("comtaskenablement/DELETED"),
    COMTASKENABLEMENT_VALIDATEDELETE("comtaskenablement/VALIDATEDELETE"),
    COMTASKENABLEMENT_SUSPEND("comtaskenablement/SUSPEND") {
        @Override
        EventTypeBuilder shouldPublish(EventTypeBuilder eventTypeBuilder) {
            return eventTypeBuilder.shouldPublish();
        }
    },
    COMTASKENABLEMENT_RESUME("comtaskenablement/RESUME") {
        @Override
        EventTypeBuilder shouldPublish(EventTypeBuilder eventTypeBuilder) {
            return eventTypeBuilder.shouldPublish();
        }
    },
    COMTASKENABLEMENT_PRIORITY_UPDATED("comtaskenablement/PRIORITY_UPDATED") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            eventTypeBuilder.withProperty("comTaskEnablementId", ValueType.LONG, "comTaskEnablementId");
            eventTypeBuilder.withProperty("oldPriority", ValueType.INTEGER, "oldPriority");
            eventTypeBuilder.withProperty("newPriority", ValueType.INTEGER, "newPriority");
            return eventTypeBuilder;
        }

        @Override
        EventTypeBuilder shouldPublish(EventTypeBuilder eventTypeBuilder) {
            return eventTypeBuilder.shouldPublish();
        }
    },
    COMTASKENABLEMENT_SWITCH_ON_DEFAULT("comtaskenablement/SWITCH_ON_DEFAULT") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            eventTypeBuilder.withProperty("comTaskEnablementId", ValueType.LONG, "comTaskEnablementId");
            return eventTypeBuilder;
        }

        @Override
        EventTypeBuilder shouldPublish(EventTypeBuilder eventTypeBuilder) {
            return eventTypeBuilder.shouldPublish();
        }
    },
    COMTASKENABLEMENT_SWITCH_ON_CONNECTION_FUNCTION("comtaskenablement/SWITCH_ON_CF") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            eventTypeBuilder.withProperty("comTaskEnablementId", ValueType.LONG, "comTaskEnablementId");
            eventTypeBuilder.withProperty("newConnectionFunctionId", ValueType.LONG, "newConnectionFunctionId");
            return eventTypeBuilder;
        }

        @Override
        EventTypeBuilder shouldPublish(EventTypeBuilder eventTypeBuilder) {
            return eventTypeBuilder.shouldPublish();
        }
    },
    COMTASKENABLEMENT_SWITCH_OFF_DEFAULT("comtaskenablement/SWITCH_OFF_DEFAULT") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            eventTypeBuilder.withProperty("comTaskEnablementId", ValueType.LONG, "comTaskEnablementId");
            return eventTypeBuilder;
        }

        @Override
        EventTypeBuilder shouldPublish(EventTypeBuilder eventTypeBuilder) {
            return eventTypeBuilder.shouldPublish();
        }
    },
    COMTASKENABLEMENT_SWITCH_OFF_CONNECTION_FUNCTION("comtaskenablement/SWITCH_OFF_CF") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            eventTypeBuilder.withProperty("comTaskEnablementId", ValueType.LONG, "comTaskEnablementId");
            eventTypeBuilder.withProperty("oldConnectionFunctionId", ValueType.LONG, "oldConnectionFunctionId");
            return eventTypeBuilder;
        }

        @Override
        EventTypeBuilder shouldPublish(EventTypeBuilder eventTypeBuilder) {
            return eventTypeBuilder.shouldPublish();
        }
    },
    COMTASKENABLEMENT_SWITCH_FROM_DEFAULT_TO_TASK("comtaskenablement/SWITCH_FROM_DEFAULT_TO_TASK") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            eventTypeBuilder.withProperty("comTaskEnablementId", ValueType.LONG, "comTaskEnablementId");
            eventTypeBuilder.withProperty("partialConnectionTaskId", ValueType.LONG, "partialConnectionTaskId");
            return eventTypeBuilder;
        }

        @Override
        EventTypeBuilder shouldPublish(EventTypeBuilder eventTypeBuilder) {
            return eventTypeBuilder.shouldPublish();
        }
    },
    COMTASKENABLEMENT_SWITCH_FROM_DEFAULT_TO_CONNECTION_FUNCTION("comtaskenablement/SWITCH_FROM_DEFAULT_TO_CF") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            eventTypeBuilder.withProperty("comTaskEnablementId", ValueType.LONG, "comTaskEnablementId");
            eventTypeBuilder.withProperty("newConnectionFunctionId", ValueType.LONG, "newConnectionFunctionId");
            return eventTypeBuilder;
        }

        @Override
        EventTypeBuilder shouldPublish(EventTypeBuilder eventTypeBuilder) {
            return eventTypeBuilder.shouldPublish();
        }
    },
    COMTASKENABLEMENT_SWITCH_FROM_TASK_TO_DEFAULT("comtaskenablement/SWITCH_FROM_TASK_TO_DEFAULT") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            eventTypeBuilder.withProperty("comTaskEnablementId", ValueType.LONG, "comTaskEnablementId");
            eventTypeBuilder.withProperty("partialConnectionTaskId", ValueType.LONG, "partialConnectionTaskId");
            return eventTypeBuilder;
        }

        @Override
        EventTypeBuilder shouldPublish(EventTypeBuilder eventTypeBuilder) {
            return eventTypeBuilder.shouldPublish();
        }
    },
    COMTASKENABLEMENT_SWITCH_FROM_TASK_TO_CONNECTION_FUNCTION("comtaskenablement/SWITCH_FROM_TASK_TO_CF") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            eventTypeBuilder.withProperty("comTaskEnablementId", ValueType.LONG, "comTaskEnablementId");
            eventTypeBuilder.withProperty("oldPartialConnectionTaskId", ValueType.LONG, "oldPartialConnectionTaskId");
            eventTypeBuilder.withProperty("newConnectionFunctionId", ValueType.LONG, "newConnectionFunctionId");
            return eventTypeBuilder;
        }

        @Override
        EventTypeBuilder shouldPublish(EventTypeBuilder eventTypeBuilder) {
            return eventTypeBuilder.shouldPublish();
        }
    },
    COMTASKENABLEMENT_SWITCH_FROM_CONNECTION_FUNCTION_TO_DEFAULT("comtaskenablement/SWITCH_FROM_CF_TO_DEFAULT") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            eventTypeBuilder.withProperty("comTaskEnablementId", ValueType.LONG, "comTaskEnablementId");
            eventTypeBuilder.withProperty("oldConnectionFunctionId", ValueType.LONG, "oldConnectionFunctionId");
            return eventTypeBuilder;
        }

        @Override
        EventTypeBuilder shouldPublish(EventTypeBuilder eventTypeBuilder) {
            return eventTypeBuilder.shouldPublish();
        }
    },
    COMTASKENABLEMENT_SWITCH_FROM_CONNECTION_FUNCTION_TO_TASK("comtaskenablement/SWITCH_FROM_CF_TO_TASK") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            eventTypeBuilder.withProperty("comTaskEnablementId", ValueType.LONG, "comTaskEnablementId");
            eventTypeBuilder.withProperty("oldConnectionFunctionId", ValueType.LONG, "oldConnectionFunctionId");
            eventTypeBuilder.withProperty("newPartialConnectionTaskId", ValueType.LONG, "newPartialConnectionTaskId");
            return eventTypeBuilder;
        }

        @Override
        EventTypeBuilder shouldPublish(EventTypeBuilder eventTypeBuilder) {
            return eventTypeBuilder.shouldPublish();
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

        @Override
        EventTypeBuilder shouldPublish(EventTypeBuilder eventTypeBuilder) {
            return eventTypeBuilder.shouldPublish();
        }
    },
    COMTASKENABLEMENT_SWITCH_BETWEEN_CONNECTION_FUNCTIONS("comtaskenablement/SWITCH_BETWEEN_CFS") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            eventTypeBuilder.withProperty("comTaskEnablementId", ValueType.LONG, "comTaskEnablementId");
            eventTypeBuilder.withProperty("oldConnectionFunctionId", ValueType.LONG, "oldConnectionFunctionId");
            eventTypeBuilder.withProperty("newConnectionFunctionId", ValueType.LONG, "newConnectionFunctionId");
            return eventTypeBuilder;
        }

        @Override
        EventTypeBuilder shouldPublish(EventTypeBuilder eventTypeBuilder) {
            return eventTypeBuilder.shouldPublish();
        }
    },
    COMTASKENABLEMENT_START_USING_TASK("comtaskenablement/USE_TASK") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            eventTypeBuilder.withProperty("comTaskEnablementId", ValueType.LONG, "comTaskEnablementId");
            return eventTypeBuilder;
        }

        @Override
        EventTypeBuilder shouldPublish(EventTypeBuilder eventTypeBuilder) {
            return eventTypeBuilder.shouldPublish();
        }
    },
    COMTASKENABLEMENT_REMOVE_TASK("comtaskenablement/REMOVE_TASK") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            eventTypeBuilder.withProperty("partialConnectionTaskId", ValueType.LONG, "partialConnectionTaskId");
            return eventTypeBuilder;
        }

        @Override
        EventTypeBuilder shouldPublish(EventTypeBuilder eventTypeBuilder) {
            return eventTypeBuilder.shouldPublish();
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
    PROTOCOLCONFIGURATIONPROPS_VALIDATEREMOVE_ONE("protocolconfigurationprops/VALIDATE_REMOVE_ONE"),
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
    SECURITY_PROPERTY_SET_VALIDATE_DELETE("securitypropertyset/VALIDATE_DELETE"),
    SECURITY_PROPERTY_SET_DELETED("securitypropertyset/DELETED"),
    DEVICE_MESSAGE_ENABLEMENT_UPDATED("devicemessageenablement/UPDATED"),
    DEVICE_MESSAGE_ENABLEMENT_CREATED("devicemessageenablement/CREATED"),
    DEVICE_MESSAGE_ENABLEMENT_DELETE("devicemessageenablement/DELETED"),
    DEVICE_CONFIG_CONFLICT_VALIDATE_CREATE("deviceconfigconflict/VALIDATE_CREATE"),
    ALLOWED_CALENDAR_VALIDATE_DELETE("allowedcalendar/VALIDATE_DELETE"),
    DEVICE_MESSAGE_FILE_OBSOLETE("devicemessagefile/OBSOLETE"),
    SECURITY_ACCESSOR_TYPE_VALIDATE_DELETE("securityaccessortype/VALIDATE_DELETE"),
    DEVICE_TYPE_PRE_DELETE("devicetype/PRE_DELETE"),
    DEVICE_TYPE_LIFE_CYCLE_CACHE_RECALCULATED("devicetype/LIFE_CYCLE_CACHE_RECALCULATED");

    private static final String NAMESPACE = "com/energyict/mdc/device/config/";
    private final String topic;

    EventType(String topic) {
        this.topic = topic;
    }

    public String topic() {
        return NAMESPACE + topic;
    }

    @TransactionRequired
    public void install(EventService eventService) {
        EventTypeBuilder builder = eventService.buildEventTypeWithTopic(topic())
                .name(name())
                .component(DeviceConfigConstants.COMPONENTNAME)
                .category("Crud")
                .scope("System");
        this.addCustomProperties(builder);
        this.shouldPublish(builder).create();
    }

    EventTypeBuilder shouldPublish(EventTypeBuilder eventTypeBuilder) {
        return eventTypeBuilder.shouldNotPublish();
    }

    protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
        eventTypeBuilder.withProperty("id", ValueType.LONG, "id");
        return eventTypeBuilder;
    }

}
