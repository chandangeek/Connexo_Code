/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders.configuration;

import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.masterdata.ChannelType;

import com.energyict.obis.ObisCode;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ChannelsOnDevConfPostBuilder implements Consumer<DeviceConfiguration> {

    private BigDecimal overflow = new BigDecimal(9999999999L);
    private String overruledCode;

    @Override
    public void accept(DeviceConfiguration configuration) {
        List<String> channelsOnConfiguration = configuration.getChannelSpecs().stream()
                .map(spec -> spec.getChannelType().getReadingType().getMRID()).collect(Collectors.toList());
        for (LoadProfileSpec loadProfileSpec : configuration.getLoadProfileSpecs()) {
            List<ChannelType> availableChannelTypes = loadProfileSpec.getLoadProfileType().getChannelTypes();
            availableChannelTypes.stream().filter(channelType -> !channelsOnConfiguration.contains(channelType.getReadingType().getMRID())).forEach(channelType -> {
                        ChannelSpec.ChannelSpecBuilder builder = configuration.createChannelSpec(channelType, loadProfileSpec);
                        builder.overflow(overflow)
                                .nbrOfFractionDigits(0);
                        if (overruledCode != null) {
                            builder.overruledObisCode(ObisCode.fromString(overruledCode));
                        }
                        builder.add();
                    }
            );
        }
    }

    public void setOverruledObisCode(String overruledObisCode) {
        this.overruledCode = overruledObisCode;
    }
}
