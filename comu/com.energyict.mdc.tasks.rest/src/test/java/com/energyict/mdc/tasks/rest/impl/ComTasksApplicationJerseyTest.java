package com.energyict.mdc.tasks.rest.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.tasks.TaskService;
import org.mockito.Mock;

import javax.ws.rs.core.Application;

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

    @Override
    protected MessageSeed[] getMessageSeeds() {
        return MessageSeeds.values();
    }

    @Override
    protected Application getApplication() {
        ComTasksApplication comTasksApplication = new ComTasksApplication();
        comTasksApplication.setTransactionService(transactionService);
        comTasksApplication.setNlsService(nlsService);
        comTasksApplication.setTaskService(taskService);
        comTasksApplication.setMasterDataService(masterDataService);
        comTasksApplication.setDeviceMessageSpecificationService(deviceMessageSpecificationService);
        return comTasksApplication;
    }

}
