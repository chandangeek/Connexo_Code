package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.v210;

import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 16-mei-2011
 * Time: 11:56:28
 */
public class OverSpeedAlarmInfo {

    private int flowRatePerSecond; //In number of pulses
    private Date date;

    public OverSpeedAlarmInfo(Date date, int flowRatePerSecond) {
        this.date = date;
        this.flowRatePerSecond = flowRatePerSecond;
    }

    public Date getDate() {
        return date;
    }

    public int getFlowRatePerSecond() {
        return flowRatePerSecond;
    }
}
