/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.collect;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.tasks.LoadProfilesTask;

import java.util.List;

/**
 * The {@link ComCommand} which can perform the actions necessary for a {@link com.energyict.mdc.tasks.ClockTask}
 *
 * @author gna
 * @since 9/05/12 - 15:56
 */
public interface LoadProfileCommand extends CompositeComCommand {

    /**
     * Value for an invalid LoadProfile interval
     */
    int INVALID_LOAD_PROFILE_INTERVAL = -1;

    /**
     * @return a list of {@link LoadProfileReader loadProfileReaders} which will be read from the device
     */
    List<LoadProfileReader> getLoadProfileReaders();

    /**
     * Remove all given {@link LoadProfileReader loadProfileReaders} from the readerMap
     *
     * @param readersToRemove the list of {@link LoadProfileReader loadProfileReaders} to remove
     */
    void removeIncorrectLoadProfileReaders(List<LoadProfileReader> readersToRemove);

    /**
     * Get the configured interval of the {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile} corresponding with the given {@link LoadProfileReader}
     *
     * @param loadProfileReader the given LoadProfileReader
     * @return the requested interval in seconds
     */
    int findLoadProfileIntervalForLoadProfileReader(LoadProfileReader loadProfileReader);

    /**
     * @return the {@link VerifyLoadProfilesCommand}
     */
    VerifyLoadProfilesCommand getVerifyLoadProfilesCommand();

    /**
     * @return the {@link MarkIntervalsAsBadTimeCommand}
     */
    MarkIntervalsAsBadTimeCommand getMarkIntervalsAsBadTimeCommand();

    /**
     * @return the {@link CreateMeterEventsFromStatusFlagsCommand}
     */
    CreateMeterEventsFromStatusFlagsCommand getCreateMeterEventsFromStatusFlagsCommand();

    /**
     * @return the {@link TimeDifferenceCommand}
     */
    TimeDifferenceCommand getTimeDifferenceCommand();

    /**
     * @return the {@link LoadProfilesTaskOptions}
     */
    public LoadProfilesTaskOptions getLoadProfilesTaskOptions();

    /**
     * @return the {@link OfflineDevice}
     */
    OfflineDevice getOfflineDevice();

    public void updateAccordingTo(LoadProfilesTask loadProfilesTask, GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution);
}