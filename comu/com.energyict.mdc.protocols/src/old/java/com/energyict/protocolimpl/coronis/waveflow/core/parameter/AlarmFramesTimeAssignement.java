/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.parameter;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

import java.io.IOException;

public class AlarmFramesTimeAssignement extends AbstractParameter {

    int flag;

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    AlarmFramesTimeAssignement(WaveFlow waveFlow) {
        super(waveFlow);
    }

    @Override
    protected ParameterId getParameterId() {
        return ParameterId.AlarmFramesTimeAssignement;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        flag = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) flag};
    }

    /**
     * Set the time slot granularity.
     * @param minutes: 15 min, 30 min or 60 min
     */
    public void setTimeSlotGranularity(int minutes) {
        int mode = 0;
        if (minutes == 15) {
            mode = 0;
        }
        if (minutes == 30) {
            mode = 1;
        }
        if (minutes == 60) {
            mode = 2;
        }
        flag = flag & 0x1F;         //Reset the granularity bits (b7, b6, b5)
        flag = flag | mode << 5;    //Add the mode bits to the flag
    }

    public int getTimeSlotGranularity() {
        return (flag & 0xE0) >> 5;      //Return b7b6b5
    }

    public int getTimeSlotDuration() {
        return (flag & 0x1C) >> 2;      //Return b4b3b2
    }

    public int getTimeSlotActivation() {
        return flag & 0x01;
    }

    public void setTimeSlotDuration(int seconds) {
        int duration = 0;
        switch (seconds) {
            case 30:
                duration = 0;
                break;
            case 45:
                duration = 1;
                break;
            case 60:
                duration = 2;
                break;
            case 90:
                duration = 3;
                break;
            case 120:
                duration = 4;
                break;
        }
        flag = flag & 0xE3;             //Reset the duration bits (b4, b3, b2)
        flag = flag | duration << 2;    //Add the duration bits to the flag
    }

    public void enableTimeSlotMechanism() {
        flag = flag | 0x01;         //Set b0 to 1
    }
    
    public void disableTimeSlotMechanism() {
        flag = flag & 0xFE;         //Set b0 to 0
    }
}