package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.engine.EngineService;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-21 (14:51)
 */
public enum MessageSeeds implements MessageSeed, TranslationKey {

    UNSUPPORTED_LOAD_PROFILE(1, "loadProfile.notSupported", "Loadprofile ''{0}'' is not supported by the device"),
    CHANNEL_UNIT_MISMATCH(2, "channel.unit.mismatch", "Channel unit mismatch: load profile in the meter with OBIS code ''{0}'' has a channel ({1}) with the unit ''{2}'', whilst the configured unit for that channel is ''{3}''"),
    LOAD_PROFILE_INTERVAL_MISMATCH(3, "loadprofile.interval.mismatch", "Load profile interval mismatch; load profile with OBIS code ''{0}'' has a {1} second(s) interval on the device, while {2} second(s) is configured"),
    LOAD_PROFILE_NUMBER_OF_CHANNELS_MISMATCH(4, "loadprofile.nbrOfChannels.mismatch", "Number of channels mismatch; load profile with OBIS code ''{0}'' has {1} channel(s) on the device, while there are {2} channel(s) configured"),
    ;
    private final int number;
    private final String key;
    private final String defaultFormat;

    MessageSeeds(int number, String key, String defaultFormat) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    @Override
    public Level getLevel() {
        return Level.INFO;
    }

    @Override
    public String getModule() {
        return EngineService.COMPONENTNAME;
    }

}