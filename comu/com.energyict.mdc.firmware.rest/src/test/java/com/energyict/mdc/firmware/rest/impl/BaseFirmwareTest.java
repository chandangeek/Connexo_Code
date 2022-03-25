/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.rest.DeviceStateAccessFeature;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.firmware.FirmwareCampaignService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.tasks.TaskService;

import javax.ws.rs.core.Application;
import java.time.Clock;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class BaseFirmwareTest extends FelixRestApplicationJerseyTest {
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
    DeviceMessageService deviceMessageService;
    @Mock
    DeviceMessageSpecificationService deviceMessageSpecificationService;
    @Mock
    TaskService taskService;
    @Mock
    Clock clock;
    @Mock
    MeteringGroupsService meteringGroupsService;
    @Mock
    PropertyValueInfoService propertyValueInfoService;
    @Mock
    MdcPropertyUtils mdcPropertyUtils;
    @Mock
    SecurityManagementService securityManagementService;
    @Mock
    FirmwareCampaignService firmwareCampaignService;
    @Mock
    ServiceCallService serviceCallService;
    @Mock
    CommunicationTaskService communicationTaskService;
    @Mock
    ConnectionTaskService connectionTaskService;

    @Override
    protected Application getApplication() {
        FirmwareApplication application = new FirmwareApplication() {
            @Override
            public Set<Class<?>> getClasses() {
                Set<Class<?>> classes = new HashSet<>(super.getClasses());
                classes.remove(DeviceStateAccessFeature.class);
                return classes;
            }
        };
        when(firmwareService.getFirmwareCampaignService()).thenReturn(firmwareCampaignService);
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        application.setDeviceConfigurationService(deviceConfigurationService);
        application.setDeviceService(deviceService);
        application.setRestQueryService(restQueryService);
        application.setFirmwareService(firmwareService);
        application.setDeviceMessageService(deviceMessageService);
        application.setDeviceMessageSpecificationService(deviceMessageSpecificationService);
        application.setTaskService(taskService);
        application.setClock(clock);
        application.setMeteringGroupsService(meteringGroupsService);
        application.setPropertyValueInfoService(propertyValueInfoService);
        application.setMdcPropertyUtils(mdcPropertyUtils);
        application.setSecurityManagementService(securityManagementService);
        application.setServiceCallService(serviceCallService);
        application.setCommunicationTaskService(communicationTaskService);
        application.setConnectionTaskService(connectionTaskService);
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        return application;
    }

    protected <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);

        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(JsonQueryParameters.class))).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        return finder;
    }
}
