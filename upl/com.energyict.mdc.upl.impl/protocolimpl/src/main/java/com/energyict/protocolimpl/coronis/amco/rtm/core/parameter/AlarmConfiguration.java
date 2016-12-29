package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;

import java.io.IOException;

public class AlarmConfiguration extends AbstractParameter {

    private int config;

    public AlarmConfiguration(PropertySpecService propertySpecService, RTM rtm) {
        super(propertySpecService, rtm);
    }

    public AlarmConfiguration(PropertySpecService propertySpecService, RTM rtm, int status) {
        super(propertySpecService, rtm);
        this.config = status;
    }

    public final int getConfig() {
        return config;
    }

    public final void setConfig(int config) {
        this.config = config;
    }

    @Override
    protected ParameterId getParameterId() {
        return ParameterId.AlarmConfiguration;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        config = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) config};
    }

    public void setAlarmOnDefaultValve(int enable) {
        config = config & 0xBF;                    //Set b6 to 0
        config = config | (0x40 * enable);         //Set b6 to [enable]
    }

    public void setAlarmOnBackFlow(int enable) {
        config = config & 0xDF;
        config = config | (0x20 * enable);
    }

    public void setAlarmOnEncoderMisread(int enable) {
        config = config & 0xEF;
        config = config | (0x10 * enable);
    }

    public void setAlarmOnHighThreshold(int enable) {
        config = config & 0xF7;
        config = config | (0x08 * enable);
    }

    public void setAlarmOnLowThreshold(int enable) {
        config = config & 0xFB;
        config = config | (0x04 * enable);
    }

    public void setAlarmOnLowBattery(int enable) {
        config = config & 0xFD;
        config = config | (0x02 * enable);
    }

    public void setAlarmOnCutCable(int enable) {
        config = config & 0xFE;
        config = config | (0x01 * enable);
    }

    public void setAlarmOnEncoderCommunicationFailure(int enable) {
        config = config & 0xFE;
        config = config | (0x01 * enable);
    }

    public void setAlarmOnCutRegisterCable(int enable) {
        config = config & 0xFE;
        config = config | (0x01 * enable);
    }

    public void enableAllAlarms() throws IOException {
        ProfileType profileType = getRTM().getParameterFactory().readProfileType();
        if (profileType.isDigitialPorts()) {
            config = 0x0F;
        } else if (profileType.isEncoderPorts()) {
            config = 0x3F;
        } else if (profileType.isEvoHop()) {
            config = 0x02;
        } else if (profileType.isDigitalAndValvePorts()) {
            config = 0x4F;
        } else if (profileType.isEncoderAndValvePorts()) {
            config = 0x7F;
        }
    }
}