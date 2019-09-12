/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.rest.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.users.UserService;

import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.tasks.TaskService;

import javax.ws.rs.core.Application;

import org.mockito.Mock;

/**
 * Created by gde on 4/05/2015.
 */
public class ComTasksApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    @Mock
    TaskService taskService;
    @Mock
    MasterDataService masterDataService;
    @Mock
    DeviceMessageSpecificationService deviceMessageSpecificationService;
    @Mock
    UserService userService;

    @Override
    protected Application getApplication() {
        ComTasksApplication comTasksApplication = new ComTasksApplication();
        comTasksApplication.setTransactionService(transactionService);
        comTasksApplication.setNlsService(nlsService);
        comTasksApplication.setTaskService(taskService);
        comTasksApplication.setMasterDataService(masterDataService);
        comTasksApplication.setDeviceMessageSpecificationService(deviceMessageSpecificationService);
        comTasksApplication.setUserService(userService);
        return comTasksApplication;
    }

}
