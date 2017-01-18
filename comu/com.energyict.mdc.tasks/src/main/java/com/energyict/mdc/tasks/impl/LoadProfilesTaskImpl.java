package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.upl.offline.DeviceOfflineFlags;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.energyict.mdc.upl.offline.DeviceOfflineFlags.ALL_LOAD_PROFILES_FLAG;
import static com.energyict.mdc.upl.offline.DeviceOfflineFlags.MASTER_LOAD_PROFILES_FLAG;
import static com.energyict.mdc.upl.offline.DeviceOfflineFlags.SLAVE_DEVICES_FLAG;

/**
 * Implementation for a {@link com.energyict.mdc.tasks.LoadProfilesTask}
 *
 * @author gna
 * @since 26/04/12 - 10:26
 */
@ValidLoadProfileTask
class LoadProfilesTaskImpl extends ProtocolTaskImpl implements LoadProfilesTask, PersistenceAware {

    private static final DeviceOfflineFlags FLAGS = new DeviceOfflineFlags(MASTER_LOAD_PROFILES_FLAG, SLAVE_DEVICES_FLAG, ALL_LOAD_PROFILES_FLAG);

    enum Fields {
        FAIL_IF_CONFIGURATION_MISMATCH("failIfConfigurationMisMatch"),
        MARK_INTERVALS_AS_BAD_TIME("markIntervalsAsBadTime"),
        CREATE_METER_EVENTS_FROM_STATUS_FLAGS("createMeterEventsFromStatusFlags"),
        MIN_CLOCK_DIFF_BEFORE_BAD_TIME("minClockDiffBeforeBadTime"),
        LOAD_PROFILE_TYPE_USAGES("loadProfileTypeUsageInProtocolTasks");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    private boolean failIfConfigurationMisMatch;
    private boolean markIntervalsAsBadTime;
    private boolean createMeterEventsFromStatusFlags;
    private TimeDuration minClockDiffBeforeBadTime;

    private List<LoadProfileTypeUsageInProtocolTask> loadProfileTypeUsageInProtocolTasks = new ArrayList<>();

    @Override
    public void postLoad() {
        this.minClockDiffBeforeBadTime = this.postLoad(this.minClockDiffBeforeBadTime);
    }

    @Inject
    LoadProfilesTaskImpl(DataModel dataModel) {
        super(dataModel);
        setFlags(FLAGS);
    }

    @Override
    void deleteDependents() {
        this.loadProfileTypeUsageInProtocolTasks.clear();
    }

    /**
     * Returns the LoadProfileTypes belonging to this Task
     *
     * @return a list containing the LoadProfileTypes
     */
    @Override
    public List<LoadProfileType> getLoadProfileTypes() {
        return this.loadProfileTypeUsageInProtocolTasks
                .stream()
                .map(LoadProfileTypeUsageInProtocolTask::getLoadProfileType)
                .collect(Collectors.toList());
    }

    @Override
    public void setLoadProfileTypes(List<LoadProfileType> loadProfileTypes) {
        List<LoadProfileType> wantedLoadProfileTypes = new ArrayList<>(loadProfileTypes);
        Iterator<LoadProfileTypeUsageInProtocolTask> iterator = loadProfileTypeUsageInProtocolTasks.iterator();
        while (iterator.hasNext()) {
            LoadProfileTypeUsageInProtocolTask loadProfileTypeUsageInProtocolTask = iterator.next();
            LoadProfileType stillWantedLoadProfileType = getById(wantedLoadProfileTypes, loadProfileTypeUsageInProtocolTask.getLoadProfileType().getId());
            if (stillWantedLoadProfileType == null) {
                iterator.remove();
            } else {
                wantedLoadProfileTypes.remove(stillWantedLoadProfileType);
            }
        }

        for (LoadProfileType wantedLoadProfileType : wantedLoadProfileTypes) {
            LoadProfileTypeUsageInProtocolTaskImpl loadProfileTypeUsageInProtocolTask = new LoadProfileTypeUsageInProtocolTaskImpl();
            loadProfileTypeUsageInProtocolTask.setLoadProfilesTask(this);
            loadProfileTypeUsageInProtocolTask.setLoadProfileType(wantedLoadProfileType);
            this.loadProfileTypeUsageInProtocolTasks.add(loadProfileTypeUsageInProtocolTask);
        }
    }

    /**
     * Returns true if a LoadProfile should <b>NOT</b> be read if his configuration does not match,
     * false if we should fetch the data even if the configuration is invalid.
     *
     * @return the indication whether to fail if the configuration does not match
     */
    @Override
    public boolean failIfLoadProfileConfigurationMisMatch() {
        return failIfConfigurationMisMatch;
    }

    @Override
    public void setFailIfConfigurationMisMatch(boolean failIfConfigurationMisMatch) {
        this.failIfConfigurationMisMatch = failIfConfigurationMisMatch;
    }

    /**
     * Returns true if the intervals should be marked as BadTime (if timeDifference exceeds the max),
     * false otherwise.
     *
     * @return the indication whether to mark the intervals as BadTime
     */
    @Override
    public boolean isMarkIntervalsAsBadTime() {
        return markIntervalsAsBadTime;
    }

    @Override
    public void setMarkIntervalsAsBadTime(boolean markIntervalsAsBadTime) {
        this.markIntervalsAsBadTime = markIntervalsAsBadTime;
    }

    /**
     * Returns the minimum clock difference before intervals can be marked as BadTime
     *
     * @return the minimum clock difference
     */
    @Override
    public Optional<TimeDuration> getMinClockDiffBeforeBadTime() {
        return Optional.ofNullable(this.minClockDiffBeforeBadTime);
    }

    @Override
    public void setMinClockDiffBeforeBadTime(TimeDuration minClockDiffBeforeBadTime) {
        this.minClockDiffBeforeBadTime = minClockDiffBeforeBadTime;
    }

    /**
     * Returns true if we should create MeterEvents from statusFlags, false otherwise.
     *
     * @return the indication whether to create MeterEvents
     */
    @Override
    public boolean createMeterEventsFromStatusFlags() {
        return this.createMeterEventsFromStatusFlags;
    }

    @Override
    public void setCreateMeterEventsFromStatusFlags(boolean createMeterEventsFromStatusFlags) {
        this.createMeterEventsFromStatusFlags = createMeterEventsFromStatusFlags;
    }
}
