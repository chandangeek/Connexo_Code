package com.energyict.mdc.engine.impl.commands.store;

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

    UNSUPPORTED_LOAD_PROFILE(5001, "loadProfile.notSupported", "Loadprofile ''{0}'' is not supported by the device"),
    CHANNEL_UNIT_MISMATCH(5002, "channel.unit.mismatch", "Channel unit mismatch: load profile in the meter with OBIS code ''{0}'' has a channel ({1}) with the unit ''{2}'', whilst the configured unit for that channel is ''{3}''"),
    LOAD_PROFILE_INTERVAL_MISMATCH(5003, "loadprofile.interval.mismatch", "Load profile interval mismatch; load profile with OBIS code ''{0}'' has a {1} second(s) interval on the device, while {2} second(s) is configured"),
    LOAD_PROFILE_NUMBER_OF_CHANNELS_MISMATCH(5004, "loadprofile.nbrOfChannels.mismatch", "Number of channels mismatch; load profile with OBIS code ''{0}'' has {1} channel(s) on the device, while there are {2} channel(s) configured"),
    COLLECTED_DEVICE_TOPOLOGY_FOR_UN_KNOWN_DEVICE(5005, "collectedDeviceTopologyForUnKnownDevice", "The collected topology is for an unknown device ''{0}''"),
    COLLECTED_DEVICE_CACHE_FOR_UNKNOWN_DEVICE(5006, "collectedDeviceCacheForUnknownDevice", "Could not store the collected device cache: device '{0}'  does not exist!"),
    SERIALS_REMOVED_FROM_TOPOLOGY(5007, "serialsRemovedFromTopology", "The following devices are removed from the topology: {0}"),
    SERIALS_ADDED_TO_TOPOLOGY(5008, "serialsAddedToTopology", "The following devices are added to the topology: {0}"),
    UNKNOWN_SERIALS_ADDED_TO_TOPOLOGY(5009, "unknownSerialsAddedToTopology", "The following unknown devices were found in the topology: ''{0}''"),
    UNKNOWN_DEVICE_LOAD_PROFILE(5010, "unknownDeviceLoadProfileCollected", "Could not store the collected device load profile: load profile '{0}' does not exist!"),
    UNKNOWN_DEVICE_LOG_BOOK(5011, "unknownDeviceLogBookCollected", "Could not store the collected device logbook: logbook '{0}' does not exist!"),
    UNKNOWN_DEVICE_REGISTER(5012, "unknownDeviceRegisterCollected", "Could not store the collected device register: register '{0}' does not exist!"),
    UNKNOWN_DEVICE_MESSAGE(5013, "unknownDeviceMessageCollected", "Could not store the collected device message: message '{0}' does not exist!"),
    PROPERTY_VALIDATION_FAILED(5014, "propertyValidationFailed", "The validation of property ''{0}'' with value ''{1}'' failed"),
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