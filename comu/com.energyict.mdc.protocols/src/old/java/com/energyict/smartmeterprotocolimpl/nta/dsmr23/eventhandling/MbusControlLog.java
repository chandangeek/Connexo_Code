/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;

import java.util.Date;
import java.util.List;

public class MbusControlLog extends AbstractEvent {

    // Mbus control log
    private static final int EVENT_EVENT_LOG_CLEARED = 255;

    private static final int EVENT_MANUAL_DISCONNECTION_MBUS1 = 160;
    private static final int EVENT_MANUAL_CONNECTION_MBUS1 = 161;
    private static final int EVENT_REMOTE_DISCONNECTION_MBUS1 = 162;
    private static final int EVENT_REMOTE_CONNECTION_MBUS1 = 163;
    private static final int EVENT_VALVE_ALARM_MBUS1 = 164;

    private static final int EVENT_MANUAL_DISCONNECTION_MBUS2 = 170;
    private static final int EVENT_MANUAL_CONNECTION_MBUS2 = 171;
    private static final int EVENT_REMOTE_DISCONNECTION_MBUS2 = 172;
    private static final int EVENT_REMOTE_CONNECTION_MBUS2 = 173;
    private static final int EVENT_VALVE_ALARM_MBUS2 = 174;

    private static final int EVENT_MANUAL_DISCONNECTION_MBUS3 = 180;
    private static final int EVENT_MANUAL_CONNECTION_MBUS3 = 181;
    private static final int EVENT_REMOTE_DISCONNECTION_MBUS3 = 182;
    private static final int EVENT_REMOTE_CONNECTION_MBUS3 = 183;
    private static final int EVENT_VALVE_ALARM_MBUS3 = 184;

    private static final int EVENT_MANUAL_DISCONNECTION_MBUS4 = 190;
    private static final int EVENT_MANUAL_CONNECTION_MBUS4 = 191;
    private static final int EVENT_REMOTE_DISCONNECTION_MBUS4 = 192;
    private static final int EVENT_REMOTE_CONNECTION_MBUS4 = 193;
    private static final int EVENT_VALVE_ALARM_MBUS4 = 194;

    public MbusControlLog(DataContainer dc, final AXDRDateTimeDeviationType deviationType) {
        super(dc, deviationType);
    }

    public MbusControlLog(DataContainer dc) {
        super(dc);
    }

    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {

        if (!ExtraEvents.extraEvents.containsKey(new Integer(eventId))) {
            switch (eventId) {
                case EVENT_EVENT_LOG_CLEARED: {
                    meterEvents.add(createNewMbusControlLogbookEvent(eventTimeStamp, MeterEvent.EVENT_LOG_CLEARED, eventId, "Mbus control event log profile cleared"));
                }
                break;

                case EVENT_MANUAL_DISCONNECTION_MBUS1: {
                    meterEvents.add(createNewMbusControlLogbookEvent(eventTimeStamp, MeterEvent.MANUAL_DISCONNECTION_MBUS, eventId, "The disconnector has been manually disconnected (MBus1)"));
                }
                break;
                case EVENT_MANUAL_CONNECTION_MBUS1: {
                    meterEvents.add(createNewMbusControlLogbookEvent(eventTimeStamp, MeterEvent.MANUAL_CONNECTION_MBUS, eventId, "The disconnector has been manually connected (MBus1)"));
                }
                break;
                case EVENT_REMOTE_DISCONNECTION_MBUS1: {
                    meterEvents.add(createNewMbusControlLogbookEvent(eventTimeStamp, MeterEvent.REMOTE_DISCONNECTION_MBUS, eventId, "The disconnector has been remotely disconnected (MBus1)"));
                }
                break;
                case EVENT_REMOTE_CONNECTION_MBUS1: {
                    meterEvents.add(createNewMbusControlLogbookEvent(eventTimeStamp, MeterEvent.REMOTE_CONNECTION_MBUS, eventId, "The disconnector has been remotely connected (MBus1)"));
                }
                break;
                case EVENT_VALVE_ALARM_MBUS1: {
                    meterEvents.add(createNewMbusControlLogbookEvent(eventTimeStamp, MeterEvent.VALVE_ALARM_MBUS, eventId, "Valve alarm has been registerd (MBus1)"));
                }
                break;

                case EVENT_MANUAL_DISCONNECTION_MBUS2: {
                    meterEvents.add(createNewMbusControlLogbookEvent(eventTimeStamp, MeterEvent.MANUAL_DISCONNECTION_MBUS, eventId, "The disconnector has been manually disconnected (MBus2)"));
                }
                break;
                case EVENT_MANUAL_CONNECTION_MBUS2: {
                    meterEvents.add(createNewMbusControlLogbookEvent(eventTimeStamp, MeterEvent.MANUAL_CONNECTION_MBUS, eventId, "The disconnector has been manually connected (MBus2)"));
                }
                break;
                case EVENT_REMOTE_DISCONNECTION_MBUS2: {
                    meterEvents.add(createNewMbusControlLogbookEvent(eventTimeStamp, MeterEvent.REMOTE_DISCONNECTION_MBUS, eventId, "The disconnector has been remotely disconnected (MBus2)"));
                }
                break;
                case EVENT_REMOTE_CONNECTION_MBUS2: {
                    meterEvents.add(createNewMbusControlLogbookEvent(eventTimeStamp, MeterEvent.REMOTE_CONNECTION_MBUS, eventId, "The disconnector has been remotely connected (MBus2)"));
                }
                break;
                case EVENT_VALVE_ALARM_MBUS2: {
                    meterEvents.add(createNewMbusControlLogbookEvent(eventTimeStamp, MeterEvent.VALVE_ALARM_MBUS, eventId, "Valve alarm has been registerd (MBus2)"));
                }
                break;

                case EVENT_MANUAL_DISCONNECTION_MBUS3: {
                    meterEvents.add(createNewMbusControlLogbookEvent(eventTimeStamp, MeterEvent.MANUAL_DISCONNECTION_MBUS, eventId, "The disconnector has been manually disconnected (Mbus3)"));
                }
                break;
                case EVENT_MANUAL_CONNECTION_MBUS3: {
                    meterEvents.add(createNewMbusControlLogbookEvent(eventTimeStamp, MeterEvent.MANUAL_CONNECTION_MBUS, eventId, "The disconnector has been manually connected (Mbus3)"));
                }
                break;
                case EVENT_REMOTE_DISCONNECTION_MBUS3: {
                    meterEvents.add(createNewMbusControlLogbookEvent(eventTimeStamp, MeterEvent.REMOTE_DISCONNECTION_MBUS, eventId, "The disconnector has been remotely disconnected (Mbus3)"));
                }
                break;
                case EVENT_REMOTE_CONNECTION_MBUS3: {
                    meterEvents.add(createNewMbusControlLogbookEvent(eventTimeStamp, MeterEvent.REMOTE_CONNECTION_MBUS, eventId, "The disconnector has been remotely connected (Mbus3)"));
                }
                break;
                case EVENT_VALVE_ALARM_MBUS3: {
                    meterEvents.add(createNewMbusControlLogbookEvent(eventTimeStamp, MeterEvent.VALVE_ALARM_MBUS, eventId, "Valve alarm has been registerd (Mbus3)"));
                }
                break;

                case EVENT_MANUAL_DISCONNECTION_MBUS4: {
                    meterEvents.add(createNewMbusControlLogbookEvent(eventTimeStamp, MeterEvent.MANUAL_DISCONNECTION_MBUS, eventId, "The disconnector has been manually disconnected (Mbus4)"));
                }
                break;
                case EVENT_MANUAL_CONNECTION_MBUS4: {
                    meterEvents.add(createNewMbusControlLogbookEvent(eventTimeStamp, MeterEvent.MANUAL_CONNECTION_MBUS, eventId, "The disconnector has been manually connected (Mbus4)"));
                }
                break;
                case EVENT_REMOTE_DISCONNECTION_MBUS4: {
                    meterEvents.add(createNewMbusControlLogbookEvent(eventTimeStamp, MeterEvent.REMOTE_DISCONNECTION_MBUS, eventId, "The disconnector has been remotely disconnected (Mbus4)"));
                }
                break;
                case EVENT_REMOTE_CONNECTION_MBUS4: {
                    meterEvents.add(createNewMbusControlLogbookEvent(eventTimeStamp, MeterEvent.REMOTE_CONNECTION_MBUS, eventId, "The disconnector has been remotely connected (Mbus4)"));
                }
                break;
                case EVENT_VALVE_ALARM_MBUS4: {
                    meterEvents.add(createNewMbusControlLogbookEvent(eventTimeStamp, MeterEvent.VALVE_ALARM_MBUS, eventId, "Valve alarm has been registerd (Mbus4)"));
                }
                break;

                default: {
                    meterEvents.add(createNewMbusControlLogbookEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
                }
                break;
            }
        } else {
            meterEvents.add(ExtraEvents.getExtraEvent(eventTimeStamp, eventId));
        }
    }

    public MeterEvent createNewMbusControlLogbookEvent(Date eventTimeStamp, int meterEvent, int eventId, String message) {
        return new MeterEvent(eventTimeStamp, meterEvent, eventId, message, EventLogbookId.MbusControlLogbook.eventLogId(), 0);
    }
}
