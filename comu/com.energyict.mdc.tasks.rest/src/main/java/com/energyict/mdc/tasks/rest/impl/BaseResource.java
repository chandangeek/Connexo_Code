package com.energyict.mdc.tasks.rest.impl;

import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.tasks.TaskService;

import javax.inject.Inject;

public abstract class BaseResource {
    private TaskService taskService;
    private MasterDataService masterDataService;

    public TaskService getTaskService() {
        return taskService;
    }

    @Inject
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    public MasterDataService getMasterDataService() {
        return masterDataService;
    }

    @Inject
    public void setMasterDataService(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }
}