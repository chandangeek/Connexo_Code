package com.energyict.protocolimpl.dlms;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.protocol.MeterEvent;

import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 24/02/12
 * Time: 14:15
 */
public interface DLMSMeterEventMapper {

    MeterEvent getMeterEvent(Date eventTime, int meterEventCode, final int eventGroup, final List<AbstractDataType> capturedObjects);


}
