package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.RegisterSpec;

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

    public static DuplicateObisCodeException forRegisterMapping(Thesaurus thesaurus, ObisCode obisCode, Phenomenon phenomenon, int timeOfUse, RegisterMapping registerMapping) {
        DuplicateObisCodeException duplicateObisCodeException = new DuplicateObisCodeException(thesaurus, MessageSeeds.REGISTER_MAPPING_OBIS_CODE_TOU_PEHNOMENON_ALREADY_EXISTS, obisCode, registerMapping);
        duplicateObisCodeException.set("obisCode", obisCode.toString());
        duplicateObisCodeException.set("unit", phenomenon.getUnit().toString());
        duplicateObisCodeException.set("timeOfUse", timeOfUse);
        duplicateObisCodeException.set("registerMapping", registerMapping.getName());
        return duplicateObisCodeException;
    }

    public static DuplicateObisCodeException forLoadProfileSpec(Thesaurus thesaurus, DeviceConfiguration deviceConfiguration, ObisCode obisCode, LoadProfileSpec loadProfileSpec) {
        DuplicateObisCodeException duplicateObisCodeException = new DuplicateObisCodeException(thesaurus, MessageSeeds.DEVICE_CONFIGURATION_DUPLICATE_OBIS_CODE_FOR_LOAD_PROFILE_SPEC, deviceConfiguration, obisCode);
        duplicateObisCodeException.set("obisCode", obisCode.toString());
        duplicateObisCodeException.set("deviceConfiguration", deviceConfiguration);
        duplicateObisCodeException.set("loadProfileSpec", loadProfileSpec);
        return duplicateObisCodeException;
    }

    public static DuplicateObisCodeException forLogBookSpec(Thesaurus thesaurus, DeviceConfiguration deviceConfiguration, ObisCode obisCode, LogBookSpec logBookSpec) {
        DuplicateObisCodeException duplicateObisCodeException = new DuplicateObisCodeException(thesaurus, MessageSeeds.DEVICE_CONFIGURATION_DUPLICATE_OBIS_CODE_FOR_LOGBOOK_SPEC, deviceConfiguration, obisCode);
        duplicateObisCodeException.set("obisCode", obisCode.toString());
        duplicateObisCodeException.set("deviceConfiguration", deviceConfiguration);
        duplicateObisCodeException.set("logBookSpec", logBookSpec);
        return duplicateObisCodeException;
    }

    public static DuplicateObisCodeException forRegisterSpec(Thesaurus thesaurus, DeviceConfiguration deviceConfiguration, ObisCode obisCode, RegisterSpec registerSpec) {
        DuplicateObisCodeException duplicateObisCodeException = new DuplicateObisCodeException(thesaurus, MessageSeeds.DEVICE_CONFIGURATION_DUPLICATE_OBIS_CODE_FOR_REGISTER_SPEC, deviceConfiguration, obisCode);
        duplicateObisCodeException.set("obisCode", obisCode.toString());
        duplicateObisCodeException.set("deviceConfiguration", deviceConfiguration);
        duplicateObisCodeException.set("registerSpec", registerSpec);
        return duplicateObisCodeException;
    }

    public static DuplicateObisCodeException forChannelSpecInLoadProfileSpec(Thesaurus thesaurus, DeviceConfiguration deviceConfiguration, ObisCode obisCode, ChannelSpec channelSpec, LoadProfileSpec loadProfileSpec) {
        DuplicateObisCodeException duplicateObisCodeException = new DuplicateObisCodeException(thesaurus, MessageSeeds.DEVICE_CONFIGURATION_DUPLICATE_OBIS_CODE_FOR_CHANNEL_SPEC_IN_LOAD_PROFILE_SPEC, loadProfileSpec, deviceConfiguration, obisCode);
        duplicateObisCodeException.set("obisCode", obisCode.toString());
        duplicateObisCodeException.set("deviceConfiguration", deviceConfiguration);
        duplicateObisCodeException.set("channelSpec", channelSpec);
        duplicateObisCodeException.set("loadProfileSpec", loadProfileSpec);
        return duplicateObisCodeException;
    }

    public static DuplicateObisCodeException forChannelSpecConfigWithoutLoadProfileSpec(Thesaurus thesaurus, DeviceConfiguration deviceConfiguration, ObisCode obisCode, ChannelSpec channelSpec) {
        DuplicateObisCodeException duplicateObisCodeException = new DuplicateObisCodeException(thesaurus, MessageSeeds.DEVICE_CONFIGURATION_DUPLICATE_OBIS_CODE_FOR_CHANNEL_SPEC, deviceConfiguration, obisCode);
        duplicateObisCodeException.set("obisCode", obisCode.toString());
        duplicateObisCodeException.set("deviceConfiguration", deviceConfiguration);
        duplicateObisCodeException.set("channelSpec", channelSpec);
        return duplicateObisCodeException;
    }

}