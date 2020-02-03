package com.energyict.protocolimplv2.nta.dsmr40.eventhandling;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimplv2.nta.dsmr23.eventhandling.DSMR23MbusEventLog;

import java.util.Date;
import java.util.List;

import static com.energyict.protocol.MeterEvent.CONFIGURATIONCHANGE;
import static com.energyict.protocol.MeterEvent.MEASUREMENT_SYSTEM_ERROR;

/**
 *  Extends the D.S.M.R v 2.3 MBUSLog with additional events for D.S.M.R. v4.0 as described int he D.S.M.R. v4.0 chapter 4.2.1
 *
 *
        100     Communication error MBus channel 1
        101     Communication ok M-Bus channel 1
        102     Replace Battery M-Bus channel 1
        103     Fraud attempt M-Bus channel 1
        104     Clock adjusted M-Bus channel 1
        105     New M-Bus device discovered channel 1
        106     Permanent error from MBus device channel 1
        107-109 reserved for future use

        110     Communication error MBus channel 2
        111     Communication ok M-Bus channel 2
        112     Replace Battery M-Bus channel 2
        113     Fraud attempt M-Bus channel 2
        114     Clock adjusted M-Bus channel 2
        115     New M-Bus device discovered channel 2
        116     Permanent error from MBus device channel 2
        117-119 reserved for future use

        120     Communication error MBus channel 3
        121     Communication ok M-Bus channel 3
        122     Replace Battery M-Bus channel 3
        123     Fraud attempt M-Bus channel 3
        124     Clock adjusted M-Bus channel 3
        125     New M-Bus device discovered channel 3
        126     Permanent error from MBus device channel 3
        127-129 reserved for future use

        130     Communication error MBus channel 4
        131     Communication ok M-Bus channel 4
        132     Replace Battery M-Bus channel 4
        133     Fraud attempt M-Bus channel 4
        134     Clock adjusted M-Bus channel 4
        135     New M-Bus device discovered channel 4
        136     Permanent error from MBus device channel 4
        137-139 RESERVED FOR FUTURE USE
 */


public class DSMR40MbusEventLog extends DSMR23MbusEventLog {

    public enum DSMR40Event {
        //COMMUNICATION_ERROR     (0,     COMMUNICATION_ERROR_MBUS,   "Communication problem when reading the meter connected to channel %s of the M-Bus" ),
        //COMMUNICATION_OK        (1,     COMMUNICATION_OK_MBUS,      "Communication with the M-Bus meter connected to channel %s of the M-Bus is ok again"),
        //REPLACE_BATTERY         (2,     REPLACE_BATTERY_MBUS,       "Battery must be exchanged due to the expected end of life time on channel %s"),
        //FRAUD_ATTEMPT           (3,     FRAUD_ATTEMPT_MBUS,         "Fraud attempt has been registered on channel %s"),
        //CLOCK_ADJUSTED          (4,     CLOCK_ADJUSTED_MBUS,        "Clock has been adjusted on channel %s"),
        NEW_MBUS_DISCOVERED     (5,     CONFIGURATIONCHANGE,        "A new M-Bus Device has been detected on channel %s"),
        PERMANENT_ERROR_MBUS    (6,     MEASUREMENT_SYSTEM_ERROR,   "Permanent error on Mbus channel %s");
        //LOGBOOK_CLEARED         (255,   EVENT_LOG_CLEARED,          "Mbus event log profile cleared");

        private final int eventIndex;
        private final int eiCode;
        private final String message;

        DSMR40Event(int eventIndex, int eiCode, String message) {
            this.eventIndex=eventIndex;
            this.eiCode = eiCode;
            this.message = message;
        }

        public static DSMR40Event forMBUSClientAndProtocolCode(MBUSClient mbusClient, int protocolCode) {
            for (DSMR40Event event : values()) {
                if (protocolCode==mbusClient.getStartIndex()+ event.getEventIndex()) {
                    return event;
                }
            }
            return null;
        }

        private int getEventIndex() {
            return eventIndex;
        }

        public String getMessage(MBUSClient client) {
            return String.format(message, client.getChannel());
        }

        public int getEiCode() {
            return eiCode;
        }
    }

    public DSMR40MbusEventLog(DataContainer dc, int mBusChannel) {
        super(dc, mBusChannel);
    }

    @Override
    protected void buildMeterEvent(final List<MeterEvent> meterEvents, final Date eventTimeStamp, final int protocolCode) {
        MBUSClient mbusClient = MBUSClient.forChannel(mBusChannel);
        if (mbusClient!=null) {
            DSMR40Event dsmr40Event = DSMR40Event.forMBUSClientAndProtocolCode(mbusClient, protocolCode);
            if (dsmr40Event != null) {
                meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, dsmr40Event.getEiCode(), protocolCode, dsmr40Event.getMessage(mbusClient)));
            }
            else {
                super.buildMeterEvent(meterEvents, eventTimeStamp, protocolCode);
            }
        }
    }
}
