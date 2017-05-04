/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex.eventhandling;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.eventhandling.FraudDetectionLog;

import java.util.Date;
import java.util.List;

/**
 * @author sva
 * @since 25/02/13 - 15:12
 */
public class XemexFraudDetectionLog extends FraudDetectionLog {

    private static final int EVENT_CONFIGURATION_CHANGE = 47;

    public XemexFraudDetectionLog(DataContainer dc, AXDRDateTimeDeviationType deviationType) {
        super(dc, deviationType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void buildMeterEvent(final List<MeterEvent> meterEvents, final Date eventTimeStamp, final int eventId) {
        switch (eventId) {
            case EVENT_CONFIGURATION_CHANGE: {
                meterEvents.add(createNewFraudDetectionLogbookEvent(eventTimeStamp, MeterEvent.CONFIGURATIONCHANGE, eventId, "Configuration changed during P3 communication"));
            }
            break;
            default:
                super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
    }
}
