package com.energyict.protocolimpl.base;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 24/02/12
 * Time: 14:15
 */
public interface MeterEventMapper {

    MeterEvent getMeterEvent(Date eventTime, int meterEventCode);

    public int getEisEventCode(int meterEventCode);

    String getEventMessage(int meterEventCode);

}
