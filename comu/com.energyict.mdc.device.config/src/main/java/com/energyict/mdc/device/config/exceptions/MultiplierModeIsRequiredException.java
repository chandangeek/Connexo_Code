package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.ChannelSpec;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to create a entity without a {@link com.energyict.mdc.protocol.api.device.MultiplierMode}.
 * <p/>
 * Copyrights EnergyICT
 * Date: 07/02/14
 * Time: 10:33
 */
public class MultiplierModeIsRequiredException extends LocalizedException{

    private MultiplierModeIsRequiredException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    /**
     * Creates a new ReadingMethodIsRequiredException that models the
     * exceptional situation that occurs when an attempt is made to create
     * a {@link com.energyict.mdc.device.config.ChannelSpec} without a
     * {@link com.energyict.mdc.protocol.api.device.MultiplierMode}
     *
     * @param thesaurus   The Thesaurus
     * @param channelSpec The ChannelSpec
     * @return the newly create ReadingMethodIsRequiredException
     */
    public static MultiplierModeIsRequiredException forChannelSpec(Thesaurus thesaurus, ChannelSpec channelSpec) {
        MultiplierModeIsRequiredException multiplierModeIsRequiredException = new MultiplierModeIsRequiredException(thesaurus, MessageSeeds.CHANNEL_SPEC_MULTIPLIER_MODE_IS_REQUIRED);
        multiplierModeIsRequiredException.set("channelSpec", channelSpec);
        return multiplierModeIsRequiredException;
    }
}
