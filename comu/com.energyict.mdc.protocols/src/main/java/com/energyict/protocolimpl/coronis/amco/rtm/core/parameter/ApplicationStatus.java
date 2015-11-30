package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.RTMFactory;

import java.io.IOException;

public class ApplicationStatus extends AbstractParameter {

    int status;

    public ApplicationStatus(RTM rtm) {
        super(rtm);
    }

    public ApplicationStatus(RTM rtm, int status) {
        super(rtm);
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
    protected void parse(byte[] data, RTMFactory rtmFactory) throws IOException {
        status = ProtocolUtils.getInt(data, 0, 1);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) status};
    }

    public boolean isLowBatteryWarning() {
        return (status & 0x01) == 0x01;
    }

    //Pulse specific flags
    public boolean isTamperDetectionOnPortA() {
        return (status & 0x02) == 0x02;
    }

    public boolean isTamperDetectionOnPortB() {
        return (status & 0x04) == 0x04;
    }

    public boolean isTamperDetectionOnPortC() {
        return (status & 0x08) == 0x08;
    }

    public boolean isTamperDetectionOnPortD() {
        return (status & 0x10) == 0x10;
    }


    //Encoder specific flags
    public boolean isEncoderCommFaultOnPortA() {
        return (status & 0x02) == 0x02;
    }

    public boolean isEncoderCommFaultOnPortB() {
        return (status & 0x04) == 0x04;
    }

    public boolean isEncoderMisreadOnPortA() {
        return (status & 0x08) == 0x08;
    }

    public boolean isEncoderMisreadOnPortB() {
        return (status & 0x10) == 0x10;
    }

    public boolean isBackFlowOnPortA() {
        return (status & 0x20) == 0x20;
    }

    public boolean isBackFlowOnPortB() {
        return (status & 0x40) == 0x40;
    }

    public boolean isValveFault() {
        return (status & 0x40) == 0x40;
    }

    public boolean isLeakDetection() {
        return (status & 0x80) == 0x80;
    }

    public void reset() {
        status = 0x00;
    }
}