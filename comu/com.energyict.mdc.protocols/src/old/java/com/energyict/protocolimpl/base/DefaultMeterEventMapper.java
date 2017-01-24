package com.energyict.protocolimpl.base;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 24/02/12
 * Time: 15:16
 */
public abstract class DefaultMeterEventMapper implements MeterEventMapper {

    public MeterEvent getMeterEvent(Date eventTime, int meterEventCode) {
        return new MeterEvent(
                eventTime,
                getEisEventCode(meterEventCode),
                meterEventCode,
                getEventMessage(meterEventCode)
        );
    }
}
