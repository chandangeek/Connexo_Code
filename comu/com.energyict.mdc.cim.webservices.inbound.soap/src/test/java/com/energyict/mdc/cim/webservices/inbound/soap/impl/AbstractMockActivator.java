/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.QueryParameters;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;

import org.osgi.framework.BundleContext;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractMockActivator {

    protected Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;

    protected UpgradeService upgradeService = UpgradeModule.FakeUpgradeService.getInstance();

    @Mock
    protected NlsService nlsService;
    @Mock
    protected Clock clock;
    @Mock
    protected TransactionService transactionService;
    @Mock
    protected TransactionContext transactionContext;
    @Mock
    protected ThreadPrincipalService threadPrincipalService;
    @Mock
    protected MeteringService meteringService;
    @Mock
    protected DeviceConfigurationService deviceConfigurationService;
    @Mock
    protected DeviceService deviceService;
    @Mock
    protected DeviceLifeCycleService deviceLifeCycleService;
    @Mock
    protected UserService userService;
    @Mock
    protected User user;
    @Mock
    protected PropertySpecService propertySpecService;
    @Mock
    protected PropertyValueInfoService propertyValueInfoService;
    @Mock
    protected LogBookService logBookService;
    @Mock
    protected EndPointConfigurationService endPointConfigurationService;
    @Mock
    protected DeviceAlarmService deviceAlarmService;
    @Mock
    protected IssueService issueService;
    @Mock
    protected BatchService batchService;
    @Mock
    protected JsonService jsonService;
    @Mock
    protected ServiceCallService serviceCallService;
    @Mock
    protected CustomPropertySetService customPropertySetService;
    @Mock
    private ServiceCallType serviceCallType;
    @Mock
    protected WebServicesService webServicesService;

    private InboundSoapEndpointsActivator activator;

    @Before
    public void init() {
        initMocks();
        initActivator();
    }

    private void initMocks() {
        when(nlsService.getThesaurus(InboundSoapEndpointsActivator.COMPONENT_NAME, Layer.SOAP)).thenReturn(thesaurus);
        when(transactionService.getContext()).thenReturn(transactionContext);
        when(threadPrincipalService.getPrincipal()).thenReturn(user);
        when(serviceCallService.findServiceCallType(anyString(), anyString())).thenReturn(Optional.of(serviceCallType));
        when(webServicesService.isPublished(anyObject())).thenReturn(true);
    }

    private void initActivator() {
        activator = new InboundSoapEndpointsActivator();
        activator.setClock(clock);
        activator.setUpgradeService(upgradeService);
        activator.setTransactionService(transactionService);
        activator.setThreadPrincipalService(threadPrincipalService);
        activator.setNlsService(nlsService);
        activator.setMeteringService(meteringService);
        activator.setDeviceConfigurationService(deviceConfigurationService);
        activator.setDeviceLifeCycleService(deviceLifeCycleService);
        activator.setDeviceService(deviceService);
        activator.setUserService(userService);
        activator.setPropertySpecService(propertySpecService);
        activator.setPropertyValueInfoService(propertyValueInfoService);
        activator.setLogBookService(logBookService);
        activator.setEndPointConfigurationService(endPointConfigurationService);
        activator.setDeviceAlarmService(deviceAlarmService);
        activator.setIssueService(issueService);
        activator.setBatchService(batchService);
        activator.setJsonService(jsonService);
        activator.setCustomPropertySetService(customPropertySetService);
        activator.setServiceCallService(serviceCallService);
        activator.setWebServicesService(webServicesService);
        activator.activate(mock(BundleContext.class));
    }

    protected <T> T getInstance(Class<T> clazz) {
        return activator.getDataModel().getInstance(clazz);
    }

    protected <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);

        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(QueryParameters.class))).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        when(finder.stream()).thenReturn(list.stream());
        return finder;
    }
}