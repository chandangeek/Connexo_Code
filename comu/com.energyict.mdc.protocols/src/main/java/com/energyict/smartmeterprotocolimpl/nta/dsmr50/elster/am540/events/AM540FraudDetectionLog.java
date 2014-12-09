package com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.events;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.FraudDetectionLog;

import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 5/06/2014 - 13:51
 */
public class AM540FraudDetectionLog extends FraudDetectionLog {

    protected static final int CONFIG_CHANGE = 47;

    public AM540FraudDetectionLog(DataContainer dc, AXDRDateTimeDeviationType deviationType) {
        super(dc, deviationType);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case EVENT_TIMES_WRONG_PASSWORD: {
                meterEvents.add(createNewFraudDetectionLogbookEvent(eventTimeStamp, MeterEvent.N_TIMES_WRONG_PASSWORD, eventId, "Association authentication failure (n time failed authentication)"));
            }
            break;
            case CONFIG_CHANGE: {
                meterEvents.add(createNewFraudDetectionLogbookEvent(eventTimeStamp, MeterEvent.CONFIGURATIONCHANGE, eventId, "Configuration change"));
            }
            break;
            default:
                super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
    }
}