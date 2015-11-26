package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.impl.MessageSeeds;

/**
 * Models an exception for the situation that occurs when you try to configure a multiplier with a
 * from date in the past while you already have data for that device. Currently we can not
 * retro-actively update the values.
 */
public class CanNotConfigureMultiplierInPastWhenYouAlreadyHaveData extends LocalizedException {
    public CanNotConfigureMultiplierInPastWhenYouAlreadyHaveData(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.CANNOT_CONFIGURE_DEVICE_MULTIPLIER_IN_PAST_WHEN_DATA_EXISTS);
    }
}
