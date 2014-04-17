package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.masterdata.LoadProfileType;

/**
 * Models the exceptional situation that occurs when an {@link com.energyict.mdc.common.TimeDuration}
 * is specified that cannot be supported.
 * The following are examples that are NOT supported:
 * <ul>
 * <li>TimeDuration of x days where x > 1</li>
 * <li>TimeDuration of x months where x > 1</li>
 * <li>TimeDuration of x years where x > 1</li>
 * <li>TimeDuration of x weeks</li>
 * <li>TimeDuration with negative count (e.g. -1 hour)</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (13:11)
 */
public class UnsupportedIntervalException extends LocalizedException {

    public static UnsupportedIntervalException weeksAreNotSupportedForChannelSpecs(Thesaurus thesaurus, ChannelSpec channelSpec) {
        UnsupportedIntervalException unsupportedIntervalException = new UnsupportedIntervalException(thesaurus, MessageSeeds.CHANNEL_SPEC_INTERVAL_IN_WEEKS_IS_NOT_SUPPORTED);
        unsupportedIntervalException.set("channelSpec", channelSpec);
        return unsupportedIntervalException;
    }

    public static UnsupportedIntervalException intervalOfChannelSpecShouldBeLargerThanZero(Thesaurus thesaurus, int count){
        UnsupportedIntervalException invalidValueException = new UnsupportedIntervalException(thesaurus, MessageSeeds.CHANNEL_SPEC_INVALID_INTERVAL_COUNT, count);
        invalidValueException.set("count", count);
        return invalidValueException;
    }

    public static UnsupportedIntervalException intervalOfChannelShouldBeOneIfUnitIsLargerThanOneHour(Thesaurus thesaurus, int count){
        UnsupportedIntervalException invalidValueException = new UnsupportedIntervalException(thesaurus, MessageSeeds.CHANNEL_SPEC_INVALID_INTERVAL_COUNT_LARGE_UNIT, count);
        invalidValueException.set("count", count);
        return invalidValueException;
    }

    private UnsupportedIntervalException(Thesaurus thesaurus, MessageSeeds messageSeed, Object... arguments) {
        super(thesaurus, messageSeed, arguments);
    }

}