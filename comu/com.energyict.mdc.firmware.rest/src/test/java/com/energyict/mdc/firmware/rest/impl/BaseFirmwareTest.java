package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.rest.FirmwareApplication;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.Application;

import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BaseFirmwareTest extends FelixRestApplicationJerseyTest {

    @Mock
    FirmwareService firmwareService;
    @Mock
    DeviceConfigurationService deviceConfigurationService;
    @Mock
    DeviceService deviceService;
    @Mock
    RestQueryService restQueryService;
    @Mock
    PropertySpecService propertySpecService;
    @Mock
    DeviceMessageSpecificationService deviceMessageSpecificationService;


    @Override
    protected MessageSeed[] getMessageSeeds() {
        return MessageSeeds.values();
    }

    @Override
    protected Application getApplication() {
        FirmwareApplication application = new FirmwareApplication();
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        application.setDeviceConfigurationService(deviceConfigurationService);
        application.setDeviceService(deviceService);
        application.setRestQueryService(restQueryService);
        application.setFirmwareService(firmwareService);

        return application;
    }

    protected  <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);

        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(QueryParameters.class))).thenReturn(finder);
        when(finder.defaultSortColumn(anyString())).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        return finder;
    }
}
