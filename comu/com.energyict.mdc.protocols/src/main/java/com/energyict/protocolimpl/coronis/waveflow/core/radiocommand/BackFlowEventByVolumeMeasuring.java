/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand;

import java.util.Date;

public class BackFlowEventByVolumeMeasuring {

    private Date endOfDetectionDate;
    private Date startOfDetectionDate;
    private int volume;
    private int inputIndex;

    public BackFlowEventByVolumeMeasuring(int inputIndex, int volume, Date startOfDetectionDate, Date endOfDetectionDate) {
        this.inputIndex = inputIndex;
        this.volume = volume;
        this.startOfDetectionDate = startOfDetectionDate;
        this.endOfDetectionDate = endOfDetectionDate;
    }

    public Date getEndOfDetectionDate() {
        return endOfDetectionDate;
    }

    public int getInputIndex() {
        return inputIndex;
    }

    public Date getStartOfDetectionDate() {
        return startOfDetectionDate;
    }

    public int getVolume() {
        return volume;
    }
}