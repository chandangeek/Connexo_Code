package com.energyict.mdc.device.config.exceptions;

import com.energyict.mdc.device.config.ChannelSpec;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an {@link com.elster.jupiter.time.TimeDuration}
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

    public static UnsupportedIntervalException weeksAreNotSupportedForChannelSpecs(Thesaurus thesaurus, ChannelSpec channelSpec, MessageSeed messageSeed) {
        UnsupportedIntervalException unsupportedIntervalException = new UnsupportedIntervalException(thesaurus, messageSeed);
        unsupportedIntervalException.set("channelSpec", channelSpec);
        return unsupportedIntervalException;
    }

    public static UnsupportedIntervalException intervalOfChannelSpecShouldBeLargerThanZero(int count, Thesaurus thesaurus, MessageSeed messageSeed){
        UnsupportedIntervalException invalidValueException = new UnsupportedIntervalException(thesaurus, messageSeed, count);
        invalidValueException.set("count", count);
        return invalidValueException;
    }

    public static UnsupportedIntervalException intervalOfChannelShouldBeOneIfUnitIsLargerThanOneHour(int count, Thesaurus thesaurus, MessageSeed messageSeed){
        UnsupportedIntervalException invalidValueException = new UnsupportedIntervalException(thesaurus, messageSeed, count);
        invalidValueException.set("count", count);
        return invalidValueException;
    }

    private UnsupportedIntervalException(Thesaurus thesaurus, MessageSeed messageSeed, Object... arguments) {
        super(thesaurus, messageSeed, arguments);
    }

}