package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.coronis.amco.rtm.RTM;

import java.io.IOException;

public class PseudoBubbleUpMaxCancellationTimeout extends AbstractParameter {

    private int seconds;

    PseudoBubbleUpMaxCancellationTimeout(PropertySpecService propertySpecService, RTM rtm) {
        super(propertySpecService, rtm);
    }

    final int getSeconds() {
        return seconds;
    }

    final void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.MaxCancelTimeout;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        seconds = ProtocolUtils.getInt(data, 0, 1);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) seconds};
    }
}
