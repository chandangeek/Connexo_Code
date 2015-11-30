package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.RTMFactory;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 7-apr-2011
 * Time: 16:45:00
 */
public class DriveByOrWalkByWakeUpChannel extends AbstractParameter {

    DriveByOrWalkByWakeUpChannel(RTM rtm) {
        super(rtm);
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
    protected void parse(byte[] data, RTMFactory rtmFactory) throws IOException {
        channel = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) channel};
    }
}
