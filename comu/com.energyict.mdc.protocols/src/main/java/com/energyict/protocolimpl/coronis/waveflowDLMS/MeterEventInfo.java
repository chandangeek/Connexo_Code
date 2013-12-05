package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 16/05/13
 * Time: 14:36
 * Author: khe
 */
public class MeterEventInfo {

    private int eiServerCode;
    private int protocolCode;
    private String description;

    public MeterEventInfo(int eiServerCode, int protocolCode, String description) {
        this.eiServerCode = eiServerCode;
        this.protocolCode = protocolCode;
        this.description = description;
    }

    public MeterEvent getMeterEvent() {
        return new MeterEvent(new Date(), eiServerCode, protocolCode, description);
    }
}
