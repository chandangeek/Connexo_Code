package com.energyict.protocolimplv2.nta.dsmr23.eventhandling;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;

import java.util.Date;
import java.util.List;

import static com.energyict.protocol.MeterEvent.CLOCK_ADJUSTED_MBUS;
import static com.energyict.protocol.MeterEvent.COMMUNICATION_ERROR_MBUS;
import static com.energyict.protocol.MeterEvent.COMMUNICATION_OK_MBUS;
import static com.energyict.protocol.MeterEvent.EVENT_LOG_CLEARED;
import static com.energyict.protocol.MeterEvent.FRAUD_ATTEMPT_MBUS;
import static com.energyict.protocol.MeterEvent.REPLACE_BATTERY_MBUS;

/**
 * Class to converts the MBUSEventLog which supports the following Events as described in the D.S.M.R. v2.3 chapter 4.2.1
 *
 * The LogBook is stored on the Master, filtering is needed to only select the items which are applicable for the requested channel.

        255     Event log cleared

        100     Communication error MBus channel 1
        101     Communication ok M-Bus channel 1
        102     Replace Battery M-Bus channel 1
        103     Fraud attempt M-Bus channel 1
        104     Clock adjusted M-Bus channel 1
        105-109 reserved for future use

        110     Communication error MBus channel 2
        111     Communication ok M-Bus channel 2
        112     Replace Battery M-Bus channel 2
        113     Fraud attempt M-Bus channel 2
        114     Clock adjusted M-Bus channel 2
        115-119 reserved for future use

        120     Communication error MBus channel 3
        121     Communication ok M-Bus channel 3
        122     Replace Battery M-Bus channel 3
        123     Fraud attempt M-Bus channel 3
        124     Clock adjusted M-Bus channel 3
        125-129 reserved for future use

        130     Communication error MBus channel 4
        131     Communication ok M-Bus channel 4
        132     Replace Battery M-Bus channel 4
        133     Fraud attempt M-Bus channel 4
        134     Clock adjusted M-Bus channel 4
        135-139 RESERVED FOR FUTURE USE

 */

public class DSMR23MbusEventLog extends AbstractEvent {

    public enum DSMR23Event {
        COMMUNICATION_ERROR     (0,     COMMUNICATION_ERROR_MBUS,   "Communication problem when reading the meter connected to channel %s of the M-Bus" ),
        COMMUNICATION_OK        (1,     COMMUNICATION_OK_MBUS,      "Communication with the M-Bus meter connected to channel %s of the M-Bus is ok again"),
        REPLACE_BATTERY         (2,     REPLACE_BATTERY_MBUS,       "Battery must be exchanged due to the expected end of life time on channel %s"),
        FRAUD_ATTEMPT           (3,     FRAUD_ATTEMPT_MBUS,         "Fraud attempt has been registered on channel %s"),
        CLOCK_ADJUSTED          (4,     CLOCK_ADJUSTED_MBUS,        "Clock has been adjusted on channel %s"),
        LOGBOOK_CLEARED         (255,   EVENT_LOG_CLEARED,          "Mbus event log profile cleared");

        private final int eventIndex;
        private final int eiCode;
        private final String message;

        DSMR23Event(int eventIndex, int eiCode, String message) {
            this.eventIndex=eventIndex;
            this.eiCode = eiCode;
            this.message = message;
        }

        public static DSMR23Event forMBUSClientAndProtocolCode(MBUSClient mbusClient, int protocolCode) {
            if (protocolCode == LOGBOOK_CLEARED.getEventIndex()) {
                return LOGBOOK_CLEARED;
            }
            for (DSMR23Event event : values()) {
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

    public enum MBUSClient {
        CLIENT1(1,100),
        CLIENT2(2,110),
        CLIENT3(3,120),
        CLIENT4(4,130);

        private final int channel;
        private final int startIndex;

        MBUSClient(int channel, int startIndex) {
            this.channel=channel;
            this.startIndex=startIndex;
        }

        public static MBUSClient forChannel(int channel) {
            for (MBUSClient mbusClient : values()) {
                if (mbusClient.getChannel()==channel) {
                    return mbusClient;
                }
            }
            return null;
        }

        public int getStartIndex() {
            return startIndex;
        }

        public int getChannel() {
            return channel;
        }
    }

    protected int mBusChannel;
    protected int ignoredEvents;

    public DSMR23MbusEventLog(DataContainer dc, int mBusChannel) {
        super(dc);
        this.mBusChannel = mBusChannel;
        this.ignoredEvents = 0;
    }

    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int protocolCode) {
        MBUSClient mbusClient = MBUSClient.forChannel(mBusChannel);
        if (mbusClient!=null) {
            DSMR23Event dsmr23event = DSMR23Event.forMBUSClientAndProtocolCode(mbusClient, protocolCode);
            if (dsmr23event != null) {
                meterEvents.add(createNewMbusEventLogbookEvent(eventTimeStamp, dsmr23event.getEiCode(), protocolCode, dsmr23event.getMessage(mbusClient)));
            }
            else {
                ignoredEvents++;
            }
        }
    }

    public MeterEvent createNewMbusEventLogbookEvent(Date eventTimeStamp, int eiCode, int protocolCode, String message) {
        return new MeterEvent(eventTimeStamp, eiCode, protocolCode, message, EventLogbookId.MbusEventLogbook.eventLogId(), 0);
    }

    public int getIgnoredEvents() {
        return ignoredEvents;
    }
}
