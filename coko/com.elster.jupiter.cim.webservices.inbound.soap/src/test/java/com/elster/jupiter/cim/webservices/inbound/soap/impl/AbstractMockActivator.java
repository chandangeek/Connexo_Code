/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.json.JsonService;

import org.osgi.framework.BundleContext;

import java.time.Clock;
import java.util.Optional;

import org.junit.Before;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
    protected MetrologyConfigurationService metrologyConfigurationService;
    @Mock
    protected UserService userService;
    @Mock
    protected User user;
    @Mock
    protected UsagePointLifeCycleService usagePointLifeCycleService;
    @Mock
    protected CustomPropertySetService customPropertySetService;

    private CIMInboundSoapEndpointsActivator activator;
    @Mock
    private JsonService jsonService;
    @Mock
    private EndPointConfigurationService endPointConfigurationService;
    @Mock
    private ServiceCallService serviceCallService;
    @Mock
    private ServiceCallType serviceCallType;
    @Mock
    private WebServicesService webServicesService;

    @Before
    public void init() {
        initMocks();
        initActivator();
    }

    private void initMocks() {
        when(nlsService.getThesaurus(CIMInboundSoapEndpointsActivator.COMPONENT_NAME, Layer.SOAP))
                .thenReturn(thesaurus);

        when(transactionService.getContext()).thenReturn(transactionContext);

        when(threadPrincipalService.getPrincipal()).thenReturn(user);
        when(serviceCallService.findServiceCallType(anyString(), anyString())).thenReturn(Optional.of(serviceCallType));
    }

    private void initActivator() {
        activator = new CIMInboundSoapEndpointsActivator(mock(BundleContext.class), clock, threadPrincipalService,
                transactionService, meteringService, nlsService, upgradeService, metrologyConfigurationService,
                userService, usagePointLifeCycleService, customPropertySetService, jsonService,
                endPointConfigurationService, serviceCallService, webServicesService);
    }

    protected <T> T getInstance(Class<T> clazz) {
        return activator.getDataModel().getInstance(clazz);
    }
}
