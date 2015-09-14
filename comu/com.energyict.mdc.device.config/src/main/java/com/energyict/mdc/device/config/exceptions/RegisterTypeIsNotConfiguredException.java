package com.energyict.mdc.device.config.exceptions;

import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.masterdata.MeasurementType;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

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
     * @param loadProfileSpec The LoadProfileSpec which is linked to the ChannelSpec
     * @param measurementType The MeasurementType which is linked to the ChannelSpec but NOT to the LoadProfileSpec
     * @param channelSpec The ChannelSpec which will be added
     * @param thesaurus The Thesaurus
     * @param messageSeed The MessageSeed
     * @return the newly create RegisterTypeIsNotConfiguredException
     */
    public static RegisterTypeIsNotConfiguredException forChannelInLoadProfileSpec(LoadProfileSpec loadProfileSpec, MeasurementType measurementType, ChannelSpec channelSpec, Thesaurus thesaurus, MessageSeed messageSeed) {
        return new RegisterTypeIsNotConfiguredException(thesaurus, messageSeed, channelSpec.getReadingType().getAliasName(), measurementType.getReadingType().getAliasName(), loadProfileSpec.getObisCode());
    }

    /**
     * Creates a new RegisterTypeIsNotConfiguredException that models the
     * exceptional situation that occurs when an attempt is made to add a
     * {@link ChannelSpec} to a {@link com.energyict.mdc.device.config.DeviceConfiguration}
     * which is linked to a {@link com.energyict.mdc.masterdata.MeasurementType} which is not configured in the
     * linked {@link DeviceType}
     *
     * @param channelSpec     The ChannelSpec which will be added
     * @param measurementType The measurementType which is linked to the ChannelSpec but NOT to the LoadProfileSpec
     * @param deviceType      The DeviceType which is owner of the {@link com.energyict.mdc.device.config.DeviceConfiguration} to which the ChannelSpec will be added
     * @param thesaurus       The Thesaurus
     * @param messageSeed The MessageSeed
     * @return RegisterTypeIsNotConfiguredException
     */
    public static RegisterTypeIsNotConfiguredException forChannelInDeviceType(ChannelSpec channelSpec, MeasurementType measurementType, DeviceType deviceType, Thesaurus thesaurus, MessageSeed messageSeed) {
        return new RegisterTypeIsNotConfiguredException(thesaurus, messageSeed, channelSpec.getReadingType().getAliasName(), measurementType.getReadingType().getAliasName(), deviceType.getName());
    }

    private RegisterTypeIsNotConfiguredException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

}