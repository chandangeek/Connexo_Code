package com.energyict.mdc.device.config.exceptions;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.LoadProfileSpec;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an
 * attempt is made to create a entity within this bundle
 * with an {@link ObisCode} that is already used another entity of the same type
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (10:59)
 */
public class DuplicateObisCodeException extends LocalizedException {

    private DuplicateObisCodeException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    public static DuplicateObisCodeException forLoadProfileSpec(DeviceConfiguration deviceConfiguration, ObisCode obisCode, Thesaurus thesaurus, MessageSeed messageSeed) {
        return new DuplicateObisCodeException(thesaurus, messageSeed, deviceConfiguration.getName(), obisCode);
    }

    public static DuplicateObisCodeException forLogBookSpec(DeviceConfiguration deviceConfiguration, ObisCode obisCode, Thesaurus thesaurus, MessageSeed messageSeed) {
        return new DuplicateObisCodeException(thesaurus, messageSeed, deviceConfiguration.getName(), obisCode);
    }

    public static DuplicateObisCodeException forRegisterSpec(Thesaurus thesaurus, DeviceConfiguration deviceConfiguration, ObisCode obisCode, MessageSeed messageSeed) {
        return new DuplicateObisCodeException(thesaurus, messageSeed, deviceConfiguration.getName(), obisCode);
    }

    public static DuplicateObisCodeException forChannelSpecInLoadProfileSpec(Thesaurus thesaurus, DeviceConfiguration deviceConfiguration, ObisCode obisCode, LoadProfileSpec loadProfileSpec, MessageSeed messageSeed) {
        return new DuplicateObisCodeException(thesaurus, messageSeed, loadProfileSpec.getLoadProfileType().getName(), deviceConfiguration.getName(), obisCode);
    }

    public static DuplicateObisCodeException forChannelSpecConfigWithoutLoadProfileSpec(DeviceConfiguration deviceConfiguration, ObisCode obisCode, Thesaurus thesaurus, MessageSeed messageSeed) {
        return new DuplicateObisCodeException(thesaurus, messageSeed, deviceConfiguration.getName(), obisCode);
    }

}