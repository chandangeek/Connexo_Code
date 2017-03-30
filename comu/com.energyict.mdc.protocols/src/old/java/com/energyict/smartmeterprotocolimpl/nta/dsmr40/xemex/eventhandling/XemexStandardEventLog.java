/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex.eventhandling;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.eventhandling.StandardEventLog;

import java.util.Date;
import java.util.List;

/**
 * Extends the original DSMR4.0 EventsLog with additional events who are specific for the Xemex datalogger
 */
public class XemexStandardEventLog extends StandardEventLog {

    private static final int EVENT_MAINS_POWERED_ON = 21;
    private static final int EVENT_MAINS_POWERED_OFF = 22;
    private static final int EVENT_BACKUP_BATTERY_LOW = 23;
    private static final int EVENT_GPRS_MODEM_IN_USE = 24;
    private static final int EVENT_PSTN_MODEM_IN_USE = 25;
    private static final int EVENT_MT_NUMBER_OF_CONNECTIONS = 26;
    private static final int EVENT_AS_NUMBER_OF_CONNECTIONS = 27;
    private static final int EVENT_MT_AMOUNT_OF_TRANSMIT_DATA = 28;
    private static final int EVENT_AS_AMOUNT_OF_TRANSMIT_DATA = 29;
    private static final int EVENT_GSM_REGISTRATION = 30;
    private static final int EVENT_PSTN_MODEM_RESET = 31;
    private static final int EVENT_MT_CONNECTED = 32;
    private static final int EVENT_MT_DISCONNECTED = 33;
    private static final int EVENT_XMX_CONNECTION_FAILED = 231;

    public XemexStandardEventLog(DataContainer dc, AXDRDateTimeDeviationType deviationType) {
        super(dc, deviationType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void buildMeterEvent(final List<MeterEvent> meterEvents, final Date eventTimeStamp, final int eventId) {
        switch (eventId) {
            case EVENT_MAINS_POWERED_ON: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Mains power applied"));
            }
            break;
            case EVENT_MAINS_POWERED_OFF: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Mains power removed"));
            }
            break;
            case EVENT_BACKUP_BATTERY_LOW: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.BATTERY_VOLTAGE_LOW, eventId, "Backup battery voltage low"));
            }
            break;
            case EVENT_GPRS_MODEM_IN_USE: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Using GPRS modem for communication"));
            }
            break;
            case EVENT_PSTN_MODEM_IN_USE: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Using PSTN modem for communication"));
            }
            break;
            case EVENT_MT_NUMBER_OF_CONNECTIONS: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.METER_ALARM, eventId, "Maximum yearly number of allowed connections with the MT exceeded"));
            }
            break;
            case EVENT_AS_NUMBER_OF_CONNECTIONS: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.METER_ALARM, eventId, "Maximum yearly number of allowed connections with the AS exceeded"));
            }
            break;
            case EVENT_MT_AMOUNT_OF_TRANSMIT_DATA: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.METER_ALARM, eventId, "Maximum yearly allowed transmit data budget for MT exceeded"));
            }
            break;
            case EVENT_AS_AMOUNT_OF_TRANSMIT_DATA: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.METER_ALARM, eventId, "Maximum yearly allowed transmit data budget for AS exceeded"));
            }
            break;
            case EVENT_GSM_REGISTRATION: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.METER_ALARM, eventId, "Registration of GPRS module to the GSM network took longer than normal"));
            }
            break;
            case EVENT_PSTN_MODEM_RESET: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "PSTN modem is reset"));
            }
            break;
            case EVENT_MT_CONNECTED: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Start of a maintenance tool connection/session"));
            }
            break;
            case EVENT_MT_DISCONNECTED: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "End of a maintenance tool connection/session"));
            }
            case EVENT_XMX_CONNECTION_FAILED: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.METER_ALARM, eventId, "Connection to the SIM card failed"));
            }
            break;
            default: {
                super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
            }
            break;
        }
    }
}
