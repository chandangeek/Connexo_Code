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
import com.energyict.protocolimplv2.elster.garnet.structure.logbook.LogBookEvent;
import com.energyict.protocolimplv2.elster.garnet.structure.logbook.LogBookEventCode;
import com.energyict.protocolimplv2.elster.garnet.structure.logbook.SimpleEvent;
import com.energyict.protocolimplv2.elster.garnet.structure.logbook.SlaveRegistrationEvent;

import java.time.Clock;
import java.util.TimeZone;

/**
 * @author sva
 * @since 27/05/2014 - 13:57
 */
public class PoolingResponseWithLogsStructure extends Data<PoolingResponseWithLogsStructure> {

    public static final FunctionCode FUNCTION_CODE = FunctionCode.POOLING_RESPONSE_WITH_LOGS;

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

    public PoolingResponseWithLogsStructure(Clock clock, TimeZone timeZone) {
        super(FUNCTION_CODE);
        this.clock = clock;
        this.timeZone = timeZone;
        this.dateTime = new DateTime(clock, timeZone);
        this.model = new ConcentratorModel();
        this.userId = new UserId();
        this.logNr = new LogBookEventNr();
        this.totalNrOfLogs = new LogBookEventNr();
        this.sourceOfEvent = new Address();
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
    public PoolingResponseWithLogsStructure parse(byte[] rawData, int offset) throws ParsingException {
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
                this.eventData = new SlaveRegistrationEvent().parse(rawData, ptr);
                break;
            case CUSTOMER_CONFIGURATION:
                this.eventData = new SlaveRegistrationEvent().parse(rawData, ptr);
                break;
            case DISPLAY_CONFIGURATION:
                this.eventData = new SlaveRegistrationEvent().parse(rawData, ptr);
                break;
            case DISCONNECT_EVENT:
                this.eventData = new SlaveRegistrationEvent().parse(rawData, ptr);
                break;
            case RECONNECT_EVENT:
                this.eventData = new SlaveRegistrationEvent().parse(rawData, ptr);
                break;
            case CONCENTRATOR_CONFIGURATION:
                this.eventData = new SlaveRegistrationEvent().parse(rawData, ptr);
                break;
            case CONTACTOR_OPERATION_VIA_SERIAL:
                this.eventData = new SlaveRegistrationEvent().parse(rawData, ptr);
                break;
            case SENSOR_IN_ALARM:
                this.eventData = new SlaveRegistrationEvent().parse(rawData, ptr);
                break;
            case METER_SETUP_INCORRECT:
                this.eventData = new SlaveRegistrationEvent().parse(rawData, ptr);
                break;
            case AUTOMATIC_CONTACOTR_OPERATION:
                this.eventData = new SlaveRegistrationEvent().parse(rawData, ptr);
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
}