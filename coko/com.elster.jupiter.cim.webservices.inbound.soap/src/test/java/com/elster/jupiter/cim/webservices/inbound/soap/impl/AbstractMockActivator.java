/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.usagepointconfig.UsagePointConfigCustomPropertySet;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.usagepointconfig.UsagePointConfigMasterCustomPropertySet;
import com.elster.jupiter.cim.webservices.outbound.soap.ReplyUsagePointConfigWebService;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceService;
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

import java.lang.reflect.Field;
import java.time.Clock;
import java.util.Optional;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
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
    protected MetrologyConfigurationService metrologyConfigurationService;
    @Mock
    protected UserService userService;
    @Mock
    protected User user;
    @Mock
    protected UsagePointLifeCycleService usagePointLifeCycleService;
    @Mock
    protected CustomPropertySetService customPropertySetService;
    @Mock
    protected EndPointConfigurationService endPointConfigurationService;
    @Mock
    protected WebServicesService webServicesService;
    @Mock
    protected WebServiceCallOccurrenceService webServiceCallOccurrenceService;
    @Mock
    protected ServiceCallService serviceCallService;
    @Mock
    protected MessageService messageService;
    @Mock
    protected JsonService jsonService;
    @Mock
    private ServiceCallType serviceCallType;
    @Mock
    private QueueTableSpec queueTableSpec;
    @Mock
    protected DestinationSpec destinationSpec;
    @Mock
    protected ServiceCall serviceCall;

    private CIMInboundSoapEndpointsActivator activator;
    @Mock
    private ReplyUsagePointConfigWebService replyUsagePointConfigWebService;
    @Mock
    private UsagePointConfigMasterCustomPropertySet usagePointConfigMasterCustomPropertySet;
    @Mock
    private UsagePointConfigCustomPropertySet usagePointConfigCustomPropertySet;
    @Mock
    private OrmService ormService;

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
        ServiceCallBuilder builder = mock(ServiceCallBuilder.class);
        when(builder.origin(anyString())).thenReturn(builder);
        when(builder.extendedWith(any())).thenReturn(builder);
        when(builder.create()).thenReturn(serviceCall);
        when(serviceCallType.newServiceCall()).thenReturn(builder);
        when(serviceCall.newChildCall(any(ServiceCallType.class))).thenReturn(builder);
        DataModel dataModel = upgradeService.newNonOrmDataModel();
        when(ormService.newDataModel(anyString(), anyString())).thenReturn(dataModel);
    }

    private void initActivator() {
        activator = new CIMInboundSoapEndpointsActivator(mock(BundleContext.class), clock, threadPrincipalService,
                transactionService, meteringService, nlsService, upgradeService, metrologyConfigurationService,
                userService, usagePointLifeCycleService, customPropertySetService, endPointConfigurationService,
                webServicesService, serviceCallService, messageService, jsonService,
                replyUsagePointConfigWebService,
                usagePointConfigMasterCustomPropertySet, usagePointConfigCustomPropertySet, ormService);
    }

    protected <T> T getInstance(Class<T> clazz) {
        return activator.getDataModel().getInstance(clazz);
    }

    protected void mockWebServices(boolean isPublished) {
        when(webServicesService.isPublished(anyObject())).thenReturn(isPublished);
    }

    protected static void inject(Class<?> clazz, Object instance, String fieldName, Object value) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
