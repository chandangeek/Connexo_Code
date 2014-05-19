package com.energyict.mdc.engine.impl;

import com.energyict.mdc.masterdata.MasterDataService;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.TransactionRequired;

/**
 * Models the different event types that are produced by the mdc engine bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-14 (13:42)
 */
public enum EventType {
    DEVICE_CONNECTION_SETUP_FAILURE("connectiontasksetup/FAILURE") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            return super.addCustomProperties(eventTypeBuilder).
                    withProperty("comPortName", ValueType.STRING, "comPortName").
                    withProperty("comServerName", ValueType.STRING, "comServerName").
                    withProperty("deviceIdentifier", ValueType.STRING, "deviceIdentifier").
                    withProperty("connectionTypePluggableClassId", ValueType.LONG, "connectionTypePluggableClassId");
        }
    },
    DEVICE_CONNECTION_FAILURE("connectiontask/FAILURE") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            return super.addCustomProperties(eventTypeBuilder).
                    withProperty("comPortName", ValueType.STRING, "comPortName").
                    withProperty("comServerName", ValueType.STRING, "comServerName").
                    withProperty("deviceIdentifier", ValueType.STRING, "deviceIdentifier");
        }
    },
    DEVICE_COMMUNICATION_FAILURE("comtask/FAILURE") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            return super.addCustomProperties(eventTypeBuilder).
                    withProperty("comTaskId", ValueType.STRING, "comTaskId").
                    withProperty("comPortName", ValueType.STRING, "comPortName").
                    withProperty("comServerName", ValueType.STRING, "comServerName").
                    withProperty("deviceIdentifier", ValueType.STRING, "deviceIdentifier");
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
    };

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
                .scope("System")
                .shouldPublish();
        this.addCustomProperties(builder).create().save();
    }

    protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
        return eventTypeBuilder;
    }

}