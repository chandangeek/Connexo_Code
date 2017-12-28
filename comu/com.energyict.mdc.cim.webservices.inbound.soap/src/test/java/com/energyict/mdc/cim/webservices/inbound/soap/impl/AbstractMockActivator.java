/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.DeviceService;

import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import org.osgi.framework.BundleContext;

import java.time.Clock;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
    protected BatchService batchService;

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
        activator.setBatchService(batchService);
        activator.activate(mock(BundleContext.class));
    }

    protected <T> T getInstance(Class<T> clazz) {
        return activator.getDataModel().getInstance(clazz);
    }
}