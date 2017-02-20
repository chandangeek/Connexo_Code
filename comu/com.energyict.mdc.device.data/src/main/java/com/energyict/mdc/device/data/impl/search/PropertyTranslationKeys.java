/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Provides the translation keys for the search properties
 * that are supported by the device data bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-01 (15:46)
 */
public enum PropertyTranslationKeys implements TranslationKey {

    DEVICE_DOMAIN("device.search.domain", "Device"),
    DEVICE_MRID("device.mRID", "MRID"),
    DEVICE_NAME("device.name", "Name"),
    DEVICE_SERIAL_NUMBER("device.serial.number", "Serial number"),
    DEVICE_TYPE("device.type", "Device type"),
    DEVICE_CONFIGURATION("device.configuration", "Device configuration"),
    DEVICE_STATUS("device.status.name", "State"),
    DEVICE_GROUP("device.group", "Device group"),
    DEVICE_CERT_YEAR("device.cert.year", "Year of certification"),
    BATCH("device.batch", "Batch"),
    CONNECTION_METHOD("device.connection.method", "Connection method"),
    SHARED_SCHEDULE("device.shared.schedule", "Shared schedule"),
    USAGE_POINT("device.usage.point", "Usage point"),
    SERVICE_CATEGORY("device.service.category", "Service category"),
    TOPOLOGY("device.topology", "Topology"),
    DEVICE_MASTER_NAME("device.master.name", "Master device"),
    DEVICE_SLAVE_NAME("device.slave.name", "Slave device"),
    VALIDATION("device.validation", "Validation"),
    VALIDATION_STATUS("device.validation.status", "Data validation"),
    ESTIMATION("device.estimation", "Estimation"),
    ESTIMATION_STATUS("device.estimation.status", "Data estimation"),
    SECURITY("device.security", "Security"),
    SECURITY_NAME("device.security.name", "Name"),
    REGISTER("device.register", "Register"),
    REGISTER_OBISCODE("device.register.obiscode", "OBIS code"),
    REGISTER_LAST_READING("device.register.last.reading", "Last reading"),
    READING_TYPE_NAME("reading.type.name", "Reading type: Name"),
    READING_TYPE_UNIT_OF_MEASURE("reading.type.unit.of.measure", "Reading type: Unit of measure"),
    READING_TYPE_TOU("reading.type.tou", "Reading type: Time of use"),
    PROTOCOL_DIALECT("protocol.dialect", "Protocol dialect"),
    CHANNEL("device.channel", "Channel"),
    CHANNEL_OBISCODE("device.channel.obiscode", "OBIS code"),
    CHANNEL_INTERVAL("device.channel.interval", "Interval"),
    CHANNEL_LAST_READING("device.channel.last.reading", "Last reading"),
    CHANNEL_LAST_VALUE("device.channel.last.value", "Timestamp last value"),
    LOGBOOK("device.logbook", "Logbook"),
    LOGBOOK_NAME("device.logbook.name", "Name"),
    LOGBOOK_OBISCODE("device.logbook.obiscode", "OBIS code"),
    LOGBOOK_LAST_READING("device.logbook.last.reading", "Last reading"),
    LOGBOOK_LAST_EVENT("device.logbook.last.event", "Last event timestamp"),
    LOADPROFILE("device.loadprofile", "Load profile"),
    LOADPROFILE_NAME("device.loadprofile.name", "Name"),
    LOADPROFILE_LAST_READING("device.loadprofile.last.reading", "Last reading"),
    COMTASK("device.comtask", "Communication task"),
    COMTASK_NAME("device.comtask.name", "Name"),
    COMTASK_SECURITY_SETTING("device.comtask.security", "Security settings"),
    COMTASK_LAST_COMMUNICATION("device.comtask.last.communication", "Last communication start"),
    COMTASK_NEXT_COMMUNICATION("device.comtask.next.communication", "Next communication"),
    COMTASK_STATUS("device.comtask.status", "Current status"),
    COMTASK_SCHEDULE_TYPE("device.comtask.schedule.type", "Schedule type"),
    COMTASK_SCHEDULE_NAME("device.comtask.schedule.name", "Schedule name"),
    COMTASK_PLANNED_DATE("device.comtask.planned.date", "Planned date"),
    COMTASK_URGENCY("device.comtask.urgency", "Urgency"),
    PROTOCOL_DIALECT_DYNAMIC_PROP("protocol.dialect.dynamic", "Protocol dialect dynamic properties"),
    CONNECTION("device.connection", "Connection"),
    CONNECTION_NAME("device.connection.name", "Name"),
    CONNECTION_DIRECTION("device.connection.direction", "Direction"),
    CONNECTION_PORTPOOL("device.connection.portpool", "Communication port pool"),
    CONNECTION_SIMULTANEOUS("device.connection.simultaneous", "Allow simultaneous connection"),
    CONNECTION_STATUS("device.connection.status", "Status"),
    TRANSITIONS("device.transition", "Transitions"),
    TRANSITION_SHIPMENT("device.transition.shipment", "Shipment date"),
    TRANSITION_INSTALLATION("device.transition.installation", "Installation date"),
    TRANSITION_DEACTIVATION("device.transition.deactivation", "Deactivation date"),
    TRANSITION_DECOMMISSIONING("device.transition.decommissioning", "Decommissioning date"),
    GENERAL_ATTRIBUTES_DYNAMIC_PROP("deviceType.attr.dynamic", "General attributes dynamic properties"),
    CONNECTION_DYNAMIC_PROP("device.connection.method.dynamic", "Connection dynamic attributes"),

    COMTASK_SCHEDULE_TYPE_ON_REQUEST("device.comtask.schedule.type.ScheduleTypeKey.ON_REQUEST", "On request"),
    COMTASK_SCHEDULE_TYPE_INDIVIDUAL("device.comtask.schedule.type.ScheduleTypeKey.INDIVIDUAL", "Individual"),
    COMTASK_SCHEDULE_TYPE_SHARED("device.comtask.schedule.type.ScheduleTypeKey.SHARED", "Shared"),

    CONNECTION_TASK_STATUS_INCOMPLETE("device.connection.status.INCOMPLETE", "Incomplete"),
    CONNECTION_TASK_STATUS_ACTIVE("device.connection.status.ACTIVE", "Active"),
    CONNECTION_TASK_STATUS_INACTIVE("device.connection.status.INACTIVE", "Inactive"),

    CONNECTION_TASK_DIRECTION_INBOUND("device.connection.direction.inbound", "Inbound"),
    CONNECTION_TASK_DIRECTION_OUTBOUND("device.connection.direction.outbound", "Outbound"),
    DEVICE_LOCATION("device.location", "Location"),
    FIRMWARE("device.firmware","Firmware") ,
    FIRMWARE_VERSION("device.firmware.version", "Firmware version"),

    DEVICE_DATA_STATUS_ACTIVE("device.data.status.ACTIVE", "Active"),
    DEVICE_DATA_STATUS_INACTIVE("device.data.status.INACTIVE", "Inactive"),
    DEVICE_MANUFACTURER("device.manufacturer", "Manufacturer"),
    DEVICE_MODEL_NBR("device.model.nbr", "Model number"),
    DEVICE_MODEL_VERSION("device.model.version", "Model version"),
    DEVICE_ATTRIBUTES("device.attributes", "Device attributes")

    DEVICE_MASTER_SEARCH_CRITERION_NAME("device.master.searchcriterion.search", "Has master device"),
    ;

    private String key;
    private String defaultFormat;

    PropertyTranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }


    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

}