package com.energyict.protocolimpl.dlms;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 24/02/12
 * Time: 15:16
 */
public abstract class DefaultDLMSMeterEventMapper implements DLMSMeterEventMapper {

    public MeterEvent getMeterEvent(Date eventTime, int meterEventCode, int eventLogId, final List<AbstractDataType> capturedObjects) {
        return new MeterEvent(
                eventTime,
                getEisEventCode(meterEventCode),
                meterEventCode,
                getEventMessage(meterEventCode),
                eventLogId,
                0
        );
    }

    protected abstract int getEisEventCode(final int meterEventCode);

    protected abstract String getEventMessage(final int meterEventCode);


}
