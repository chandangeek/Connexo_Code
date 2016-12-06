package com.energyict.mdc.engine.impl.commands.collect;

import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.LogBooksTask;
import java.util.List;

/**
 * @author sva
 * @since 14/12/12 - 15:36
 */

public interface LegacyLoadProfileLogBooksCommand extends LoadProfileCommand, LogBooksCommand {

    /**
     * @return the {@link LoadProfilesTaskOptions}
     */
    public LoadProfilesTaskOptions getLoadProfilesTaskOptions();

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
