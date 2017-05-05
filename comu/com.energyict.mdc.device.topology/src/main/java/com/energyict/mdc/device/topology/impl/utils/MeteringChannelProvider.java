package com.energyict.mdc.device.topology.impl.utils;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.topology.impl.DataLoggerLinkException;

import java.util.Optional;

/**
 * Getting the {@link com.elster.jupiter.metering.Channel} for a {@link }given com.energyict.mdc.device.data.Channel} or {@link com.energyict.mdc.device.data.Register}
 * Copyrights EnergyICT
 * Date: 16/03/2017
 * Time: 16:09
 */
public class MeteringChannelProvider {

    private Thesaurus thesaurus;

    public MeteringChannelProvider(Thesaurus thesaurus){
       this.thesaurus = thesaurus;
    }

    public Optional<Channel> getMeteringChannel(final com.energyict.mdc.device.data.Channel channel) {
        return channel.getDevice().getCurrentMeterActivation().map(meterActivation -> getMeteringChannel(channel, meterActivation));
    }

    public com.elster.jupiter.metering.Channel getMeteringChannel(final com.energyict.mdc.device.data.Channel channel, final MeterActivation meterActivation) {
        return meterActivation.getChannelsContainer().getChannels()
                .stream()
                .filter(meterActivationChannel -> meterActivationChannel.getReadingTypes().contains(channel.getReadingType()))
                .findFirst()
                .orElseThrow(() -> DataLoggerLinkException.noPhysicalChannelForReadingType(this.thesaurus, channel.getReadingType()));
    }

    public Optional<com.elster.jupiter.metering.Channel> getMeteringChannel(final Register register) {
        return register.getDevice().getCurrentMeterActivation().map(meterActivation -> getMeteringChannel(register, meterActivation));
    }

    public com.elster.jupiter.metering.Channel getMeteringChannel(final Register register, final MeterActivation meterActivation) {
        return meterActivation.getChannelsContainer().getChannels().stream().filter((x) -> x.getReadingTypes().contains(register.getReadingType()))
                .findFirst()
                .orElseThrow(() -> DataLoggerLinkException.noPhysicalChannelForReadingType(this.thesaurus, register.getReadingType()));
    }
}
