package com.elster.jupiter.demo.impl.builders.configuration;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.masterdata.ChannelType;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ChannelsOnDevConfPostBuilder implements Consumer<DeviceConfiguration>{

    @Override
    public void accept(DeviceConfiguration configuration) {
        List<String> channelsOnConfiguration = configuration.getChannelSpecs().stream()
                .map(spec -> spec.getChannelType().getReadingType().getMRID()).collect(Collectors.toList());
        for (LoadProfileSpec loadProfileSpec : configuration.getLoadProfileSpecs()) {
            List<ChannelType> availableChannelTypes = loadProfileSpec.getLoadProfileType().getChannelTypes();
            for (ChannelType channelType : availableChannelTypes) {
                if (!channelsOnConfiguration.contains(channelType.getReadingType().getMRID())) {
                    configuration.createChannelSpec(channelType, loadProfileSpec)
                            .setMultiplier(new BigDecimal(1))
                            .setOverflow(new BigDecimal(9999999999L))
                            .setNbrOfFractionDigits(0)
                            .add();
                }
            }
        }
    }
}
