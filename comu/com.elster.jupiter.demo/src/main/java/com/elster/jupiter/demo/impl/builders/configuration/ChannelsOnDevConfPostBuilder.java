package com.elster.jupiter.demo.impl.builders.configuration;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.masterdata.ChannelType;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ChannelsOnDevConfPostBuilder implements Consumer<DeviceConfiguration> {

    private BigDecimal overflow = new BigDecimal(9999999999L);

    @Override
    public void accept(DeviceConfiguration configuration) {
        List<String> channelsOnConfiguration = configuration.getChannelSpecs().stream()
                .map(spec -> spec.getChannelType().getReadingType().getMRID()).collect(Collectors.toList());
        for (LoadProfileSpec loadProfileSpec : configuration.getLoadProfileSpecs()) {
            List<ChannelType> availableChannelTypes = loadProfileSpec.getLoadProfileType().getChannelTypes();
            availableChannelTypes.stream().filter(channelType -> !channelsOnConfiguration.contains(channelType.getReadingType().getMRID())).forEach(channelType ->
                    configuration.createChannelSpec(channelType, loadProfileSpec)
                            .overflow(overflow)
                            .nbrOfFractionDigits(0)
                            .add()
            );
        }
    }
}
