/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.cim.webservices.outbound.soap.ReplyMasterDataLinkageConfigWebService;
import com.elster.jupiter.cim.webservices.outbound.soap.SendMeterReadingsProvider;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.impl.MeteringDataModelService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.InboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.cim.webservices.inbound.soap.InboundCIMWebServiceExtension;
import com.energyict.mdc.cim.webservices.inbound.soap.enddeviceevents.ExecuteEndDeviceEventsEndpoint;
import com.energyict.mdc.cim.webservices.inbound.soap.getenddeviceevents.GetEndDeviceEventsEndpoint;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.upgrade.UpgraderV10_6;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.upgrade.UpgraderV10_7;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.upgrade.UpgraderV10_7_2;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.upgrade.UpgraderV10_9;
import com.energyict.mdc.cim.webservices.inbound.soap.masterdatalinkageconfig.ExecuteMasterDataLinkageConfigEndpoint;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.ExecuteMeterConfigEndpoint;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.InboundCIMWebServiceExtensionFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.MeterStatusHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.meterreadings.ExecuteMeterReadingsEndpoint;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getenddeviceevents.GetEndDeviceEventsCustomPropertySet;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.ChildGetMeterReadingsCustomPropertySet;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.ComTaskExecutionServiceCallHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.DeviceMessageServiceCallHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.ParentGetMeterReadingsCustomPropertySet;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.ParentGetMeterReadingsServiceCallHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.SubParentGetMeterReadingsCustomPropertySet;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.SubParentGetMeterReadingsServiceCallHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigCustomPropertySet;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigMasterCustomPropertySet;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigMasterServiceCallHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigServiceCallHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.meterconfig.MeterConfigCustomPropertySet;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.meterconfig.MeterConfigMasterCustomPropertySet;
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

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static com.elster.jupiter.orm.Version.version;


@Singleton
@Component(
        name = "com.energyict.mdc.cim.webservices.inbound.soap.impl.InboundSoapEndpointsActivator",
        service = {MessageSeedProvider.class, TranslationKeyProvider.class},
        property = {"name=" + InboundSoapEndpointsActivator.COMPONENT_NAME},
        immediate = true)
public class InboundSoapEndpointsActivator implements MessageSeedProvider, TranslationKeyProvider {

    public static final String COMPONENT_NAME = "SIM";

    private static final String CIM_METER_CONFIG = "CIM MeterConfig";
    private static final String CIM_GET_END_DEVICE_EVENTS = "CIM GetEndDeviceEvents";
    private static final String CIM_END_DEVICE_EVENTS = "CIM EndDeviceEvents";
    private static final String CIM_METER_READINGS = "CIM MeterReadings";
    private static final String CIM_MASTER_DATA_LINKAGE_CONFIG = "CIM MasterDataLinkageConfig";

    private static final Logger LOGGER = Logger.getLogger(InboundSoapEndpointsActivator.class.getName());
    private static final String RECURRENT_TASK_FREQUENCY = "com.energyict.mdc.cim.webservices.inbound.soap.recurrenttaskfrequency";
    private static final String RECURRENT_TASK_NAME = "Future com tasks execution task";
    private static final int RECURRENT_TASK_DEFAULT_FREQUENCY = 5;
    private static final int RECURRENT_TASK_RETRY_DELAY = 60;
    public static int actualRecurrentTaskFrequency = RECURRENT_TASK_DEFAULT_FREQUENCY;

    private static final String RECURRENT_TASK_READ_OUT_DELAY = "com.energyict.mdc.cim.webservices.inbound.soap.readoutdelay";
    private static final int RECURRENT_TASK_DEFAULT_READ_OUT_DELAY = 1;
    public static int actualRecurrentTaskReadOutDelay = RECURRENT_TASK_DEFAULT_READ_OUT_DELAY;

    private volatile DataModel dataModel;
    private volatile UpgradeService upgradeService;
    private volatile Clock clock;
    private volatile Thesaurus thesaurus;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile MeteringService meteringService;
    private volatile MetrologyConfigurationService metrologyConfigurationService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile DeviceLifeCycleService deviceLifeCycleService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile DeviceService deviceService;
    private volatile UserService userService;
    private volatile PropertySpecService propertySpecService;
    private volatile PropertyValueInfoService propertyValueInfoService;
    private volatile LogBookService logBookService;
    private volatile EndPointConfigurationService endPointConfigurationService;
    private volatile DeviceAlarmService deviceAlarmService;
    private volatile IssueService issueService;
    private volatile BatchService batchService;
    private volatile ServiceCallService serviceCallService;
    private volatile JsonService jsonService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile WebServicesService webServicesService;
    private volatile InboundCIMWebServiceExtensionFactory webServiceExtensionFactory = new InboundCIMWebServiceExtensionFactory();
    private volatile HsmEnergyService hsmEnergyService;
    private volatile SecurityManagementService securityManagementService;
    private volatile DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private volatile SendMeterReadingsProvider sendMeterReadingsProvider;
    private volatile MessageService messageService;
    private volatile OrmService ormService;
    private volatile MeterConfigFactory meterConfigFactory;
    private volatile TaskService taskService;
    private volatile BundleContext bundleContext;
    private volatile DeviceMessageSpecificationService deviceMessageSpecificationService;
    private volatile MasterDataService masterDataService;
    private volatile CommunicationTaskService communicationTaskService;
    private volatile MeteringTranslationService meteringTranslationService;
    private volatile TopologyService topologyService;
    private volatile ReplyMasterDataLinkageConfigWebService replyMasterDataLinkageConfigWebService;

    private List<ServiceRegistration> serviceRegistrations = new ArrayList<>();
    private List<PropertyValueConverter> converters = new ArrayList<>();

    public InboundSoapEndpointsActivator() {
        // for OSGI purposes
    }

    @Inject
    public InboundSoapEndpointsActivator(BundleContext bundleContext, Clock clock, ThreadPrincipalService threadPrincipalService,
                                         TransactionService transactionService, MeteringService meteringService, MeteringGroupsService meteringGroupsService,
                                         NlsService nlsService, UpgradeService upgradeService,
                                         DeviceLifeCycleService deviceLifeCycleService, DeviceConfigurationService deviceConfigurationService,
                                         DeviceService deviceService, UserService userService, BatchService batchService,
                                         PropertySpecService propertySpecService, PropertyValueInfoService propertyValueInfoService, LogBookService logBookService,
                                         EndPointConfigurationService endPointConfigurationService, ServiceCallService serviceCallService,
                                         JsonService jsonService, CustomPropertySetService customPropertySetService,
                                         WebServicesService webServicesService, InboundCIMWebServiceExtension webServiceExtensionFactory,
                                         HsmEnergyService hsmEnergyService, SecurityManagementService securityManagementService,
                                         DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService,
                                         MetrologyConfigurationService metrologyConfigurationService,
                                         SendMeterReadingsProvider sendMeterReadingsProvider, MessageService messageService,
                                         OrmService ormService, TaskService taskService,
                                         DeviceMessageSpecificationService deviceMessageSpecificationService,
                                         MasterDataService masterDataService,
                                         CommunicationTaskService communicationTaskService,
                                         MeteringTranslationService meteringTranslationService,
                                         TopologyService topologyService,
                                         ReplyMasterDataLinkageConfigWebService replyMasterDataLinkageConfigWebService,
                                         MasterDataLinkageConfigMasterCustomPropertySet masterDataLinkageConfigMasterCustomPropertySet,
                                         MasterDataLinkageConfigCustomPropertySet masterDataLinkageConfigCustomPropertySet
                                         ) {
        this();
        setClock(clock);
        setThreadPrincipalService(threadPrincipalService);
        setTransactionService(transactionService);
        setMeteringService(meteringService);
        setMeteringGroupsService(meteringGroupsService);
        setNlsService(nlsService);
        setUpgradeService(upgradeService);
        setDeviceLifeCycleService(deviceLifeCycleService);
        setDeviceConfigurationService(deviceConfigurationService);
        setDeviceService(deviceService);
        setUserService(userService);
        setBatchService(batchService);
        setPropertySpecService(propertySpecService);
        setPropertyValueInfoService(propertyValueInfoService);
        setLogBookService(logBookService);
        setEndPointConfigurationService(endPointConfigurationService);
        setServiceCallService(serviceCallService);
        setJsonService(jsonService);
        setCustomPropertySetService(customPropertySetService);
        setWebServicesService(webServicesService);
        setWebServiceExtensionFactory(webServiceExtensionFactory);
        setMetrologyConfigurationService(metrologyConfigurationService);
        setSendMeterReadingsProvider(sendMeterReadingsProvider);
        setMessageService(messageService);
        activate(bundleContext);
        setHsmEnergyService(hsmEnergyService);
        setSecurityManagementService(securityManagementService);
        setDeviceLifeCycleConfigurationService(deviceLifeCycleConfigurationService);
        setOrmService(ormService);
        setTaskService(taskService);
        setDeviceMessageSpecificationService(deviceMessageSpecificationService);
        setMasterDataService(masterDataService);
        setCommunicationTaskService(communicationTaskService);
        setMeteringTranslationService(meteringTranslationService);
        setTopologyService(topologyService);
        setMasterDataLinkageConfigMasterCustomPropertySet(masterDataLinkageConfigMasterCustomPropertySet);
        setMasterDataLinkageConfigCustomPropertySet(masterDataLinkageConfigCustomPropertySet);
        setReplyMasterDataLinkageConfigWebService(replyMasterDataLinkageConfigWebService);
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(Clock.class).toInstance(clock);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(TransactionService.class).toInstance(transactionService);
                bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
                bind(MeteringService.class).toInstance(meteringService);
                bind(MeteringGroupsService.class).toInstance(meteringGroupsService);
                bind(DeviceLifeCycleService.class).toInstance(deviceLifeCycleService);
                bind(DeviceConfigurationService.class).toInstance(deviceConfigurationService);
                bind(DeviceService.class).toInstance(deviceService);
                bind(UserService.class).toInstance(userService);
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(PropertyValueInfoService.class).toInstance(propertyValueInfoService);
                bind(LogBookService.class).toInstance(logBookService);
                bind(EndPointConfigurationService.class).toInstance(endPointConfigurationService);
                bind(DeviceAlarmService.class).toInstance(deviceAlarmService);
                bind(IssueService.class).toInstance(issueService);
                bind(BatchService.class).toInstance(batchService);
                bind(JsonService.class).toInstance(jsonService);
                bind(ServiceCallService.class).toInstance(serviceCallService);
                bind(CustomPropertySetService.class).toInstance(customPropertySetService);
                bind(WebServicesService.class).toInstance(webServicesService);
                bind(InboundCIMWebServiceExtensionFactory.class).toInstance(webServiceExtensionFactory);
                bind(HsmEnergyService.class).toInstance(hsmEnergyService);
                bind(SecurityManagementService.class).toInstance(securityManagementService);
                bind(DeviceLifeCycleConfigurationService.class).toInstance(deviceLifeCycleConfigurationService);
                bind(MeteringTranslationService.class).toInstance(meteringTranslationService);
                bind(MetrologyConfigurationService.class).toInstance(metrologyConfigurationService);
                bind(SendMeterReadingsProvider.class).toInstance(sendMeterReadingsProvider);
                bind(MessageService.class).toInstance(messageService);
                bind(MeterConfigFactory.class).toInstance(meterConfigFactory);
                bind(OrmService.class).toInstance(ormService);
                bind(TaskService.class).toInstance(taskService);
                bind(BundleContext.class).toInstance(bundleContext);
                bind(DeviceMessageSpecificationService.class).toInstance(deviceMessageSpecificationService);
                bind(MasterDataService.class).toInstance(masterDataService);
                bind(CommunicationTaskService.class).toInstance(communicationTaskService);
                bind(TopologyService.class).toInstance(topologyService);
                bind(ReplyMasterDataLinkageConfigWebService.class).toInstance(replyMasterDataLinkageConfigWebService);
            }
        };
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        dataModel = ormService.newDataModel(COMPONENT_NAME, "Multisense SOAP webservices");
        dataModel.register(getModule());

        upgradeService.register(InstallIdentifier.identifier("MultiSense", COMPONENT_NAME), dataModel, Installer.class,
                ImmutableMap.<Version, Class<? extends Upgrader>>builder()
                        .put(version(10, 6), UpgraderV10_6.class)
                        .put(version(10, 7), UpgraderV10_7.class)
                        .put(version(10, 7, 2), UpgraderV10_7_2.class)
                        .put(version(10, 9), UpgraderV10_9.class)
                        .build());

        setActualRecurrentTaskFrequency();
        setActualRecurrentTaskReadOutDelay();
        createOrUpdateFutureComTasksExecutionTask();
        addConverter(new ObisCodePropertyValueConverter());
        registerHandlers();
        registerServices(bundleContext);
        serviceRegistrations.add(bundleContext.registerService(Subscriber.class, dataModel.getInstance(MeterStatusHandler.class), new Hashtable<>()));

    }

    @Deactivate
    public void stop() {
        serviceRegistrations.forEach(ServiceRegistration::unregister);
        converters.forEach(propertyValueInfoService::removePropertyValueInfoConverter);
    }

    private void createOrUpdateFutureComTasksExecutionTask() {
        threadPrincipalService.set(() -> "Activator");
        try (TransactionContext context = transactionService.getContext()) {
            Optional<RecurrentTask> taskOptional = taskService.getRecurrentTask(RECURRENT_TASK_NAME);
            if (taskOptional.isPresent()) {
                RecurrentTask task = taskOptional.get();
                task.setScheduleExpression(PeriodicalScheduleExpression.every(actualRecurrentTaskFrequency).minutes().at(0).build());
                task.save();
            } else {
                createActionTask(FutureComTaskExecutionHandlerFactory.FUTURE_COM_TASK_EXECUTION_DESTINATION,
                        RECURRENT_TASK_RETRY_DELAY,
                        TranslationKeys.FUTURE_COM_TASK_EXECUTION_NAME,
                        RECURRENT_TASK_NAME,
                        "0 0/" + actualRecurrentTaskFrequency + " * 1/1 * ? *");
            }
            context.commit();
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }
    }

    private void setActualRecurrentTaskFrequency() {
        String property = bundleContext.getProperty(RECURRENT_TASK_FREQUENCY);
        if (property != null) {
            actualRecurrentTaskFrequency = Integer.parseInt(property);
        }
    }

    private void setActualRecurrentTaskReadOutDelay() {
        String property = bundleContext.getProperty(RECURRENT_TASK_READ_OUT_DELAY);
        if (property != null) {
            actualRecurrentTaskReadOutDelay = Integer.parseInt(property);
        }
    }

    private void createActionTask(String destinationSpecName, int destinationSpecRetryDelay, TranslationKey subscriberSpecName, String taskName, String taskSchedule) {
        DestinationSpec destination = messageService.getQueueTableSpec("MSG_RAWTOPICTABLE")
                .get()
                .createDestinationSpec(destinationSpecName, destinationSpecRetryDelay);
        destination.activate();
        destination.subscribe(subscriberSpecName, InboundSoapEndpointsActivator.COMPONENT_NAME, Layer.DOMAIN);

        taskService.newBuilder()
                .setApplication("MultiSense")
                .setName(taskName)
                .setScheduleExpressionString(taskSchedule)
                .setDestination(destination)
                .setPayLoad("payload")
                .scheduleImmediately(true)
                .build();
    }

    private void registerHandlers() {
        serviceCallService.addServiceCallHandler(dataModel.getInstance(ParentGetMeterReadingsServiceCallHandler.class),
                ImmutableMap.of("name", ParentGetMeterReadingsServiceCallHandler.SERVICE_CALL_HANDLER_NAME));
        serviceCallService.addServiceCallHandler(dataModel.getInstance(SubParentGetMeterReadingsServiceCallHandler.class),
                ImmutableMap.of("name", SubParentGetMeterReadingsServiceCallHandler.SERVICE_CALL_HANDLER_NAME));
        serviceCallService.addServiceCallHandler(dataModel.getInstance(ComTaskExecutionServiceCallHandler.class),
                ImmutableMap.of("name", ComTaskExecutionServiceCallHandler.SERVICE_CALL_HANDLER_NAME));
        serviceCallService.addServiceCallHandler(dataModel.getInstance(DeviceMessageServiceCallHandler.class),
                ImmutableMap.of("name", DeviceMessageServiceCallHandler.SERVICE_CALL_HANDLER_NAME));
        serviceCallService.addServiceCallHandler(dataModel.getInstance(MasterDataLinkageConfigServiceCallHandler.class),
                ImmutableMap.of("name", MasterDataLinkageConfigServiceCallHandler.SERVICE_CALL_HANDLER_NAME));
        serviceCallService.addServiceCallHandler(dataModel.getInstance(MasterDataLinkageConfigMasterServiceCallHandler.class),
                ImmutableMap.of("name", MasterDataLinkageConfigMasterServiceCallHandler.SERVICE_CALL_HANDLER_NAME));
    }

    private void addConverter(PropertyValueConverter converter) {
        converters.add(converter);
        propertyValueInfoService.addPropertyValueInfoConverter(converter);
    }

    private void registerServices(BundleContext bundleContext) {
        registerInboundSoapEndpoint(bundleContext, () -> dataModel.getInstance(ExecuteMeterConfigEndpoint.class), CIM_METER_CONFIG);
        registerInboundSoapEndpoint(bundleContext, () -> dataModel.getInstance(GetEndDeviceEventsEndpoint.class), CIM_GET_END_DEVICE_EVENTS);
        registerInboundSoapEndpoint(bundleContext, () -> dataModel.getInstance(ExecuteEndDeviceEventsEndpoint.class), CIM_END_DEVICE_EVENTS);
        registerInboundSoapEndpoint(bundleContext, () -> dataModel.getInstance(ExecuteMeterReadingsEndpoint.class), CIM_METER_READINGS);
        registerInboundSoapEndpoint(bundleContext, () -> dataModel.getInstance(ExecuteMasterDataLinkageConfigEndpoint.class), CIM_MASTER_DATA_LINKAGE_CONFIG);
    }

    private <T extends InboundSoapEndPointProvider> void registerInboundSoapEndpoint(BundleContext bundleContext,
                                                                                     T provider, String serviceName) {
        Dictionary<String, Object> properties = new Hashtable<>(ImmutableMap.of("name", serviceName));
        serviceRegistrations.add(bundleContext.registerService(InboundSoapEndPointProvider.class, provider, properties));
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(COMPONENT_NAME, getLayer())
                .join(nlsService.getThesaurus(MeteringDataModelService.COMPONENT_NAME, Layer.DOMAIN));
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setDeviceLifeCycleService(DeviceLifeCycleService deviceLifeCycleService) {
        this.deviceLifeCycleService = deviceLifeCycleService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setBatchService(BatchService batchService) {
        this.batchService = batchService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setPropertyValueInfoService(PropertyValueInfoService propertyValueInfoService) {
        this.propertyValueInfoService = propertyValueInfoService;
    }

    @Reference
    public void setLogBookService(LogBookService logBookService) {
        this.logBookService = logBookService;
    }

    @Reference
    public void setEndPointConfigurationService(EndPointConfigurationService endPointConfigurationService) {
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Reference
    public void setDeviceAlarmService(DeviceAlarmService deviceAlarmService) {
        this.deviceAlarmService = deviceAlarmService;
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setWebServicesService(WebServicesService webServicesService) {
        this.webServicesService = webServicesService;
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    public void setWebServiceExtensionFactory(InboundCIMWebServiceExtension webServiceExtension) {
        webServiceExtensionFactory.setWebServiceExtension(webServiceExtension);
    }

    public void unsetWebServiceExtensionFactory(InboundCIMWebServiceExtension webServiceExtension) {
        webServiceExtensionFactory.unsetWebServiceExtension(webServiceExtension);
    }


    @Reference(target = "(name=" + MeterConfigMasterCustomPropertySet.CUSTOM_PROPERTY_SET_NAME + ")")
    public void setMeterConfigMasterCustomPropertySet(CustomPropertySet customPropertySet) {
        // PATCH; required for proper startup; do not delete
    }

    @Reference(target = "(name=" + MeterConfigCustomPropertySet.CUSTOM_PROPERTY_SET_NAME + ")")
    public void setMeterConfigCustomPropertySet(CustomPropertySet customPropertySet) {
        // PATCH; required for proper startup; do not delete
    }

    @Reference(target = "(name=" + GetEndDeviceEventsCustomPropertySet.CUSTOM_PROPERTY_SET_NAME + ")")
    public void setGetEndDeviceEventsCustomPropertySet(CustomPropertySet customPropertySet) {
        // PATCH; required for proper startup; do not delete
    }

    @Reference(target = "(name=" + ParentGetMeterReadingsCustomPropertySet.CUSTOM_PROPERTY_SET_NAME + ")")
    public void setParentGetMeterReadingsCustomPropertySet(CustomPropertySet customPropertySet) {
        // PATCH; required for proper startup; do not delete
    }

    @Reference(target = "(name=" + SubParentGetMeterReadingsCustomPropertySet.CUSTOM_PROPERTY_SET_NAME + ")")
    public void setSubParentGetMeterReadingsCustomPropertySet(CustomPropertySet customPropertySet) {
        // PATCH; required for proper startup; do not delete
    }

    @Reference(target = "(name=" + ChildGetMeterReadingsCustomPropertySet.CUSTOM_PROPERTY_SET_NAME + ")")
    public void setChildGetMeterReadingsCustomPropertySet(CustomPropertySet customPropertySet) {
        // PATCH; required for proper startup; do not delete
    }

    @Reference
    public void setHsmEnergyService(HsmEnergyService hsmEnergyService) {
        this.hsmEnergyService = hsmEnergyService;
    }

    @Reference
    public void setSecurityManagementService(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
    }

    @Reference
    public void setDeviceLifeCycleConfigurationService(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    @Reference
    public void setMeteringTranslationService(MeteringTranslationService meteringTranslationService) {
        this.meteringTranslationService = meteringTranslationService;
    }

    @Reference
    public void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Reference
    public void setMeterConfigFactory(MeterConfigFactory meterConfigFactory) {
        this.meterConfigFactory = meterConfigFactory;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    @Reference
    public void setDeviceMessageSpecificationService(DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    @Reference
    public void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Reference
    public void setSendMeterReadingsProvider(SendMeterReadingsProvider sendMeterReadingsProvider) {
        this.sendMeterReadingsProvider = sendMeterReadingsProvider;
    }

    @Reference
    public final void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setMasterDataService(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    @Reference
    public void setCommunicationTaskService(CommunicationTaskService communicationTaskService) {
        this.communicationTaskService = communicationTaskService;
    }

    @Reference
    public void setReplyMasterDataLinkageConfigWebService(ReplyMasterDataLinkageConfigWebService webService) {
        replyMasterDataLinkageConfigWebService = webService;
    }

    @Reference(target = "(name=" + MasterDataLinkageConfigMasterCustomPropertySet.CUSTOM_PROPERTY_SET_NAME + ")")
    public void setMasterDataLinkageConfigMasterCustomPropertySet(
            CustomPropertySet masterDataLinkageConfigMasterCustomPropertySet) {
        // Just for reference
    }

    @Reference(target = "(name=" + MasterDataLinkageConfigCustomPropertySet.CUSTOM_PROPERTY_SET_NAME + ")")
    public void setMasterDataLinkageConfigCustomPropertySet(
            CustomPropertySet masterDataLinkageConfigCustomPropertySet) {
        // Just for reference
    }

    @Override
    public Layer getLayer() {
        return Layer.SOAP;
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> translationKeys = new ArrayList<>();
        translationKeys.addAll(Arrays.asList(TranslationKeys.values()));
        return translationKeys;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    // for test purposes
    DataModel getDataModel() {
        return dataModel;
    }
}