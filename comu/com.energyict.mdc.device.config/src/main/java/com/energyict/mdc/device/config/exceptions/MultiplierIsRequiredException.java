package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.protocol.api.device.MultiplierMode;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to create a entity without a Multiplier when it is required.
 * <p/>
 * Copyrights EnergyICT
 * Date: 07/02/14
 * Time: 10:40
 */
public class MultiplierIsRequiredException extends LocalizedException {

    private MultiplierIsRequiredException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    /**
     * Creates a new MultiplierIsRequiredException that models the
     * exceptional situation that occurs when an attempt is made to create
     * a {@link com.energyict.mdc.device.config.ChannelSpec} without a
     * Multiplier
     *
     * @param thesaurus      The Thesaurus
     * @param channelSpec    The ChannelSpec which lacks the Multiplier
     * @param multiplierMode The MultiplierMode of the ChannelSpec
     * @return the newly created MultiplierIsRequiredException
     */
    public static MultiplierIsRequiredException onChannelSpecWhenModeIsOnObject(Thesaurus thesaurus, ChannelSpec channelSpec, MultiplierMode multiplierMode) {
        MultiplierIsRequiredException multiplierIsRequiredException = new MultiplierIsRequiredException(thesaurus, MessageSeeds.CHANNEL_SPEC_MULTIPLIER_IS_REQUIRED_WHEN, channelSpec, multiplierMode);
        multiplierIsRequiredException.set("channelSpec", channelSpec);
        multiplierIsRequiredException.set("multiplierMode", multiplierMode);
        return multiplierIsRequiredException;
    }
}
