/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.nta.dsmr40.eventhandling;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;

import java.util.Date;
import java.util.List;

/**
 * Extends the original DSMR2.3 FraudDetectionLog with additional events for DSMR4.0
 */
public class FraudDetectionLog extends com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.FraudDetectionLog{

    private static final int EVENT_CONFIGURATION_CHANGE = 47;

    public FraudDetectionLog(DataContainer dc, AXDRDateTimeDeviationType deviationType) {
        super(dc, deviationType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void buildMeterEvent(final List<MeterEvent> meterEvents, final Date eventTimeStamp, final int eventId) {
        switch(eventId){
            case EVENT_CONFIGURATION_CHANGE : {meterEvents.add(createNewFraudDetectionLogbookEvent(eventTimeStamp, MeterEvent.CONFIGURATIONCHANGE, eventId, "Configuration is activated or de-activated after the meter was installed"));}break;
            default: super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
    }
}
