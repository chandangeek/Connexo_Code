/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.impl.MessageSeeds;

/**
 * Models the exceptions related to a multiplier configuration
 */
public class MultiplierConfigurationException extends LocalizedException {

    private MultiplierConfigurationException(Thesaurus thesaurus, MessageSeeds messageSeed) {
        super(thesaurus, messageSeed);
    }

    /**
     * Models an exception for the situation that occurs when you try to configure a multiplier with a
     * from date in the past while you already have data for that device. Currently we can not
     * retro-actively update the values.
     *
     * @param thesaurus the Thesaurus ...
     * @return the newly created exception
     */
    public static MultiplierConfigurationException canNotConfigureMultiplierInPastWhenYouAlreadyHaveData(Thesaurus thesaurus) {
        return new MultiplierConfigurationException(thesaurus, MessageSeeds.CANNOT_CONFIGURE_DEVICE_MULTIPLIER_IN_PAST_WHEN_DATA_EXISTS);
    }

    /**
     * Models an exception for the situation that occurs when you try to configure a multiplier with a
     * from date before the starDate of the current meterActivation
     *
     * @param thesaurus the Thesaurus ...
     * @return the newly created exception
     */
    public static MultiplierConfigurationException multiplierMustHaveMeterActivation(Thesaurus thesaurus) {
        return new MultiplierConfigurationException(thesaurus, MessageSeeds.MULTIPLIER_MUST_HAVE_METERACTIVATION);
    }

    /**
     * Models an exception for the situation where you define a multiplier value of zero
     *
     * @param thesaurus the Thesaurus ...
     * @return the newly created exception
     */
    public static MultiplierConfigurationException multiplierShouldBeLargerThanZero(Thesaurus thesaurus) {
        return new MultiplierConfigurationException(thesaurus, MessageSeeds.MULTIPLIER_SHOULD_BE_LARGER_THAN_ZERO);
    }

    /**
     * Models an exception for the situation where you define a multiplier value larger than Integer.MAX_VALUE
     *
     * @param thesaurus the Thesaurus ...
     * @return the newly created exception
     */
    public static MultiplierConfigurationException multiplierValueExceedsMax(Thesaurus thesaurus) {
        return new MultiplierConfigurationException(thesaurus, MessageSeeds.MULTIPLIER_VALUE_EXCEEDS_MAX_VALUE);
    }
}
