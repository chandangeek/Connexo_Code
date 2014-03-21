package com.energyict.mdc.task;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.protocol.api.device.LoadProfile;
import java.util.List;

/**
 * Models the {@link com.energyict.mdc.task.ProtocolTask} which can read one or multiple {@link LoadProfile loadProfiles}
 * from a Device.
 * <p>
 * The task can contain an optional list of {@link com.energyict.mdc.device.config.LoadProfileType loadProfileTypes},
 * which means only these types should be fetched from the device. If no list is provided, then <b>all</b>
 * {@link LoadProfile loadProfiles} should be fetched.
 * </p><p>
 * There is an option available to check the configuration of the loadProfiles. If checked and the configuration
 * of a certain LoadProfile does not match, then it will not be fetched from the Device.
 * (a proper note in the Logging will be added)
 * </p><p>
 * All collected intervals of all the LoadProfiles can be marked as <i>BadTime</i> if the clockDifference exceeds the maximum.
 * </p><p>
 * It is also possible to map certain intervalStateBits to proper MeterEvents (of the Device itself ex. does not have MeterEvents).
 * See {@link com.energyict.mdc.protocol.api.device.data.IntervalData#generateEvents()} for a mapping between the two.
 * </p>
 *
 * @author gna
 * @since 19/04/12 - 14:26
 */
public interface LoadProfilesTask extends ProtocolTask {

    /**
     * Returns the LoadProfileTypes belonging to this Task
     *
     * @return a list containing the LoadProfileTypes
     */
    public List<LoadProfileType> getLoadProfileTypes();

    /**
     * Returns true if a LoadProfile should <b>NOT</b> be read if his configuration does not match,
     * false if we should fetch the data even if the configuration is invalid.
     *
     * @return the indication whether to fail if the configuration does not match
     */
    public boolean failIfLoadProfileConfigurationMisMatch();

    /**
     * Returns true if the intervals should be marked as BadTime (if timeDifference exceeds the max),
     * false otherwise.
     *
     * @return the indication whether to mark the intervals as BadTime
     */
    public boolean isMarkIntervalsAsBadTime();

    /**
     * Returns the minimum clock difference before intervals can be marked as BadTime
     *
     * @return the minimum clock difference
     */
    public TimeDuration getMinClockDiffBeforeBadTime();

    /**
     * Returns true if we should create MeterEvents from statusFlags, false otherwise.
     *
     * @return the indication whether to create MeterEvents
     */
    public boolean createMeterEventsFromStatusFlags();

    void setFailIfConfigurationMisMatch(boolean failIfConfigurationMisMatch);

    void setMarkIntervalsAsBadTime(boolean markIntervalsAsBadTime);

    void setMinClockDiffBeforeBadTime(TimeDuration minClockDiffBeforeBadTime);

    void setCreateMeterEventsFromStatusFlags(boolean createMeterEventsFromStatusFlags);
}
