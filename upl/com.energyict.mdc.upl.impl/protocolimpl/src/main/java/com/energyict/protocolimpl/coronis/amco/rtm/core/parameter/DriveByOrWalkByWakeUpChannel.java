package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 7-apr-2011
 * Time: 16:45:00
 */
public class DriveByOrWalkByWakeUpChannel extends AbstractParameter {

    DriveByOrWalkByWakeUpChannel(PropertySpecService propertySpecService, RTM rtm, NlsService nlsService) {
        super(propertySpecService, rtm, nlsService);
    }

    private int channel;

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.WalkByOrDriveByWakeUpChannel;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        channel = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) channel};
    }
}
