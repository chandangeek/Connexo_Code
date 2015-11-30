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
    public static MultiplierConfigurationException canNotConfigureMultiplierWithStartDateOutOfCurrentMeterActivation(Thesaurus thesaurus) {
        return new MultiplierConfigurationException(thesaurus, MessageSeeds.CANNOT_CONFIGURE_DEVICE_MULTIPLIER_START_DATE_OUT_CURRENT_METERACTIVATION);
    }
}
