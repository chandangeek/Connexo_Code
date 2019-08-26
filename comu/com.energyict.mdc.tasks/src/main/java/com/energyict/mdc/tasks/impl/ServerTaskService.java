/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.masterdata.LoadProfileType;
import com.energyict.mdc.common.masterdata.LogBookType;
import com.energyict.mdc.common.masterdata.RegisterGroup;
import com.energyict.mdc.common.tasks.LoadProfilesTask;
import com.energyict.mdc.common.tasks.LogBooksTask;
import com.energyict.mdc.common.tasks.RegistersTask;
import com.energyict.mdc.tasks.TaskService;

import java.util.List;

/**
 * Adds behavior to {@link TaskService} that is specific to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-21 (10:48)
 */
public interface ServerTaskService extends TaskService {

    String FIRMWARE_COMTASK_NAME = "Firmware management";

    Thesaurus getThesaurus();

    List<LogBooksTask> findTasksUsing(LogBookType logBookType);

    List<LoadProfilesTask> findTasksUsing(LoadProfileType loadProfileType);

    List<RegistersTask> findTasksUsing(RegisterGroup registerGroup);

}