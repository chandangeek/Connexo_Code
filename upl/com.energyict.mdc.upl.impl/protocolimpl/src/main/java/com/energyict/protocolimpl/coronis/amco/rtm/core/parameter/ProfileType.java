package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 7-apr-2011
 * Time: 14:58:21
 */
public class ProfileType extends AbstractParameter {

    private static final int DIGITAL_PORTS = 0x01;
    private static final int ENCODER_PORTS = 0x02;
    private static final int EVOHOP = 0x03;
    private static final int DIGITAL_AND_VALVE_PORTS = 0x04;
    private static final int ENCODER_AND_VALVE_PORTS  = 0x05;

    public ProfileType(PropertySpecService propertySpecService, RTM rtm, NlsService nlsService) {
        super(propertySpecService, rtm, nlsService);
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
    public void parse(byte[] data) throws IOException {
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