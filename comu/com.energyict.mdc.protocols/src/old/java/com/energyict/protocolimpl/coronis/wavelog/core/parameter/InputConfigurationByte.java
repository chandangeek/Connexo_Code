package com.energyict.protocolimpl.coronis.wavelog.core.parameter;


import com.energyict.protocolimpl.coronis.wavelog.WaveLog;

import java.io.IOException;

public class InputConfigurationByte extends AbstractParameter {

    public InputConfigurationByte(WaveLog waveLog) {
        super(waveLog);
    }

    public InputConfigurationByte(WaveLog waveLog, int input) {
        super(waveLog);
        this.input = input;
    }

    @Override
    ParameterId getParameterId() {
        switch (input) {
            case 1:
                return ParameterId.Input1ConfigByte;
            case 2:
                return ParameterId.Input2ConfigByte;
            case 3:
                return ParameterId.Input3ConfigByte;
            case 4:
                return ParameterId.Input4ConfigByte;
            default:
                return ParameterId.Input1ConfigByte;
        }
    }

    private int config;
    private int input;

    public int getConfig() {
        return config;
    }

    public void setConfig(int config) {
        this.config = config;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        config = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) config};
    }
}