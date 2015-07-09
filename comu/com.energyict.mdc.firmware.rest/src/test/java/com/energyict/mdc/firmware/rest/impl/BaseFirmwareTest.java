package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.rest.DeviceStateAccessFeature;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.rest.FirmwareApplication;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.tasks.TaskService;
import org.mockito.Mock;

import javax.ws.rs.core.Application;
import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    @Mock
    TaskService taskService;
    @Mock
    Clock clock;
    @Mock
    MeteringGroupsService meteringGroupsService;

    @Override
    protected MessageSeed[] getMessageSeeds() {
        return MessageSeeds.values();
    }

    @Override
    protected Application getApplication() {
        FirmwareApplication application = new FirmwareApplication(){
            @Override
            public Set<Class<?>> getClasses() {
                Set<Class<?>> classes = new HashSet<>(super.getClasses());
                classes.remove(DeviceStateAccessFeature.class);
                return classes;
            }
        };
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        application.setDeviceConfigurationService(deviceConfigurationService);
        application.setDeviceService(deviceService);
        application.setRestQueryService(restQueryService);
        application.setFirmwareService(firmwareService);
        application.setDeviceMessageSpecificationService(deviceMessageSpecificationService);
        application.setTaskService(taskService);
        application.setClock(clock);
        application.setMeteringGroupsService(meteringGroupsService);

        return application;
    }

    protected  <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);

        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(JsonQueryParameters.class))).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        return finder;
    }
}
