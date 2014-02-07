package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.ChannelSpec;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to create a entity without a {@link com.energyict.mdc.protocol.api.device.ValueCalculationMethod}.
 * <p/>
 * Copyrights EnergyICT
 * Date: 07/02/14
 * Time: 10:36
 */
public class ValueCalculationMethodIsRequiredException extends LocalizedException {

    private ValueCalculationMethodIsRequiredException(Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed);
    }

    /**
     * Creates a new ValueCalculationMethodIsRequiredException that models the
     * exceptional situation that occurs when an attempt is made to create
     * a {@link com.energyict.mdc.device.config.ChannelSpec} without a
     * {@link com.energyict.mdc.protocol.api.device.ValueCalculationMethod}
     *
     * @param thesaurus   The Thesaurus
     * @param channelSpec The ChannelSpec
     * @return the newly create ValueCalculationMethodIsRequiredException
     */
    public static ValueCalculationMethodIsRequiredException forChannelSpec(Thesaurus thesaurus, ChannelSpec channelSpec) {
        ValueCalculationMethodIsRequiredException valueCalculationMethodIsRequiredException = new ValueCalculationMethodIsRequiredException(thesaurus, MessageSeeds.CHANNEL_SPEC_VALUE_CALCULATION_METHOD_IS_REQUIRED);
        valueCalculationMethodIsRequiredException.set("channelSpec", channelSpec);
        return valueCalculationMethodIsRequiredException;
    }
}
