/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.v210;

import java.util.Date;

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
