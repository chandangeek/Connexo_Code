package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.LoadProfileType;

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

    public static UnsupportedIntervalException strictlyPositive (Thesaurus thesaurus, TimeDuration timeDuration) {
        return new UnsupportedIntervalException(thesaurus, MessageSeeds.INTERVAL_MUST_BE_STRICTLY_POSITIVE, timeDuration.toString());
    }

    public static UnsupportedIntervalException weeksAreNotSupportedForLoadProfileTypes(Thesaurus thesaurus, LoadProfileType loadProfileType) {
        UnsupportedIntervalException unsupportedIntervalException = new UnsupportedIntervalException(thesaurus, MessageSeeds.LOAD_PROFILE_TYPE_INTERVAL_IN_WEEKS_IS_NOT_SUPPORTED);
        unsupportedIntervalException.set("loadProfileType", loadProfileType);
        return unsupportedIntervalException;
    }

    public static UnsupportedIntervalException weeksAreNotSupportedForChannelSpecs(Thesaurus thesaurus, ChannelSpec channelSpec) {
        UnsupportedIntervalException unsupportedIntervalException = new UnsupportedIntervalException(thesaurus, MessageSeeds.CHANNEL_SPEC_INTERVAL_IN_WEEKS_IS_NOT_SUPPORTED);
        unsupportedIntervalException.set("channelSpec", channelSpec);
        return unsupportedIntervalException;
    }

    public static UnsupportedIntervalException multipleNotSupported(Thesaurus thesaurus, TimeDuration timeDuration) {
        switch (timeDuration.getTimeUnitCode()) {
            case TimeDuration.DAYS: {
                return UnsupportedIntervalException.multipleDays(thesaurus, timeDuration.getCount());
            }
            case TimeDuration.MONTHS: {
                return UnsupportedIntervalException.multipleMonths(thesaurus, timeDuration.getCount());
            }
            case TimeDuration.YEARS: {
                return UnsupportedIntervalException.multipleYears(thesaurus, timeDuration.getCount());
            }
            default: {
                assert false : "Unknown TimeDuration that is supposed not to support multiples";
            }
        }
        return null;
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

    private static UnsupportedIntervalException multipleDays (Thesaurus thesaurus, int actualCount) {
        return new UnsupportedIntervalException(thesaurus, MessageSeeds.INTERVAL_IN_DAYS_MUST_BE_ONE, actualCount);
    }
    private static UnsupportedIntervalException multipleMonths (Thesaurus thesaurus, int actualCount) {
        return new UnsupportedIntervalException(thesaurus, MessageSeeds.INTERVAL_IN_MONTHS_MUST_BE_ONE, actualCount);
    }

    private static UnsupportedIntervalException multipleYears (Thesaurus thesaurus, int actualCount) {
        return new UnsupportedIntervalException(thesaurus, MessageSeeds.INTERVAL_IN_YEARS_MUST_BE_ONE, actualCount);
    }

    private UnsupportedIntervalException(Thesaurus thesaurus, MessageSeeds messageSeed, Object... arguments) {
        super(thesaurus, messageSeed, arguments);
    }

}