package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.RTMFactory;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.IOException;

public class SamplingActivationType extends AbstractParameter {

    public SamplingActivationType(RTM rtm) {
        super(rtm);
    }

    /**
     * start hour when the data logging has to start. (in periodic step mode)
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
        return ParameterId.SamplingActivationType;
    }

    @Override
    public void parse(byte[] data, RTMFactory rtmFactory) throws IOException {
        startHour = WaveflowProtocolUtils.toInt(data[0]);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) getStartHour()};
    }
}
