/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.tools;

import com.elster.jupiter.time.TimeDuration;

/**
 * Provides tools to handle {@link TimeDuration TimeDurations}
 *
 * @author gna
 * @since 24/05/12 - 11:48
 */
public final class TimeDurations {

    /**
     * Checks if it is possible to compare the TimeDuration based on the {@link TimeDuration#timeUnitCode}.
     * {@link TimeDuration#MONTHS} can only be compared with a timeDuration of {@link TimeDuration#MONTHS}, the same applies for {@link TimeDuration#YEARS}
     *
     * @param firstTimeDuration the initial {@link TimeDuration}
     * @param durationToCompare the timeDuration to compare with
     * @return false if my own {@link TimeDuration#timeUnitCode} is {@link TimeDuration#MONTHS} or {@link TimeDuration#YEARS} and the compared timeDuration does not have the same {@link TimeDuration#timeUnitCode}, true otherwise
     */
    protected static boolean timeUnitCodeCheck(TimeDuration firstTimeDuration, TimeDuration durationToCompare) {
        return !(oneTimeUnitCodeCheck(firstTimeDuration, durationToCompare, TimeDuration.TimeUnit.MONTHS)
                || oneTimeUnitCodeCheck(durationToCompare, firstTimeDuration, TimeDuration.TimeUnit.MONTHS)
                || oneTimeUnitCodeCheck(firstTimeDuration, durationToCompare, TimeDuration.TimeUnit.YEARS)
                || oneTimeUnitCodeCheck(durationToCompare, firstTimeDuration, TimeDuration.TimeUnit.YEARS));
    }

    private static boolean oneTimeUnitCodeCheck (TimeDuration first, TimeDuration second, TimeDuration.TimeUnit timeDurationField) {
        return first.getTimeUnit() == timeDurationField && second.getTimeUnit() != timeDurationField;
    }

    /**
     * Indicates that the first {@link TimeDuration} has a larger time-in-milliseconds then the second TimeDuration.
     * <p>
     * <b>If the allowVariableTimeUnitCodes is set to false, then the following applies:</b><br/>
     * <ul>
     * <li>TimeDurations with a {@link TimeDuration#timeUnitCode} of {@link TimeDuration#MONTHS} can only be compared with another TimeDuration with a {@link TimeDuration#timeUnitCode} of {@link TimeDuration#MONTHS},
     * otherwise <b>FALSE</b> will be returned <i>(this is due to the fact that a Month do not have a fixed period)</i></li>
     * <li>TimeDurations with a {@link TimeDuration#timeUnitCode} of {@link TimeDuration#YEARS} can only be compared with another TimeDuration with a {@link TimeDuration#timeUnitCode} of {@link TimeDuration#YEARS}
     * otherwise <b>FALSE</b> will be returned <i>(this is due to the fact that a years do not have a fixed period)</i></li>
     * <li>TimeDurations with a {@link TimeDuration#timeUnitCode} OTHER then {@link TimeDuration#MONTHS} or {@link TimeDuration#YEARS} can be mixed as these have a fixed period</li>
     * </ul>
     * </p>
     * <b>If the allowVariableTimeUnitCodes is set to true,</b> then {@link TimeDuration#MONTHS} are assumed to have 31 days and {@link TimeDuration#YEARS} are assumed to have
     * 365 days, for proper conversion to seconds and milliseconds.
     *
     * @param firstTimeDuration          the initial {@link TimeDuration}
     * @param durationToCompare          the {@link TimeDuration} to compare with
     * @param allowVariableTimeUnitCodes if set to false, then months can only be compared to months and years can only be compared to years
     * @return true if and only if firstTimeDuration TimeDuration has a larger amount of milliseconds representation then the durationToCompare one
     */
    public static boolean hasLargerDurationThen(final TimeDuration firstTimeDuration, final TimeDuration durationToCompare, final boolean allowVariableTimeUnitCodes) {
        return firstTimeDuration != null && durationToCompare != null &&
                (allowVariableTimeUnitCodes || timeUnitCodeCheck(firstTimeDuration, durationToCompare)) &&
                (firstTimeDuration.getMilliSeconds() > durationToCompare.getMilliSeconds());
    }

    /**
     * Indicates that the {@link TimeDuration firstTimeDuration} has an equal or larger time-in-milliseconds then the second {@link TimeDuration durationToCompare}.
     * <p>
     * <b>If the allowVariableTimeUnitCodes is set to false, then the following applies:</b><br/>
     * <ul>
     * <li>TimeDurations with a {@link TimeDuration#timeUnitCode} of {@link TimeDuration#MONTHS} can only be compared with another TimeDuration with a {@link TimeDuration#timeUnitCode} of {@link TimeDuration#MONTHS},
     * otherwise <b>FALSE</b> will be returned <i>(this is due to the fact that a Month do not have a fixed period)</i></li>
     * <li>TimeDurations with a {@link TimeDuration#timeUnitCode} of {@link TimeDuration#YEARS} can only be compared with another TimeDuration with a {@link TimeDuration#timeUnitCode} of {@link TimeDuration#YEARS}
     * otherwise <b>FALSE</b> will be returned <i>(this is due to the fact that a years do not have a fixed period)</i></li>
     * <li>TimeDurations with a {@link TimeDuration#timeUnitCode} OTHER then {@link TimeDuration#MONTHS} or {@link TimeDuration#YEARS} can be mixed as these have a fixed period</li>
     * </ul>
     * </p>
     * <b>If the allowVariableTimeUnitCodes is set to true,</b> then {@link TimeDuration#MONTHS} are assumed to have 31 days and {@link TimeDuration#YEARS} are assumed to have
     * 365 days, for proper conversion to seconds and milliseconds.
     *
     * @param firstTimeDuration          the initial {@link TimeDuration}
     * @param durationToCompare          the {@link TimeDuration} to compare with
     * @param allowVariableTimeUnitCodes if set to false, then months can only be compared to months and years can only be compared to years
     * @return true if and only if firstTimeDuration has a larger or equal amount of milliseconds representation then the durationToCompare one
     */
    public static boolean hasLargerOrEqualDurationThen(final TimeDuration firstTimeDuration, final TimeDuration durationToCompare, final boolean allowVariableTimeUnitCodes) {
        return hasLargerDurationThen(firstTimeDuration, durationToCompare, allowVariableTimeUnitCodes) ||
                        (firstTimeDuration.getMilliSeconds() == durationToCompare.getMilliSeconds());
    }

    /**
     * Indicates that the {@link TimeDuration firstTimeDuration} has a smaller time-in-milliseconds then the second {@link TimeDuration durationToCompare}.
     * <p>
     * <b>If the allowVariableTimeUnitCodes is set to false, then the following applies:</b><br/>
     * <ul>
     * <li>TimeDurations with a {@link TimeDuration#timeUnitCode} of {@link TimeDuration#MONTHS} can only be compared with another TimeDuration with a {@link TimeDuration#timeUnitCode} of {@link TimeDuration#MONTHS},
     * otherwise <b>FALSE</b> will be returned <i>(this is due to the fact that a Month do not have a fixed period)</i></li>
     * <li>TimeDurations with a {@link TimeDuration#timeUnitCode} of {@link TimeDuration#YEARS} can only be compared with another TimeDuration with a {@link TimeDuration#timeUnitCode} of {@link TimeDuration#YEARS}
     * otherwise <b>FALSE</b> will be returned <i>(this is due to the fact that a years do not have a fixed period)</i></li>
     * <li>TimeDurations with a {@link TimeDuration#timeUnitCode} OTHER then {@link TimeDuration#MONTHS} or {@link TimeDuration#YEARS} can be mixed as these have a fixed period</li>
     * </ul>
     * </p>
     * <b>If the allowVariableTimeUnitCodes is set to true,</b> then {@link TimeDuration#MONTHS} are assumed to have 31 days and {@link TimeDuration#YEARS} are assumed to have
     * 365 days, for proper conversion to seconds and milliseconds.
     *
     * @param firstTimeDuration          the initial {@link TimeDuration}
     * @param durationToCompare          the {@link TimeDuration} to compare with
     * @param allowVariableTimeUnitCodes if set to false, then months can only be compared to months and years can only be compared to years
     * @return true if and only if firstTimeDuration TimeDuration has a smaller amount of milliseconds representation then the durationToCompare one
     */
    public static boolean hasSmallerDurationThen(final TimeDuration firstTimeDuration, final TimeDuration durationToCompare, final boolean allowVariableTimeUnitCodes) {
        return firstTimeDuration != null && durationToCompare != null &&
                (allowVariableTimeUnitCodes || timeUnitCodeCheck(firstTimeDuration, durationToCompare)) &&
                (firstTimeDuration.getMilliSeconds() < durationToCompare.getMilliSeconds());
    }

    /**
     * Indicates that the {@link TimeDuration firstTimeDuration} has a smaller or equal time-in-milliseconds then the second {@link TimeDuration durationToCompare}.
     * <p>
     * <b>If the allowVariableTimeUnitCodes is set to false, then the following applies:</b><br/>
     * <ul>
     * <li>TimeDurations with a {@link TimeDuration#timeUnitCode} of {@link TimeDuration#MONTHS} can only be compared with another TimeDuration with a {@link TimeDuration#timeUnitCode} of {@link TimeDuration#MONTHS},
     * otherwise <b>FALSE</b> will be returned <i>(this is due to the fact that a Month do not have a fixed period)</i></li>
     * <li>TimeDurations with a {@link TimeDuration#timeUnitCode} of {@link TimeDuration#YEARS} can only be compared with another TimeDuration with a {@link TimeDuration#timeUnitCode} of {@link TimeDuration#YEARS}
     * otherwise <b>FALSE</b> will be returned <i>(this is due to the fact that a years do not have a fixed period)</i></li>
     * <li>TimeDurations with a {@link TimeDuration#timeUnitCode} OTHER then {@link TimeDuration#MONTHS} or {@link TimeDuration#YEARS} can be mixed as these have a fixed period</li>
     * </ul>
     * </p>
     * <b>If the allowVariableTimeUnitCodes is set to true,</b> then {@link TimeDuration#MONTHS} are assumed to have 31 days and {@link TimeDuration#YEARS} are assumed to have
     * 365 days, for proper conversion to seconds and milliseconds.
     *
     * @param firstTimeDuration          the initial {@link TimeDuration}
     * @param durationToCompare          the {@link TimeDuration} to compare with
     * @param allowVariableTimeUnitCodes if set to false, then months can only be compared to months and years can only be compared to years
     * @return true if and only if firstTimeDuration TimeDuration has a smaller or equal amount of milliseconds representation then the durationToCompare one
     */
    public static boolean hasSmallerOrEqualDurationThen(final TimeDuration firstTimeDuration, final TimeDuration durationToCompare, final boolean allowVariableTimeUnitCodes) {
        return hasSmallerDurationThen(firstTimeDuration, durationToCompare, allowVariableTimeUnitCodes) ||
                (firstTimeDuration.getMilliSeconds() == durationToCompare.getMilliSeconds());
    }

    /**
     * Hide utility class constructor.
     */
    private TimeDurations () {super();}

}
