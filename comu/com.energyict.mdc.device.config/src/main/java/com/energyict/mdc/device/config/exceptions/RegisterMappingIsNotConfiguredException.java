package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.RegisterMapping;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to add a {@link ChannelSpec} to a {@link com.energyict.mdc.device.config.DeviceConfiguration}
 * which is linked to a {@link RegisterMapping} which is not configured on the correct object.
 * <p/>
 * Copyrights EnergyICT
 * Date: 07/02/14
 * Time: 09:21
 */
public class RegisterMappingIsNotConfiguredException extends LocalizedException {

    /**
     * Creates a new RegisterMappingIsNotConfiguredException that models the
     * exceptional situation that occurs when an attempt is made to add a
     * {@link ChannelSpec} to a {@link com.energyict.mdc.device.config.DeviceConfiguration}
     * which is linked to a {@link RegisterMapping} which is not configured in the
     * linked {@link LoadProfileSpec}
     *
     * @param thesaurus       The Thesaurus
     * @param channelSpec     The ChannelSpec which will be added
     * @param loadProfileSpec The LoadProfileSpec which is linked to the ChannelSpec
     * @param registerMapping The RegisterMapping which is linked to the ChannelSpec but NOT to the LoadProfileSpec
     * @return the newly create RegisterMappingIsNotConfiguredException
     */
    public static RegisterMappingIsNotConfiguredException forChannelInLoadProfileSpec(Thesaurus thesaurus, LoadProfileSpec loadProfileSpec, RegisterMapping registerMapping, ChannelSpec channelSpec) {
        return new RegisterMappingIsNotConfiguredException(thesaurus, MessageSeeds.CHANNEL_SPEC_REGISTER_MAPPING_IS_NOT_IN_LOAD_PROFILE_SPEC, channelSpec.getName(), registerMapping.getName(), loadProfileSpec.getObisCode());
    }

    /**
     * Creates a new RegisterMappingIsNotConfiguredException that models the
     * exceptional situation that occurs when an attempt is made to add a
     * {@link ChannelSpec} to a {@link com.energyict.mdc.device.config.DeviceConfiguration}
     * which is linked to a {@link RegisterMapping} which is not configured in the
     * linked {@link DeviceType}
     *
     * @param thesaurus       The Thesaurus
     * @param channelSpec     The ChannelSpec which will be added
     * @param registerMapping The RegisterMapping which is linked to the ChannelSpec but NOT to the LoadProfileSpec
     * @param deviceType      The DeviceType which is owner of the {@link com.energyict.mdc.device.config.DeviceConfiguration} to which the ChannelSpec will be added
     * @return RegisterMappingIsNotConfiguredException
     */
    public static RegisterMappingIsNotConfiguredException forChannelInDeviceType(Thesaurus thesaurus, ChannelSpec channelSpec, RegisterMapping registerMapping, DeviceType deviceType) {
        return new RegisterMappingIsNotConfiguredException(thesaurus, MessageSeeds.CHANNEL_SPEC_REGISTER_MAPPING_IS_NOT_ON_DEVICE_TYPE, channelSpec.getName(), registerMapping.getName(), deviceType.getName());
    }

    private RegisterMappingIsNotConfiguredException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

}