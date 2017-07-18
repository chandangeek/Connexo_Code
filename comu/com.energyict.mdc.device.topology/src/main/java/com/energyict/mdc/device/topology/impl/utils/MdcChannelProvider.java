package com.energyict.mdc.device.topology.impl.utils;

import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;

import java.util.Optional;

/**
 * Getting the {@link }given com.energyict.mdc.device.data.Channel} for a {@link com.elster.jupiter.metering.Channel}
 * Copyrights EnergyICT
 * Date: 16/03/2017
 * Time: 16:47
 */
public class MdcChannelProvider {

    public Optional<Channel> getChannel(Device device, com.elster.jupiter.metering.Channel channel) {
        return device.getChannels().stream().filter(mdcChannel -> channel.getReadingTypes().contains(mdcChannel.getReadingType())).findFirst();
    }
}
