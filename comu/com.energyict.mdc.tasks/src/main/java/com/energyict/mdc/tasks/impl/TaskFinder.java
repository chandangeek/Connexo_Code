package com.energyict.mdc.tasks.impl;

import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;

import java.util.Optional;

public class TaskFinder implements CanFindByLongPrimaryKey<ComTask> {

    private final TaskService taskService;

    public TaskFinder(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public FactoryIds factoryId() {
        return FactoryIds.COMTASK;
    }

    @Override
    public Class<ComTask> valueDomain() {
        return ComTask.class;
    }

    @Override
    public Optional<ComTask> findByPrimaryKey(long id) {
        return taskService.findComTask(id);
    }
}
