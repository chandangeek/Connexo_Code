package com.energyict.smartmeterprotocolimpl.nta.dsmr23.xemex.events;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.protocol.MeterEvent;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.EventsLog;

import java.util.Date;
import java.util.List;

/**
 * @author sva
 * @since 19/03/14 - 14:35
 */
public class XemeWatchTalkStandardEventLog extends EventsLog {

    private static final int EVENT_UNSUPPORTED_LOGICAL_DEVICE_NAME = 230;
    private static final int EVENT_CONNECTION_FAILED = 231;
    private static final int EVENT_DECRYPT_SUCCESSFUL_MBUS_CHANNEL1 = 232;
    private static final int EVENT_DECRYPT_SUCCESSFUL_MBUS_CHANNEL2 = 233;
    private static final int EVENT_DECRYPT_SUCCESSFUL_MBUS_CHANNEL3 = 234;
    private static final int EVENT_DECRYPT_SUCCESSFUL_MBUS_CHANNEL4 = 235;
    private static final int EVENT_DECRYPT_FAILED_MBUS_CHANNEL1 = 236;
    private static final int EVENT_DECRYPT_FAILED_MBUS_CHANNEL2 = 237;
    private static final int EVENT_DECRYPT_FAILED_MBUS_CHANNEL3 = 238;
    private static final int EVENT_DECRYPT_FAILED_MBUS_CHANNEL4 = 239;


    public XemeWatchTalkStandardEventLog(DataContainer dc, AXDRDateTimeDeviationType deviationType) {
        super(dc, deviationType);
    }

    @Override
    protected void buildMeterEvent(final List<MeterEvent> meterEvents, final Date eventTimeStamp, final int eventId) {
        switch (eventId) {
            case EVENT_UNSUPPORTED_LOGICAL_DEVICE_NAME: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.HARDWARE_ERROR, eventId, "Unsupported logical device name"));
            }
            break;
            case EVENT_CONNECTION_FAILED: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Connection failed"));
            }
            case EVENT_DECRYPT_SUCCESSFUL_MBUS_CHANNEL1: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.COMMUNICATION_OK_MBUS, eventId, "Decrypt successful Mbus channel 1"));
            }
            case EVENT_DECRYPT_SUCCESSFUL_MBUS_CHANNEL2: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.COMMUNICATION_OK_MBUS, eventId, "Decrypt successful Mbus channel 2"));
            }
            case EVENT_DECRYPT_SUCCESSFUL_MBUS_CHANNEL3: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.COMMUNICATION_OK_MBUS, eventId, "Decrypt successful Mbus channel 3"));
            }
            case EVENT_DECRYPT_SUCCESSFUL_MBUS_CHANNEL4: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.COMMUNICATION_OK_MBUS, eventId, "Decrypt successful Mbus channel 4"));
            }
            case EVENT_DECRYPT_FAILED_MBUS_CHANNEL1: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.COMMUNICATION_ERROR_MBUS, eventId, "Decrypt failed Mbus channel 1"));
            }
            case EVENT_DECRYPT_FAILED_MBUS_CHANNEL2: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.COMMUNICATION_ERROR_MBUS, eventId, "Decrypt failed Mbus channel 2"));
            }
            case EVENT_DECRYPT_FAILED_MBUS_CHANNEL3: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.COMMUNICATION_ERROR_MBUS, eventId, "Decrypt failed Mbus channel 3"));
            }
            case EVENT_DECRYPT_FAILED_MBUS_CHANNEL4: {
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.COMMUNICATION_ERROR_MBUS, eventId, "Decrypt failed Mbus channel 4"));
            }
            break;
            default:
                super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
    }
}
