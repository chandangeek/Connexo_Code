/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.axrdencoding.AbstractDataType;

import java.util.Date;
import java.util.List;

public interface DLMSMeterEventMapper {

    MeterEvent getMeterEvent(Date eventTime, int meterEventCode, final int eventGroup, final List<AbstractDataType> capturedObjects);


}
