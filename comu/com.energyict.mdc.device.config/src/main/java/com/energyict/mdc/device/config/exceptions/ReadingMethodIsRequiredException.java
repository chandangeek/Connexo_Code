package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.ChannelSpec;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to create a {@link com.energyict.mdc.device.config.ChannelSpec}
 * without a {@link com.energyict.mdc.protocol.api.device.ReadingMethod}.
 * <p/>
 * Copyrights EnergyICT
 * Date: 07/02/14
 * Time: 10:27
 */
public class ReadingMethodIsRequiredException extends LocalizedException {

    private ReadingMethodIsRequiredException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    /**
     * Creates a new ReadingMethodIsRequiredException that models the
     * exceptional situation that occurs when an attempt is made to create
     * a {@link com.energyict.mdc.device.config.ChannelSpec} without a
     * {@link com.energyict.mdc.protocol.api.device.ReadingMethod}
     *
     * @param thesaurus   The Thesaurus
     * @param channelSpec The ChannelSpec
     * @return the newly create ReadingMethodIsRequiredException
     */
    public static ReadingMethodIsRequiredException forChannelSpec(Thesaurus thesaurus, ChannelSpec channelSpec) {
        ReadingMethodIsRequiredException readingMethodIsRequiredException = new ReadingMethodIsRequiredException(thesaurus, MessageSeeds.CHANNEL_SPEC_READING_METHOD_IS_REQUIRED);
        readingMethodIsRequiredException.set("channelSpec", channelSpec);
        return readingMethodIsRequiredException;
    }
}
