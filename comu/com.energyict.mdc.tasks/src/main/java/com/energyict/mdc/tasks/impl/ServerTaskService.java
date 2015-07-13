package com.energyict.mdc.tasks.impl;

import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.LogBooksTask;
import com.energyict.mdc.tasks.RegistersTask;
import com.energyict.mdc.tasks.TaskService;

import com.elster.jupiter.nls.Thesaurus;

import java.util.List;

/**
 * Adds behavior to {@link TaskService} that is specific to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-21 (10:48)
 */
public interface ServerTaskService extends TaskService {

    String FIRMWARE_COMTASK_NAME = "Firmware management";
    String STATUS_INFORMATION_COMTASK_NAME = "Verify status information";

    Thesaurus getThesaurus();

    List<LogBooksTask> findTasksUsing(LogBookType logBookType);

    List<LoadProfilesTask> findTasksUsing(LoadProfileType loadProfileType);

    List<RegistersTask> findTasksUsing(RegisterGroup registerGroup);

}