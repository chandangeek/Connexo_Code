package com.energyict.mdc.engine.impl.commands.collect;

import com.energyict.mdc.common.TimeDuration;

/**
 * The {@link ComCommand} which can perform the necessary actions to verify the TimeDifference
 *
 * @author sva
 * @since 18/04/13 - 9:59
 */
public interface VerifyTimeDifferenceCommand extends ComCommand {

    /**
     * Indication that there was no timeDifference read for this {@link ClockCommand}
     */
    public static final TimeDuration DID_NOT_READ_TIME_DIFFERENCE = new TimeDuration(Integer.MAX_VALUE, 0);

    /**
     * Get the TimeDifference of the ClockCommand. If the timeDifference is not read,
     * then {@link TimeDifferenceCommand#DID_NOT_READ_TIME_DIFFERENCE} will be returned.
     *
     * @return the timeDifference
     */
    public TimeDuration getTimeDifference();

}
