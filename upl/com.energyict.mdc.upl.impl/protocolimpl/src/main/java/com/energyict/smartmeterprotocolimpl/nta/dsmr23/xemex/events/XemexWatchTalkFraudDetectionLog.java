package com.energyict.smartmeterprotocolimpl.nta.dsmr23.xemex.events;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.protocol.MeterEvent;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.FraudDetectionLog;

import java.util.Date;
import java.util.List;

/**
 * @author sva
 * @since 20/03/2014 - 14:02
 */
public class XemexWatchTalkFraudDetectionLog extends FraudDetectionLog {

    private static final int EVENT_CONFIGURATION_CHANGE = 47;
    private static final int EVENT_FAILED_LOGIN_ATTEMPT = 48;

    public XemexWatchTalkFraudDetectionLog(DataContainer dc, AXDRDateTimeDeviationType deviationType) {
        super(dc, deviationType);
    }

    @Override
    protected void buildMeterEvent(final List<MeterEvent> meterEvents, final Date eventTimeStamp, final int eventId) {
        switch (eventId) {
            case EVENT_CONFIGURATION_CHANGE: {
                meterEvents.add(createNewFraudDetectionLogbookEvent(eventTimeStamp, MeterEvent.CONFIGURATIONCHANGE, eventId, "Configuration changed during P3 communication"));
            }
            break;
            case EVENT_FAILED_LOGIN_ATTEMPT: {
                meterEvents.add(createNewFraudDetectionLogbookEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Failed login attempt"));
            }
            break;
            default:
                super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
    }
}
