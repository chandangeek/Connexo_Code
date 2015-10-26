package com.energyict.mdc.master.data.rest.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.rest.impl.MasterDataApplication;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import org.mockito.Mock;

import javax.ws.rs.core.Application;

public class MasterDataApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    @Mock
    DeviceConfigurationService deviceConfigurationService;
    @Mock
    JsonService jsonService;
    @Mock
    MasterDataService masterDataService;
    @Mock
    MeteringService meteringService;
    @Mock
    MdcReadingTypeUtilService mdcReadingTypeUtilService;

    @Override
    protected Application getApplication() {
        MasterDataApplication application = new MasterDataApplication();
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        application.setDeviceConfigurationService(deviceConfigurationService);
        application.setJsonService(jsonService);
        application.setMasterDataService(masterDataService);
        application.setMeteringService(meteringService);
        application.setMdcReadingTypeUtilService(mdcReadingTypeUtilService);
        return application;
    }
}