package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 11-apr-2011
 * Time: 11:51:26
 */
public class AlarmWindowConfiguration extends AbstractParameter {

    AlarmWindowConfiguration(PropertySpecService propertySpecService, RTM rtm, NlsService nlsService) {
        super(propertySpecService, rtm, nlsService);
    }

    AlarmWindowConfiguration(PropertySpecService propertySpecService, RTM rtm, int durationInSeconds, boolean mechanismActivation, int granularityInMinutes, NlsService nlsService) {
        super(propertySpecService, rtm, nlsService);
        switch (granularityInMinutes) {
            case 15:
                granularity = 0;
            case 30:
                granularity = 1;
            case 60:
                granularity = 2;
        }
        switch (durationInSeconds) {
            case 30:
                duration = 0;
            case 45:
                duration = 1;
            case 60:
                duration = 2;
            case 90:
                duration = 3;
            case 120:
                duration = 4;
        }
        activation = mechanismActivation ? 1 : 0;
    }

    private int config;
    private int duration = 4;
    private int activation = 1;
    private int granularity = 2;

    public int getConfig() {
        return config;
    }

    public int getActivation() {
        return activation;
    }

    public void setActivation(int activation) {
        this.activation = activation;
    }

    public int getDuration() {
        switch (duration) {
            case 0:
                return 30;
            case 1:
                return 45;
            case 2:
                return 60;
            case 3:
                return 90;
            default:
                return 120;
        }
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getGranularity() {
        switch (granularity) {
            case 0:
                return 15;
            case 1:
                return 30;
            default:
                return 60;
        }
    }

    public void setGranularity(int granularity) {
        this.granularity = granularity;
    }

    @Override
    ParameterId getParameterId() throws WaveFlowException {
        return ParameterId.AlarmWindowConfiguration;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        this.config = data[0] & 0xFF;                                  //TODO test
        activation = config & 0x01;
        duration = (config & 0x1C) >> 2;
        granularity = (config & 0xE0) >> 5;
    }

    @Override
    protected byte[] prepare() throws IOException {
        config = 0x00;
        config = config | activation;                       //b0 indicates activation
        config = config | (duration << 2);                  //b4 b3 b2 represent the duration
        config = config | (granularity << 5);               //b7 b6 b5 represent the granularity
        return new byte[]{(byte) config};
    }
}