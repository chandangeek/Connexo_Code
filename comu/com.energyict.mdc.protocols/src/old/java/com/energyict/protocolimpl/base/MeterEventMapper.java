/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.base;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.Date;

public interface MeterEventMapper {

    MeterEvent getMeterEvent(Date eventTime, int meterEventCode);

    public int getEisEventCode(int meterEventCode);

    String getEventMessage(int meterEventCode);

}
