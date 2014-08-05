package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.masterdata.MeasurementType;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to add a {@link ChannelSpec} to a {@link com.energyict.mdc.device.config.DeviceConfiguration}
 * which is linked to a {@link com.energyict.mdc.masterdata.MeasurementType} which is not configured on the correct object.
 * <p/>
 * Copyrights EnergyICT
 * Date: 07/02/14
 * Time: 09:21
 */
public class RegisterTypeIsNotConfiguredException extends LocalizedException {

    /**
     * Creates a new RegisterTypeIsNotConfiguredException that models the
     * exceptional situation that occurs when an attempt is made to add a
     * {@link ChannelSpec} to a {@link com.energyict.mdc.device.config.DeviceConfiguration}
     * which is linked to a {@link com.energyict.mdc.masterdata.MeasurementType} which is not configured in the
     * linked {@link LoadProfileSpec}
     *
     * @param thesaurus       The Thesaurus
     * @param channelSpec     The ChannelSpec which will be added
     * @param loadProfileSpec The LoadProfileSpec which is linked to the ChannelSpec
     * @param measurementType The MeasurementType which is linked to the ChannelSpec but NOT to the LoadProfileSpec
     * @return the newly create RegisterTypeIsNotConfiguredException
     */
    public static RegisterTypeIsNotConfiguredException forChannelInLoadProfileSpec(Thesaurus thesaurus, LoadProfileSpec loadProfileSpec, MeasurementType measurementType, ChannelSpec channelSpec) {
        return new RegisterTypeIsNotConfiguredException(thesaurus, MessageSeeds.CHANNEL_SPEC_CHANNEL_TYPE_IS_NOT_IN_LOAD_PROFILE_SPEC, channelSpec.getName(), measurementType.getName(), loadProfileSpec.getObisCode());
    }

    /**
     * Creates a new RegisterTypeIsNotConfiguredException that models the
     * exceptional situation that occurs when an attempt is made to add a
     * {@link ChannelSpec} to a {@link com.energyict.mdc.device.config.DeviceConfiguration}
     * which is linked to a {@link com.energyict.mdc.masterdata.MeasurementType} which is not configured in the
     * linked {@link DeviceType}
     *
     * @param thesaurus       The Thesaurus
     * @param channelSpec     The ChannelSpec which will be added
     * @param measurementType The measurementType which is linked to the ChannelSpec but NOT to the LoadProfileSpec
     * @param deviceType      The DeviceType which is owner of the {@link com.energyict.mdc.device.config.DeviceConfiguration} to which the ChannelSpec will be added
     * @return RegisterTypeIsNotConfiguredException
     */
    public static RegisterTypeIsNotConfiguredException forChannelInDeviceType(Thesaurus thesaurus, ChannelSpec channelSpec, MeasurementType measurementType, DeviceType deviceType) {
        return new RegisterTypeIsNotConfiguredException(thesaurus, MessageSeeds.CHANNEL_SPEC_CHANNEL_TYPE_IS_NOT_ON_DEVICE_TYPE, channelSpec.getName(), measurementType.getName(), deviceType.getName());
    }

    private RegisterTypeIsNotConfiguredException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

}