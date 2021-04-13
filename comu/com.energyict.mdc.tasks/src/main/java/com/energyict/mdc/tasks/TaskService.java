/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks;

import com.elster.jupiter.domain.util.Finder;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ProtocolTask;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

import static com.energyict.mdc.common.tasks.TaskServiceKeys.TASK_SERVICE_COMPONENT_NAME;

@ProviderType
public interface TaskService {

    String COMPONENT_NAME = TASK_SERVICE_COMPONENT_NAME;

    String FIRMWARE_COMTASK_NAME = "Firmware management";

    public ComTask newComTask(String name);

    public Optional<ComTask> findComTask(long id);

    List<ComTask> findComTasksByName(String name);

    /**
     * @return a list of all ComTasks created and maintained by the System
     * @since 1.1
     * @see #findAllUserComTasks()
     * @see #findAllComTasks()
     */
    public List<ComTask> findAllSystemComTasks();

    /**
     * @return a list of ComTasks which are created and maintained by the Users
     * @since 1.1
     * @see #findAllSystemComTasks()
     * @see #findAllComTasks()
     */
    public List<ComTask> findAllUserComTasks();

    /**
     * @return a list of <b>all</b> ComTasks
     * @see #findAllUserComTasks
     * @see #findAllSystemComTasks()
     */
    public Finder<ComTask> findAllComTasks();

    public Optional<ProtocolTask> findProtocolTask(long id);

    /**
     * @return a list of <b>all</b> ProtocolTasks, paging is mandatory in this method
     */
    public Finder<ProtocolTask> findAllProtocolTasks();

    public Optional<ComTask> findFirmwareComTask();

    Optional<ComTask> findAndLockComTaskByIdAndVersion(long id, long version);

}