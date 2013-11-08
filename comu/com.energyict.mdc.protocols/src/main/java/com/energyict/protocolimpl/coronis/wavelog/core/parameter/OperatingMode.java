package com.energyict.protocolimpl.coronis.wavelog.core.parameter;

import com.energyict.protocolimpl.coronis.wavelog.WaveLog;

import java.io.IOException;

public class OperatingMode extends AbstractParameter {

    OperatingMode(WaveLog waveLog) {
        super(waveLog);
    }

    public OperatingMode(WaveLog waveLog, int opMode) {
        super(waveLog);
        this.operationMode = opMode;
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.OperationMode;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        this.operationMode = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) operationMode};
    }

    public boolean isAlarmOnEndOfBatteryLife() {
        return (operationMode & 0x01) == 0x01;
    }

    public boolean isPeriodicTransmission() {
        return (operationMode & 0x02) == 0x02;
    }

    public boolean isStorageOfEvents() {
        return (operationMode & 0x04) == 0x04;
    }

    /**
     * Enables monitoring (every 100 ms) of the inputs.
     * An alarm frame is sent on event.
     * @return
     */
    public boolean isTransmissionOfAlarmFramesOnEvent() {
        return (operationMode & 0x08) == 0x08;
    }

    public void setAlarmOnEndOfBatteryLife() {
        operationMode =  operationMode | 0x01;
    }

    public void setPeriodicTransmission() {
        operationMode =  operationMode | 0x02;
    }

    public void setStorageOfEvents() {
        operationMode =  operationMode | 0x04;
    }

    public void setTransmissionOfAlarmFramesOnEvent() {
        operationMode = operationMode | 0x08;
    }
}