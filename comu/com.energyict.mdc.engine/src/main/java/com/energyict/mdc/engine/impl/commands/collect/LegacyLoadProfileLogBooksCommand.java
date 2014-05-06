package com.energyict.mdc.engine.impl.commands.collect;

import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.LogBooksTask;
import java.util.List;

/**
 * @author sva
 * @since 14/12/12 - 15:36
 */

public interface LegacyLoadProfileLogBooksCommand extends LoadProfileCommand, LogBooksCommand {

    /**
     * The LoadProfilesTask which is used for modeling this command
     *
     * @return the {@link com.energyict.mdc.tasks.LoadProfilesTask}
     */
    public LoadProfilesTask getLoadProfilesTask();

    /**
     * The LogBooksTask which is used for modeling this command
     *
     * @return the {@link com.energyict.mdc.tasks.LogBooksTask}
     */
    public LogBooksTask getLogBooksTask();

    /**
     * @return a list of {@link LoadProfileReader loadProfileReaders} which will be read from the device
     */
    List<LoadProfileReader> getLoadProfileReaders();

    /**
     * Get a list of all the {@link LogBookReader LogBookReaders} for this Command
     *
     * @return the requested list
     */
    public List<LogBookReader> getLogBookReaders();

    /**
     * @return the {@link ReadLoadProfileDataCommand}
     */
    public ReadLegacyLoadProfileLogBooksDataCommand getReadLegacyLoadProfileLogBooksDataCommand();

}
