package com.energyict.protocolimpl.coronis.wavetherm.core.parameter;

import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.wavetherm.WaveTherm;

import java.io.IOException;

public class DataLoggingStartHourForPeriodicSteps extends AbstractParameter {

    DataLoggingStartHourForPeriodicSteps(WaveTherm waveTherm) {
        super(waveTherm);
    }


    /**
     * start hour when the datalogging has to start.
     */
    int startHour = 0;


    final int getStartHour() {
        return startHour;
    }

    final void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.SamplingActivationStartHour;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        startHour = WaveflowProtocolUtils.toInt(data[0]);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) getStartHour()};
    }
}