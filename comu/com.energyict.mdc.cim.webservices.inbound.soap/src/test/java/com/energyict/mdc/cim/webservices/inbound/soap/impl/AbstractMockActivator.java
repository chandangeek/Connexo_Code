/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.cim.webservices.outbound.soap.ReplyMasterDataLinkageConfigWebService;
import com.elster.jupiter.cim.webservices.outbound.soap.SendMeterReadingsProvider;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.QueryParameters;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.tasks.RecurrentTaskBuilder;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.json.JsonService;

import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigCustomPropertySet;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigMasterCustomPropertySet;
import com.energyict.mdc.cim.webservices.inbound.soap.task.FutureComTaskExecutionHandlerFactory;
import com.energyict.mdc.cim.webservices.outbound.soap.MeterConfigFactory;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;

import org.osgi.framework.BundleContext;

import java.lang.reflect.Field;
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
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
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
    protected HsmEnergyService hsmEnergyService;
    @Mock
    protected SecurityManagementService securityManagementService;
    @Mock
    private ServiceCallType serviceCallType;
    @Mock
    protected WebServicesService webServicesService;
    @Mock
    private DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    @Mock
    protected MetrologyConfigurationService metrologyConfigurationService;
    @Mock
    protected MessageService messageService;
    @Mock
    protected SendMeterReadingsProvider sendMeterReadingsProvider;
    @Mock
    private QueueTableSpec queueTableSpec;
    @Mock
    protected DestinationSpec destinationSpec;
    @Mock
    protected ServiceCall serviceCall;
    @Mock
    OrmService ormService;
    @Mock
    DataModel dataModel;
    @Mock
    protected MeterConfigFactory meterConfigFactory;
    @Mock
    protected TaskService taskService;
    @Mock
    protected DeviceMessageSpecificationService deviceMessageSpecificationService;
    @Mock
    protected MasterDataService masterDataService;
    @Mock
    protected CommunicationTaskService communicationTaskService;
    @Mock
    protected MeteringTranslationService meteringTranslationService;
    @Mock
    protected TopologyService topologyService;
    @Mock
    protected ReplyMasterDataLinkageConfigWebService replyMasterDataLinkageConfigWebService;
    @Mock
    protected MasterDataLinkageConfigCustomPropertySet masterDataLinkageConfigCustomPropertySet;
    @Mock
    protected MasterDataLinkageConfigMasterCustomPropertySet masterDataLinkageConfigMasterCustomPropertySet;

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
        mockWebServices(true);

        ServiceCallBuilder builder = mock(ServiceCallBuilder.class);
        when(builder.origin(anyString())).thenReturn(builder);
        when(builder.extendedWith(any())).thenReturn(builder);
        when(builder.targetObject(any())).thenReturn(builder);
        when(builder.create()).thenReturn(serviceCall);

        when(serviceCallType.newServiceCall()).thenReturn(builder);
        when(serviceCall.newChildCall(any(ServiceCallType.class))).thenReturn(builder);
        when(messageService.getDestinationSpec(FutureComTaskExecutionHandlerFactory.FUTURE_COM_TASK_EXECUTION_DESTINATION)).thenReturn(Optional.of(destinationSpec));
        when(messageService.getQueueTableSpec("MSG_RAWTOPICTABLE")).thenReturn(Optional.of(queueTableSpec));
        when(queueTableSpec.createDestinationSpec(anyString(), anyInt())).thenReturn(destinationSpec);
        when(ormService.newDataModel(InboundSoapEndpointsActivator.COMPONENT_NAME, "Multisense SOAP webservices")).thenReturn(upgradeService.newNonOrmDataModel());

        RecurrentTaskBuilder recurrentTaskBuilder = mock(RecurrentTaskBuilder.class, RETURNS_DEEP_STUBS);
        when(this.taskService.newBuilder()).thenReturn(recurrentTaskBuilder);
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
        activator.setHsmEnergyService(hsmEnergyService);
        activator.setSecurityManagementService(securityManagementService);
        activator.setServiceCallService(serviceCallService);
        activator.setWebServicesService(webServicesService);
        activator.setDeviceLifeCycleConfigurationService(deviceLifeCycleConfigurationService);
        activator.setMetrologyConfigurationService(metrologyConfigurationService);
        activator.setEndPointConfigurationService(endPointConfigurationService);
        activator.setWebServicesService(webServicesService);
        activator.setServiceCallService(serviceCallService);
        activator.setMessageService(messageService);
        activator.setJsonService(jsonService);
        activator.setSendMeterReadingsProvider(sendMeterReadingsProvider);
        activator.setOrmService(ormService);
        activator.setMeterConfigFactory(meterConfigFactory);
        activator.setTaskService(taskService);
        activator.setDeviceMessageSpecificationService(deviceMessageSpecificationService);
        activator.setMasterDataService(masterDataService);
        activator.setCommunicationTaskService(communicationTaskService);
        activator.setMeteringTranslationService(meteringTranslationService);
        activator.setTopologyService(topologyService);
        activator.addReplyMasterDataLinkageConfigWebService(replyMasterDataLinkageConfigWebService);
        activator.setMasterDataLinkageConfigCustomPropertySet(masterDataLinkageConfigCustomPropertySet);
        activator.setMasterDataLinkageConfigMasterCustomPropertySet(masterDataLinkageConfigMasterCustomPropertySet);
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

    protected EndPointConfiguration mockEndPointConfiguration(String name) {
        EndPointConfiguration mock = mock(EndPointConfiguration.class);
        when(mock.getName()).thenReturn(name);
        when(mock.isActive()).thenReturn(true);
        when(mock.isInbound()).thenReturn(false);
        return mock;
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

    protected void mockWebServices(boolean isPublished) {
        when(webServicesService.isPublished(anyObject())).thenReturn(isPublished);
    }
}
