/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.base;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.Date;

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
