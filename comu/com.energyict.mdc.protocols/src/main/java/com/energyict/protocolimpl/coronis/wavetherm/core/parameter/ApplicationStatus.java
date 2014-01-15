package com.energyict.protocolimpl.coronis.wavetherm.core.parameter;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.coronis.wavetherm.WaveTherm;

import java.io.IOException;

public class ApplicationStatus extends AbstractParameter {

    int status;

    ApplicationStatus(WaveTherm waveTherm) {
        super(waveTherm);
    }

    public final int getStatus() {
        return status;
    }

    public final void setStatus(int status) {
        this.status = status;
    }

    @Override
    protected ParameterId getParameterId() {
        return ParameterId.ApplicationStatus;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        status = ProtocolUtils.getInt(data, 0, 1);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) status};
    }

    public boolean resetDetected() {
        return (status & 0x80) == 0x80;
    }

    public boolean lowThresholdOnSensor2() {
        return (status & 0x40) == 0x40;
    }

    public boolean highThresholdOnSensor2() {
        return (status & 0x20) == 0x20;
    }

    public boolean lowThresholdOnSensor1() {
        return (status & 0x10) == 0x10;
    }

    public boolean highThresholdOnSensor1() {
        return (status & 0x08) == 0x08;
    }

    public int getNumberOfSensors() {
        return ((status & 0x04) >> 2) + 1;         //b2 = 0 ==> 1 sensor
    }                                              //b2 = 1 ==> 2 sensors

    public boolean endOfBatteryLifeDetected() {
        return (status & 0x01) == 0x01;
    }

    public void reset() {
        status = status & 0x04;        //Set every bit to zero, except the one indicating the number of sensors!
    }
}