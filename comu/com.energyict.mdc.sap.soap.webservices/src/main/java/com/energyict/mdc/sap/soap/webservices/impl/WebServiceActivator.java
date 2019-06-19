/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.impl.MeteringDataModelService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.InboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.sap.soap.webservices.SAPMeterReadingDocumentReason;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.StatusChangeRequestCreateEndpoint;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentCreateBulkEndpoint;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentCreateEndpoint;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentResultBulkCreateConfirmationEndpoint;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentResultCreateConfirmationEndpoint;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.ConnectionStatusChangeCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.ConnectionStatusChangeDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MasterMeterReadingDocumentCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MasterMeterReadingDocumentCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MasterMeterReadingDocumentCreateResultCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MasterMeterReadingDocumentCreateResultDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MeterReadingDocumentCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MeterReadingDocumentCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MeterReadingDocumentCreateResultCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MeterReadingDocumentCreateResultDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.database.UpgraderV10_7;

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
import java.util.Collections;
import java.util.Dictionary;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.elster.jupiter.orm.Version.version;

@Singleton
@Component(
        name = "com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator",
        service = {WebServiceActivator.class, MessageSeedProvider.class, TranslationKeyProvider.class},
        property = {"name=" + WebServiceActivator.COMPONENT_NAME},
        immediate = true)
public class WebServiceActivator implements MessageSeedProvider, TranslationKeyProvider {

    public static final String BATCH_EXECUTOR_USER_NAME = "batch executor";
    public static final String COMPONENT_NAME = "SAP";
    public static final String URL_PROPERTY = "url";
    public static final String APPLICATION_NAME = "MultiSense";
    public static final Map<AdditionalProperties, Integer> SAP_PROPERTIES = new HashMap<>();
    public static final List<SAPMeterReadingDocumentReason> METER_READING_REASONS = new CopyOnWriteArrayList<>();
    public static final List<StatusChangeRequestCreateConfirmation> STATUS_CHANGE_REQUEST_CREATE_CONFIRMATIONS = new CopyOnWriteArrayList<>();
    public static final List<MeterReadingDocumentRequestConfirmation> METER_READING_DOCUMENT_REQUEST_CONFIRMATIONS = new CopyOnWriteArrayList<>();
    public static final List<MeterReadingDocumentBulkRequestConfirmation> METER_READING_DOCUMENT_BULK_REQUEST_CONFIRMATIONS = new CopyOnWriteArrayList<>();
    public static final List<MeterReadingDocumentResult> METER_READING_DOCUMENT_RESULTS = new CopyOnWriteArrayList<>();
    public static final List<MeterReadingDocumentBulkResult> METER_READING_DOCUMENT_BULK_RESULTS = new CopyOnWriteArrayList<>();

    private volatile DataModel dataModel;
    private volatile UpgradeService upgradeService;
    private volatile Clock clock;
    private volatile Thesaurus thesaurus;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile MeteringService meteringService;
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
    private volatile MessageService messageService;
    private volatile SAPCustomPropertySets sapCustomPropertySets;
    private volatile TaskService taskService;
    private volatile BundleContext bundleContext;

    private List<ServiceRegistration> serviceRegistrations = new ArrayList<>();

    public WebServiceActivator() {
        // for OSGI purposes
    }

    @Inject
    public WebServiceActivator(BundleContext bundleContext, Clock clock, ThreadPrincipalService threadPrincipalService,
                               TransactionService transactionService, MeteringService meteringService,
                               NlsService nlsService, UpgradeService upgradeService,
                               DeviceLifeCycleService deviceLifeCycleService, DeviceConfigurationService deviceConfigurationService,
                               DeviceService deviceService, UserService userService, BatchService batchService,
                               PropertySpecService propertySpecService, PropertyValueInfoService propertyValueInfoService, LogBookService logBookService,
                               EndPointConfigurationService endPointConfigurationService, ServiceCallService serviceCallService,
                               JsonService jsonService, CustomPropertySetService customPropertySetService,
                               WebServicesService webServicesService, MessageService messageService,
                               TaskService taskService, SAPCustomPropertySets sapCustomPropertySets, OrmService ormService) {
        this();
        setClock(clock);
        setThreadPrincipalService(threadPrincipalService);
        setTransactionService(transactionService);
        setMeteringService(meteringService);
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
        setMessageService(messageService);
        setTaskService(taskService);
        setSAPCustomPropertySets(sapCustomPropertySets);
        setOrmService(ormService);
        activate(bundleContext);
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
                bind(MessageService.class).toInstance(messageService);
                bind(TaskService.class).toInstance(taskService);
                bind(BundleContext.class).toInstance(bundleContext);
                bind(SAPCustomPropertySets.class).toInstance(sapCustomPropertySets);
            }
        };
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        dataModel.register(getModule());

        loadProperties(bundleContext);
        getServiceCallCustomPropertySets().values().forEach(customPropertySetService::addCustomPropertySet);

        upgradeService.register(InstallIdentifier.identifier(APPLICATION_NAME, COMPONENT_NAME), dataModel, Installer.class,
                ImmutableMap.of(version(10, 7), UpgraderV10_7.class));

        registerServices(bundleContext);
    }

    @Deactivate
    public void stop() {
        serviceRegistrations.forEach(ServiceRegistration::unregister);
        getServiceCallCustomPropertySets().values().forEach(customPropertySetService::removeCustomPropertySet);
    }

    private void loadProperties(BundleContext context) {
        EnumSet.allOf(AdditionalProperties.class)
                .forEach(key -> SAP_PROPERTIES.put(key, Optional.ofNullable(context.getProperty(key.getKey()))
                        .map(Integer::valueOf)
                        .orElse(key.getDefaultValue())));
    }

    private Map<String, CustomPropertySet> getServiceCallCustomPropertySets() {
        Map<String, CustomPropertySet> customPropertySetsMap = new HashMap<>();
        customPropertySetsMap.put(ConnectionStatusChangeDomainExtension.class.getName(),
                new ConnectionStatusChangeCustomPropertySet(thesaurus, propertySpecService));
        customPropertySetsMap.put(MasterMeterReadingDocumentCreateRequestDomainExtension.class.getName(),
                new MasterMeterReadingDocumentCreateRequestCustomPropertySet(thesaurus, propertySpecService));
        customPropertySetsMap.put(MasterMeterReadingDocumentCreateResultDomainExtension.class.getName(),
                new MasterMeterReadingDocumentCreateResultCustomPropertySet(thesaurus, propertySpecService));
        customPropertySetsMap.put(MeterReadingDocumentCreateRequestDomainExtension.class.getName(),
                new MeterReadingDocumentCreateRequestCustomPropertySet(thesaurus, propertySpecService));
        customPropertySetsMap.put(MeterReadingDocumentCreateResultDomainExtension.class.getName(),
                new MeterReadingDocumentCreateResultCustomPropertySet(thesaurus, propertySpecService));
        return customPropertySetsMap;
    }

    private void registerServices(BundleContext bundleContext) {
        registerInboundSoapEndpoint(bundleContext,
                () -> dataModel.getInstance(StatusChangeRequestCreateEndpoint.class),
                InboundServices.SAP_STATUS_CHANGE_REQUEST_CREATE.getName());
        registerInboundSoapEndpoint(bundleContext,
                () -> dataModel.getInstance(MeterReadingDocumentCreateEndpoint.class),
                InboundServices.SAP_METER_READING_CREATE_REQUEST.getName());
        registerInboundSoapEndpoint(bundleContext,
                () -> dataModel.getInstance(MeterReadingDocumentCreateBulkEndpoint.class),
                InboundServices.SAP_METER_READING_CREATE_BULK_REQUEST.getName());
        registerInboundSoapEndpoint(bundleContext,
                () -> dataModel.getInstance(MeterReadingDocumentResultCreateConfirmationEndpoint.class),
                InboundServices.SAP_METER_READING_RESULT_CONFIRMATION.getName());
        registerInboundSoapEndpoint(bundleContext,
                () -> dataModel.getInstance(MeterReadingDocumentResultBulkCreateConfirmationEndpoint.class),
                InboundServices.SAP_METER_READING_BULK_RESULT_CONFIRMATION.getName());
    }

    private <T extends InboundSoapEndPointProvider> void registerInboundSoapEndpoint(BundleContext bundleContext,
                                                                                     T provider, String serviceName) {
        Dictionary<String, Object> properties = new Hashtable<>(ImmutableMap.of("name", serviceName));
        serviceRegistrations
                .add(bundleContext.registerService(InboundSoapEndPointProvider.class, provider, properties));
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addSAPMeterReadingDocumentReason(SAPMeterReadingDocumentReason sapMeterReadingDocumentReason) {
        METER_READING_REASONS.add(sapMeterReadingDocumentReason);
    }

    public void removeSAPMeterReadingDocumentReason(SAPMeterReadingDocumentReason sapMeterReadingDocumentReason) {
        METER_READING_REASONS.remove(sapMeterReadingDocumentReason);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addStatusChangeRequestCreateConfirmation(StatusChangeRequestCreateConfirmation statusChangeRequestCreateConfirmation) {
        STATUS_CHANGE_REQUEST_CREATE_CONFIRMATIONS.add(statusChangeRequestCreateConfirmation);
    }

    public void removeStatusChangeRequestCreateConfirmation(StatusChangeRequestCreateConfirmation statusChangeRequestCreateConfirmation) {
        STATUS_CHANGE_REQUEST_CREATE_CONFIRMATIONS.remove(statusChangeRequestCreateConfirmation);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addMeterReadingDocumentRequestConfirmation(MeterReadingDocumentRequestConfirmation requestConfirmation) {
        METER_READING_DOCUMENT_REQUEST_CONFIRMATIONS.add(requestConfirmation);
    }

    public void removeMeterReadingDocumentRequestConfirmation(MeterReadingDocumentRequestConfirmation requestConfirmation) {
        METER_READING_DOCUMENT_REQUEST_CONFIRMATIONS.remove(requestConfirmation);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addMeterReadingDocumentBulkRequestConfirmation(MeterReadingDocumentBulkRequestConfirmation bulkRequestConfirmation) {
        METER_READING_DOCUMENT_BULK_REQUEST_CONFIRMATIONS.add(bulkRequestConfirmation);
    }

    public void removeMeterReadingDocumentBulkRequestConfirmation(MeterReadingDocumentBulkRequestConfirmation bulkRequestConfirmation) {
        METER_READING_DOCUMENT_BULK_REQUEST_CONFIRMATIONS.remove(bulkRequestConfirmation);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addMeterReadingDocumentResult(MeterReadingDocumentResult result) {
        METER_READING_DOCUMENT_RESULTS.add(result);
    }

    public void removeMeterReadingDocumentResult(MeterReadingDocumentResult result) {
        METER_READING_DOCUMENT_RESULTS.remove(result);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addMeterReadingDocumentBulkResult(MeterReadingDocumentBulkResult bulkResult) {
        METER_READING_DOCUMENT_BULK_RESULTS.add(bulkResult);
    }

    public void removeMeterReadingDocumentBulkResult(MeterReadingDocumentBulkResult bulkResult) {
        METER_READING_DOCUMENT_BULK_RESULTS.remove(bulkResult);
    }

    @Reference
    public final void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, getLayer())
                .join(nlsService.getThesaurus(MeteringDataModelService.COMPONENT_NAME, Layer.DOMAIN));
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
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

    @Reference
    public void setSAPCustomPropertySets(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = sapCustomPropertySets;
    }

    @Reference
    public final void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(COMPONENT_NAME, "SAP");
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public Layer getLayer() {
        return Layer.SOAP;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(TranslationKeys.values());
    }

    // for test purposes
    DataModel getDataModel() {
        return dataModel;
    }

    public Thesaurus getThesaurus() {
        return thesaurus;
    }
}