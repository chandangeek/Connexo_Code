/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

import java.util.Calendar;
import java.util.Date;

public class BackFlowEventByFlowRate {

    private Date endDate;
    private int volume;
    private int inputIndex;
    private int detectionDuration;
    private int backflowDuration;
    private WaveFlow waveFlow;

    public BackFlowEventByFlowRate(WaveFlow waveFlow, int inputIndex, int volume, int detectionDuration, int backflowDuration, Date endDate) {
        this.waveFlow = waveFlow;
        this.inputIndex = inputIndex;
        this.volume = volume;
        this.detectionDuration = detectionDuration;
        this.backflowDuration = backflowDuration;
        this.endDate = endDate;
    }

    public int getBackflowDuration() {
        return backflowDuration;
    }

    public void setBackflowDuration(int backflowDuration) {
        this.backflowDuration = backflowDuration;
    }

    public int getDetectionDuration() {
        return detectionDuration;
    }

    public void setDetectionDuration(int detectionDuration) {
        this.detectionDuration = detectionDuration;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getInputIndex() {
        return inputIndex;
    }

    public void setInputIndex(int inputIndex) {
        this.inputIndex = inputIndex;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    /**
     * Calculates and returns the start date of the event, based on the duration and the end date of the event.
     * @return the start date of the event
     */
    public Date getStartDate() {
        Calendar cal = Calendar.getInstance(waveFlow.getTimeZone());
        cal.setTime(getEndDate());
        cal.setLenient(true);
        cal.add(Calendar.MINUTE, -1 * getDetectionDuration());
        return cal.getTime();
    }
}