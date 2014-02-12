package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.ChannelSpec;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to create a new entity within this bundle without
 * specifying an {@link com.energyict.mdc.common.interval.Phenomenon}.
 * <p/> *
 * Copyrights EnergyICT
 * Date: 07/02/14
 * Time: 10:07
 */
public class PhenomenonIsRequiredException extends LocalizedException {

    private PhenomenonIsRequiredException(Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed);
    }

    /**
     * Creates a new PhenomenonIsRequiredException that models the
     * exceptional situation that occurs when an attempt is made to create
     * a {@link com.energyict.mdc.device.config.ChannelSpec} without a
     * {@link com.energyict.mdc.common.interval.Phenomenon}
     *
     * @param thesaurus   The Thesaurus
     * @param channelSpec The ChannelSpec which doesn't have a Phenomenon
     * @return the newly created PhenomenonIsRequiredException
     */
    public static PhenomenonIsRequiredException forChannelSpec(Thesaurus thesaurus, ChannelSpec channelSpec) {
        PhenomenonIsRequiredException phenomenonIsRequiredException = new PhenomenonIsRequiredException(thesaurus, MessageSeeds.CHANNEL_SPEC_PHENOMENON_IS_REQUIRED);
        phenomenonIsRequiredException.set("channelSpec", channelSpec);
        return phenomenonIsRequiredException;
    }
}
