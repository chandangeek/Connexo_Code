package com.energyict.protocolimpl.coronis.wavelog.core.parameter;

import com.energyict.protocolimpl.coronis.wavelog.WaveLog;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

public class ApplicationStatus extends AbstractParameter {

    int status;

    public ApplicationStatus(WaveLog waveLog) {
        super(waveLog);
    }

    public ApplicationStatus(WaveLog waveLog, int status) {
        super(waveLog);
        this.status = status;
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

    public boolean endOfBatteryLifeDetected() {
        return (status & 0x01) == 0x01;
    }

    public boolean inputsAcquisitionDiscontinuityDetected() {
        return (status & 0x02) == 0x02;
    }

    public boolean inputConflictDetected() {
        return (status & 0x40) == 0x40;
    }

    public boolean resetDetected() {
        return (status & 0x80) == 0x80;
    }

    public void reset() {
        status = 0x00;
    }
}