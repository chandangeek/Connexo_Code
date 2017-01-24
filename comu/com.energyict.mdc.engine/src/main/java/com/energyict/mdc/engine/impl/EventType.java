package com.energyict.mdc.engine.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.TransactionRequired;
import com.energyict.mdc.masterdata.MasterDataService;

/**
 * Models the different event types that are produced by the mdc engine bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-14 (13:42)
 */
public enum EventType {
    DEVICE_CONNECTION_FAILURE("connectiontask/FAILURE") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            return super.addCustomProperties(eventTypeBuilder).
                    withProperty("comSessionId", ValueType.LONG, "comSessionId").
                    withProperty("comPortId", ValueType.LONG, "comPortId").
                    withProperty("comServerId", ValueType.LONG, "comServerId").
                    withProperty("deviceIdentifier", ValueType.LONG, "deviceIdentifier").
                    withProperty("connectionTaskId", ValueType.LONG, "connectionTaskId");
        }
    },
    DEVICE_CONNECTION_COMPLETION("connectiontask/COMPLETION") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            return super.addCustomProperties(eventTypeBuilder).
                    withProperty("comSessionId", ValueType.LONG, "comSessionId").
                    withProperty("comPortId", ValueType.LONG, "comPortId").
                    withProperty("comServerId", ValueType.LONG, "comServerId").
                    withProperty("deviceIdentifier", ValueType.LONG, "deviceIdentifier").
                    withProperty("connectionTaskId", ValueType.LONG, "connectionTaskId").
                    withProperty("successTaskIDs", ValueType.STRING, "successTaskIDs").
                    withProperty("failedTaskIDs", ValueType.STRING, "failedTaskIDs").
                    withProperty("skippedTaskIDs", ValueType.STRING, "skippedTaskIDs");
        }
    },
    UNKNOWN_INBOUND_DEVICE("inboundcommunication/UNKNOWNDEVICE") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            return super.addCustomProperties(eventTypeBuilder).
                    withProperty("comPortName", ValueType.STRING, "comPortName").
                    withProperty("comServerName", ValueType.STRING, "comServerName").
                    withProperty("deviceIdentifier", ValueType.STRING, "deviceIdentifier").
                    withProperty("discoveryProtocolId", ValueType.LONG, "discoveryProtocolId");
        }
    },
    UNKNOWN_SLAVE_DEVICE("outboundcommunication/UNKNOWNSLAVEDEVICE") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            return super.addCustomProperties(eventTypeBuilder).
                    withProperty("deviceIdentifier", ValueType.STRING, "deviceIdentifier").
                    withProperty("masterDeviceId", ValueType.STRING, "masterDeviceId");
        }
    },
    DEVICE_TOPOLOGY_CHANGED("outboundcommunication/DEVICETOPOLOGYCHANGED") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            return super.addCustomProperties(eventTypeBuilder).
                    withProperty("deviceIdentifiers", ValueType.STRING, "slaveIdentifiers").
                    withProperty("masterDeviceId", ValueType.STRING, "masterDeviceId");
        }
    },
    NO_LOGBOOKS_FOR_DEVICE("outboundcommunication/NOLOGBOOKSFORDEVICE") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            return super.addCustomProperties(eventTypeBuilder).
                    withProperty("deviceIdentifier", ValueType.STRING, "deviceIdentifier");
        }
    },
    COMMANDS_WILL_BE_SENT("outboundcommunication/COMMANDSWILLBESENT");

    private static final String NAMESPACE = "com/energyict/mdc/";
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
                .component(MasterDataService.COMPONENTNAME)
                .category("Crud")
                .scope("System");
        this.addCustomProperties(builder).create();
    }

    @TransactionRequired
    void createIfNotExists(EventService eventService) {
        if (!eventService.getEventType(topic()).isPresent()) {
            install(eventService);
        }
    }

    protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
        return eventTypeBuilder;
    }

}