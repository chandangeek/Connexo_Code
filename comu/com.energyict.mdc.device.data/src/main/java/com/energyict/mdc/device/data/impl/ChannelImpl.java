package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.LoadProfile;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfileChannel;

/**
 * Provides an implementation of a Channel of a {@link com.energyict.mdc.device.data.Device},
 * which is actually a wrapping around a {@link com.energyict.mdc.device.config.ChannelSpec}
 * of the {@link com.energyict.mdc.device.config.DeviceConfiguration}
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/17/14
 * Time: 2:17 PM
 */
public class ChannelImpl implements Channel {

    /**
     * The {@link com.energyict.mdc.device.config.ChannelSpec} for which this channel is serving
     */
    private final ChannelSpec channelSpec;
    /**
     * The Device which <i>owns</i> this Register
     */
    private final Device device;

    public ChannelImpl(ChannelSpec channelSpec, Device device) {
        this.channelSpec = channelSpec;
        this.device = device;
    }

    @Override
    public int getIntervalInSeconds() {
        return channelSpec.getInterval().getSeconds();
    }

    @Override
    public BaseDevice getDevice() {
        return this.device;
    }

    @Override
    public Unit getUnit() {
        return this.channelSpec.getPhenomenon().getUnit();
    }

    @Override
    public LoadProfile getLoadProfile() {
        //TODO move LoadProfile
        return null;
    }

    @Override
    public ObisCode getRegisterTypeObisCode() {
        return this.channelSpec.getRegisterMapping().getObisCode();
    }

    @Override
    public OfflineLoadProfileChannel goOffline() {
        return null;
    }
}
