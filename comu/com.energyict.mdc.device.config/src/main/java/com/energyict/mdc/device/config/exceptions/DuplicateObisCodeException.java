package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.LoadProfileSpec;

/**
 * Models the exceptional situation that occurs when an
 * attempt is made to create a entity within this bundle
 * with an {@link ObisCode} that is already used another entity of the same type
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (10:59)
 */
public class DuplicateObisCodeException extends LocalizedException {

    private DuplicateObisCodeException(Thesaurus thesaurus, MessageSeeds messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    public static DuplicateObisCodeException forLoadProfileSpec(Thesaurus thesaurus, DeviceConfiguration deviceConfiguration, ObisCode obisCode) {
        return new DuplicateObisCodeException(thesaurus, MessageSeeds.DEVICE_CONFIGURATION_DUPLICATE_OBIS_CODE_FOR_LOAD_PROFILE_SPEC, deviceConfiguration.getName(), obisCode);
    }

    public static DuplicateObisCodeException forLogBookSpec(Thesaurus thesaurus, DeviceConfiguration deviceConfiguration, ObisCode obisCode) {
        return new DuplicateObisCodeException(thesaurus, MessageSeeds.DEVICE_CONFIGURATION_DUPLICATE_OBIS_CODE_FOR_LOGBOOK_SPEC, deviceConfiguration.getName(), obisCode);
    }

    public static DuplicateObisCodeException forRegisterSpec(Thesaurus thesaurus, DeviceConfiguration deviceConfiguration, ObisCode obisCode) {
        return new DuplicateObisCodeException(thesaurus, MessageSeeds.DEVICE_CONFIGURATION_DUPLICATE_OBIS_CODE_FOR_REGISTER_SPEC, deviceConfiguration.getName(), obisCode);
    }

    public static DuplicateObisCodeException forChannelSpecInLoadProfileSpec(Thesaurus thesaurus, DeviceConfiguration deviceConfiguration, ObisCode obisCode, LoadProfileSpec loadProfileSpec) {
        return new DuplicateObisCodeException(thesaurus, MessageSeeds.DEVICE_CONFIGURATION_DUPLICATE_OBIS_CODE_FOR_CHANNEL_SPEC_IN_LOAD_PROFILE_SPEC, loadProfileSpec.getLoadProfileType().getName(), deviceConfiguration.getName(), obisCode);
    }

    public static DuplicateObisCodeException forChannelSpecConfigWithoutLoadProfileSpec(Thesaurus thesaurus, DeviceConfiguration deviceConfiguration, ObisCode obisCode) {
        return new DuplicateObisCodeException(thesaurus, MessageSeeds.DEVICE_CONFIGURATION_DUPLICATE_OBIS_CODE_FOR_CHANNEL_SPEC, deviceConfiguration.getName(), obisCode);
    }

}