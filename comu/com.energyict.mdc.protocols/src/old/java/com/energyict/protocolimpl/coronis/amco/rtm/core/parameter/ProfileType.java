/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.protocol.api.UnsupportedException;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.RTMFactory;

import java.io.IOException;

public class ProfileType extends AbstractParameter {

    private final static int DIGITAL_PORTS = 0x01;
    private final static int ENCODER_PORTS = 0x02;
    private final static int EVOHOP = 0x03;
    private final static int DIGITAL_AND_VALVE_PORTS = 0x04;
    private final static int ENCODER_AND_VALVE_PORTS  = 0x05;

    public ProfileType(RTM rtm) {
        super(rtm);
    }

    public int getProfile() {
        return profile;
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.ProfileType;
    }

    private int profile = 0x01;

    @Override
    public void parse(byte[] data, RTMFactory rtmFactory) throws IOException {
        this.profile = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        throw new UnsupportedException("Not allowed to write this parameter");
    }

    public boolean isDigitialPorts() {
        return profile == DIGITAL_PORTS;
    }

    public boolean isEncoderPorts() {
        return profile == ENCODER_PORTS;
    }

    public boolean isEvoHop() {
        return profile == EVOHOP;
    }

    public boolean isDigitalAndValvePorts() {
        return profile == DIGITAL_AND_VALVE_PORTS;
    }

    public boolean isEncoderAndValvePorts() {
        return profile == ENCODER_AND_VALVE_PORTS;
    }

    public boolean isPulse() {
        return isDigitalAndValvePorts() || isDigitialPorts();
    }

    public boolean isEncoder() {
        return isEncoderAndValvePorts() || isEncoderPorts();
    }

    public String getDescription() {
        if (isDigitalAndValvePorts()) {
            return "1 Digital + 1 Valve Ports";
        }
        if (isDigitialPorts()) {
            return "1 to 4 Digital Ports";
        }
        if (isEncoderPorts()) {
            return "1 or 2 Encoder Ports";
        }
        if (isEvoHop()) {
            return "evoHop";
        }
        if (isEncoderAndValvePorts()) {
            return "1 Encoder + 1 Valve Ports";
        }
        return "";
    }

    public boolean isValve() {
        return isDigitalAndValvePorts() || isEncoderAndValvePorts();
    }
}