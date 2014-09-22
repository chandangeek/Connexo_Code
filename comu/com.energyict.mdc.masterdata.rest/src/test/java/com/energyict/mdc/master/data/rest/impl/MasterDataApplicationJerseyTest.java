package com.energyict.mdc.master.data.rest.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.rest.impl.MasterDataApplication;
import com.energyict.mdc.masterdata.rest.impl.MessageSeeds;
import javax.ws.rs.core.Application;
import org.mockito.Mock;

/**
 * Created by bvn on 9/19/14.
 */
public class MasterDataApplicationJerseyTest extends FelixRestApplicationJerseyTest {
    @Mock
    DeviceConfigurationService deviceConfigurationService;
    @Mock
    JsonService jsonService;
    @Mock
    MasterDataService masterDataService;

    @Override
    protected MessageSeed[] getMessageSeeds() {
        return MessageSeeds.values();
    }

    @Override
    protected Application getApplication() {
        MasterDataApplication application = new MasterDataApplication();
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        application.setDeviceConfigurationService(deviceConfigurationService);
        application.setJsonService(jsonService);
        application.setMasterDataService(masterDataService);
        return application;
    }
}
