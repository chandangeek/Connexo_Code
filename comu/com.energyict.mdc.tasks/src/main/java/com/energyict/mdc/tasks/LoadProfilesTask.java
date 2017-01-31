/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.masterdata.LoadProfileType;

import java.util.List;
import java.util.Optional;

/**
 * Models the {@link com.energyict.mdc.tasks.ProtocolTask} which can read one or multiple LoadProfile
 * from a Device.
 * <p>
 * The task can contain an optional list of {@link LoadProfileType loadProfileTypes},
 * which means only these types should be fetched from the device. If no list is provided, then <b>all</b>
 * loadProfiles should be fetched.
 * </p><p>
 * There is an option available to check the configuration of the loadProfiles. If checked and the configuration
 * of a certain LoadProfile does not match, then it will not be fetched from the Device.
 * (a proper note in the Logging will be added)
 * </p><p>
 * All collected intervals of all the LoadProfiles can be marked as <i>BadTime</i> if the clockDifference exceeds the maximum.
 * </p><p>
 * It is also possible to map certain reading qualities to proper MeterEvents (if the Device itself ex. does not have MeterEvents).
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
    public void setLoadProfileTypes(List<LoadProfileType> loadProfileTypes);

    /**
     * Returns true if a LoadProfile should <b>NOT</b> be read if his configuration does not match,
     * false if we should fetch the data even if the configuration is invalid.
     *
     * @return the indication whether to fail if the configuration does not match
     */
    public boolean failIfLoadProfileConfigurationMisMatch();
    public void setFailIfConfigurationMisMatch(boolean failIfConfigurationMisMatch);

    /**
     * Returns true if the intervals should be marked as BadTime (if timeDifference exceeds the max),
     * false otherwise.
     *
     * @return the indication whether to mark the intervals as BadTime
     */
    public boolean isMarkIntervalsAsBadTime();
    public void setMarkIntervalsAsBadTime(boolean markIntervalsAsBadTime);

    /**
     * Returns the minimum clock difference before intervals can be marked as BadTime.
     *
     * @return the minimum clock difference
     */
    public Optional<TimeDuration> getMinClockDiffBeforeBadTime();
    public void setMinClockDiffBeforeBadTime(TimeDuration minClockDiffBeforeBadTime);

    /**
     * Returns true if we should create MeterEvents from statusFlags, false otherwise.
     *
     * @return the indication whether to create MeterEvents
     */
    public boolean createMeterEventsFromStatusFlags();
    public void setCreateMeterEventsFromStatusFlags(boolean createMeterEventsFromStatusFlags);

    interface LoadProfilesTaskBuilder {
        public LoadProfilesTaskBuilder loadProfileTypes(List<LoadProfileType> loadProfileTypes);
        public LoadProfilesTaskBuilder failIfConfigurationMisMatch(boolean failIfConfigurationMisMatch);
        public LoadProfilesTaskBuilder markIntervalsAsBadTime(boolean markIntervalsAsBadTime);
        public LoadProfilesTaskBuilder minClockDiffBeforeBadTime(TimeDuration minClockDiffBeforeBadTime);
        public LoadProfilesTaskBuilder createMeterEventsFromFlags(boolean createMeterEventsFromFlags);
        public LoadProfilesTask add();
    }
}
