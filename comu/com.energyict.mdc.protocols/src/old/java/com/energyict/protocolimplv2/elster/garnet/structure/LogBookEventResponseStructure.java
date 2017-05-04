/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Address;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Data;
import com.energyict.protocolimplv2.elster.garnet.frame.field.FunctionCode;
import com.energyict.protocolimplv2.elster.garnet.structure.field.ConcentratorModel;
import com.energyict.protocolimplv2.elster.garnet.structure.field.DateTime;
import com.energyict.protocolimplv2.elster.garnet.structure.field.LogBookEventNr;
import com.energyict.protocolimplv2.elster.garnet.structure.field.UserId;
import com.energyict.protocolimplv2.elster.garnet.structure.logbook.AutomaticContactorOperationEvent;
import com.energyict.protocolimplv2.elster.garnet.structure.logbook.ConcentratorConfigurationEvent;
import com.energyict.protocolimplv2.elster.garnet.structure.logbook.ContactorOperationViaSerialNumberEvent;
import com.energyict.protocolimplv2.elster.garnet.structure.logbook.CustomerConfigurationEvent;
import com.energyict.protocolimplv2.elster.garnet.structure.logbook.DisconnectEvent;
import com.energyict.protocolimplv2.elster.garnet.structure.logbook.DisplayConfigurationEvent;
import com.energyict.protocolimplv2.elster.garnet.structure.logbook.LogBookEvent;
import com.energyict.protocolimplv2.elster.garnet.structure.logbook.LogBookEventCode;
import com.energyict.protocolimplv2.elster.garnet.structure.logbook.MeterSetupIncorrectEvent;
import com.energyict.protocolimplv2.elster.garnet.structure.logbook.ReconnectEvent;
import com.energyict.protocolimplv2.elster.garnet.structure.logbook.SchedulingConfigurationEvent;
import com.energyict.protocolimplv2.elster.garnet.structure.logbook.SensorInAlarmEvent;
import com.energyict.protocolimplv2.elster.garnet.structure.logbook.SimpleEvent;
import com.energyict.protocolimplv2.elster.garnet.structure.logbook.SlaveRegistrationEvent;

import java.time.Clock;
import java.util.TimeZone;

/**
 * @author sva
 * @since 27/05/2014 - 13:57
 */
public class LogBookEventResponseStructure extends Data<LogBookEventResponseStructure> {

    public static final FunctionCode FUNCTION_CODE = FunctionCode.LOGBOOK_EVENT_RESPONSE;
    private static final boolean PARSE_AS_BIG_ENDIAN = true;

    private final Clock clock;

    private DateTime dateTime;
    private ConcentratorModel model;
    private UserId userId;
    private LogBookEventNr logNr;
    private LogBookEventNr totalNrOfLogs;
    private Address sourceOfEvent;
    private DateTime dateTimeOfEvent;
    private LogBookEventCode eventCode;
    private LogBookEvent eventData;

    private final TimeZone timeZone;

    public LogBookEventResponseStructure(Clock clock, TimeZone timeZone) {
        super(FUNCTION_CODE);
        this.clock = clock;
        this.timeZone = timeZone;
        this.dateTime = new DateTime(clock, timeZone);
        this.model = new ConcentratorModel();
        this.userId = new UserId();
        this.logNr = new LogBookEventNr();
        this.totalNrOfLogs = new LogBookEventNr();
        this.sourceOfEvent = new Address(PARSE_AS_BIG_ENDIAN);
        this.dateTimeOfEvent = new DateTime(clock, timeZone);
        this.eventCode = new LogBookEventCode();
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                dateTime.getBytes(),
                model.getBytes(),
                userId.getBytes(),
                logNr.getBytes(),
                totalNrOfLogs.getBytes(),
                sourceOfEvent.getBytes(),
                dateTimeOfEvent.getBytes(),
                eventCode.getBytes(),
                eventData.getBytes()
        );
    }

    @Override
    public LogBookEventResponseStructure parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        dateTime.parse(rawData, ptr);
        ptr += dateTime.getLength();

        this.model.parse(rawData, ptr);
        ptr += model.getLength();

        this.userId.parse(rawData, ptr);
        ptr += userId.getLength();

        this.logNr.parse(rawData, ptr);
        ptr += logNr.getLength();

        this.totalNrOfLogs.parse(rawData, ptr);
        ptr += totalNrOfLogs.getLength();

        this.sourceOfEvent.parse(rawData, ptr);
        ptr += sourceOfEvent.getLength();

        this.dateTimeOfEvent.parse(rawData, ptr);
        ptr += dateTimeOfEvent.getLength();

        this.eventCode.parse(rawData, ptr);
        ptr += eventCode.getLength();

        this.parseEventData(rawData, ptr);
        return this;
    }

    private void parseEventData(byte[] rawData, int ptr) throws ParsingException {
        switch (this.eventCode.getEventCode()) {
            case SLAVE_REGISTRATION:
                this.eventData = new SlaveRegistrationEvent().parse(rawData, ptr);
                break;
            case SCHEDULING_CONFIGURATION:
                this.eventData = new SchedulingConfigurationEvent(this.clock, getTimeZone()).parse(rawData, ptr);
                break;
            case CUSTOMER_CONFIGURATION:
                this.eventData = new CustomerConfigurationEvent().parse(rawData, ptr);
                break;
            case DISPLAY_CONFIGURATION:
                this.eventData = new DisplayConfigurationEvent().parse(rawData, ptr);
                break;
            case DISCONNECT_EVENT:
                this.eventData = new DisconnectEvent().parse(rawData, ptr);
                break;
            case RECONNECT_EVENT:
                this.eventData = new ReconnectEvent().parse(rawData, ptr);
                break;
            case CONCENTRATOR_CONFIGURATION:
                this.eventData = new ConcentratorConfigurationEvent().parse(rawData, ptr);
                break;
            case CONTACTOR_OPERATION_VIA_SERIAL:
                this.eventData = new ContactorOperationViaSerialNumberEvent().parse(rawData, ptr);
                break;
            case SENSOR_IN_ALARM:
                this.eventData = new SensorInAlarmEvent().parse(rawData, ptr);
                break;
            case METER_SETUP_INCORRECT:
                this.eventData = new MeterSetupIncorrectEvent().parse(rawData, ptr);
                break;
            case AUTOMATIC_CONTACOTR_OPERATION:
                this.eventData = new AutomaticContactorOperationEvent().parse(rawData, ptr);
                break;
            default:
                this.eventData = new SimpleEvent(this.eventCode).parse(rawData, ptr);
                break;
        }
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public ConcentratorModel getModel() {
        return model;
    }

    public UserId getUserId() {
        return userId;
    }

    public LogBookEventNr getLogNr() {
        return logNr;
    }

    public LogBookEventNr getTotalNrOfLogs() {
        return totalNrOfLogs;
    }

    public Address getSourceOfEvent() {
        return sourceOfEvent;
    }

    public DateTime getDateTimeOfEvent() {
        return dateTimeOfEvent;
    }

    public LogBookEventCode getEventCode() {
        return eventCode;
    }

    public LogBookEvent getEventData() {
        return eventData;
    }

    public String getEventDescription() {
        return getEventData().getEventDescription();
    }
}