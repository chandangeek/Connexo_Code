package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.tasks.TaskService;

import org.mockito.Mock;

import javax.ws.rs.core.Application;

/**
 * Created by bvn on 9/19/14.
 */
public class DeviceConfigurationApplicationJerseyTest extends FelixRestApplicationJerseyTest {
    @Mock
    MeteringService meteringService;
    @Mock
    MasterDataService masterDataService;
    @Mock
    DeviceConfigurationService deviceConfigurationService;
    @Mock
    ValidationService validationService;
    @Mock
    ProtocolPluggableService protocolPluggableService;
    @Mock
    EngineModelService engineModelService;
    @Mock
    DeviceService deviceService;
    @Mock
    UserService userService;
    @Mock
    JsonService jsonService;
    @Mock
    MdcReadingTypeUtilService mdcReadingTypeUtilService;
    @Mock
    TaskService taskService;
    @Mock
    DeviceMessageService deviceMessageService;

    @Override
    protected MessageSeed[] getMessageSeeds() {
        return MessageSeeds.values();
    }

    @Override
    protected Application getApplication() {
        DeviceConfigurationApplication application = new DeviceConfigurationApplication();
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        application.setMeteringService(meteringService);
        application.setMasterDataService(masterDataService);
        application.setDeviceConfigurationService(deviceConfigurationService);
        application.setValidationService(validationService);
        application.setProtocolPluggableService(protocolPluggableService);
        application.setEngineModelService(engineModelService);
        application.setDeviceService(deviceService);
        application.setUserService(userService);
        application.setJsonService(jsonService);
        application.setTaskService(taskService);
        application.setMdcReadingTypeUtilService(mdcReadingTypeUtilService);
        application.setDeviceMessageService(deviceMessageService);
        return application;
    }
}
