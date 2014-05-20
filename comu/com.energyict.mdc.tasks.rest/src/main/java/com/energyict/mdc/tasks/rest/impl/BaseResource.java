package com.energyict.mdc.tasks.rest.impl;

import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.tasks.TaskService;

import javax.inject.Inject;

public abstract class BaseResource {
    private TransactionService transactionService;
    private MasterDataService masterDataService;
    private TaskService taskService;

    protected TransactionService getTransactionService() {
        return transactionService;
    }

    @Inject
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public MasterDataService getMasterDataService() {
        return masterDataService;
    }

    @Inject
    public void setMasterDataService(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    protected TaskService getTaskService() {
        return taskService;
    }

    @Inject
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }
}