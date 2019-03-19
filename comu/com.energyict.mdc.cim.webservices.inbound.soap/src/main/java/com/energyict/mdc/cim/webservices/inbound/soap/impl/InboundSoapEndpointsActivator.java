/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.impl.MeteringDataModelService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.InboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;

import com.energyict.mdc.cim.webservices.inbound.soap.InboundCIMWebServiceExtension;
import com.energyict.mdc.cim.webservices.inbound.soap.enddeviceevents.ExecuteEndDeviceEventsEndpoint;
import com.energyict.mdc.cim.webservices.inbound.soap.getenddeviceevents.GetEndDeviceEventsEndpoint;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.ExecuteMeterConfigEndpoint;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.InboundCIMWebServiceExtensionFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getenddeviceevents.GetEndDeviceEventsCustomPropertySet;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.meterconfig.MeterConfigCustomPropertySet;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.meterconfig.MeterConfigMasterCustomPropertySet;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
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
import java.util.Hashtable;
import java.util.List;


@Singleton
@Component(
        name = "com.energyict.mdc.cim.webservices.inbound.soap.impl.InboundSoapEndpointsActivator",
        service = {MessageSeedProvider.class, TranslationKeyProvider.class},
        property = {"name=" + InboundSoapEndpointsActivator.COMPONENT_NAME},
        immediate = true)
public class InboundSoapEndpointsActivator implements MessageSeedProvider, TranslationKeyProvider {

    public static final String COMPONENT_NAME = "SIM";

    private static final String CIM_MERER_CONFIG = "CIM MeterConfig";
    private static final String CIM_GET_END_DEVICE_EVENTS = "CIM GetEndDeviceEvents";
    private static final String CIM_END_DEVICE_EVENTS = "CIM EndDeviceEvents";

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
    private volatile InboundCIMWebServiceExtensionFactory webServiceExtensionFactory = new InboundCIMWebServiceExtensionFactory();
    private volatile HsmEnergyService hsmEnergyService;
    private volatile SecurityManagementService securityManagementService;
	private volatile DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

    private List<ServiceRegistration> serviceRegistrations = new ArrayList<>();
    private List<PropertyValueConverter> converters = new ArrayList<>();

    public InboundSoapEndpointsActivator() {
        // for OSGI purposes
    }

    @Inject
    public InboundSoapEndpointsActivator(BundleContext bundleContext, Clock clock, ThreadPrincipalService threadPrincipalService,
                                         TransactionService transactionService, MeteringService meteringService,
                                         NlsService nlsService, UpgradeService upgradeService,
                                         DeviceLifeCycleService deviceLifeCycleService, DeviceConfigurationService deviceConfigurationService,
                                         DeviceService deviceService, UserService userService, BatchService batchService,
                                         PropertySpecService propertySpecService, PropertyValueInfoService propertyValueInfoService, LogBookService logBookService,
                                         EndPointConfigurationService endPointConfigurationService, ServiceCallService serviceCallService,
                                         JsonService jsonService, CustomPropertySetService customPropertySetService,
                                         WebServicesService webServicesService, InboundCIMWebServiceExtension webServiceExtensionFactory,
                                         HsmEnergyService hsmEnergyService, SecurityManagementService securityManagementService,
                                         DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
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
        setWebServiceExtensionFactory(webServiceExtensionFactory);
        activate(bundleContext);
        setHsmEnergyService(hsmEnergyService);
        setSecurityManagementService(securityManagementService);
		setDeviceLifeCycleConfigurationService(deviceLifeCycleConfigurationService);
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
                bind(InboundCIMWebServiceExtensionFactory.class).toInstance(webServiceExtensionFactory);
                bind(HsmEnergyService.class).toInstance(hsmEnergyService);
                bind(SecurityManagementService.class).toInstance(securityManagementService);
				bind(DeviceLifeCycleConfigurationService.class).toInstance(deviceLifeCycleConfigurationService);
            }
        };
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        dataModel = upgradeService.newNonOrmDataModel();
        dataModel.register(getModule());

        upgradeService.register(InstallIdentifier.identifier("MultiSense", COMPONENT_NAME), dataModel, Installer.class, Collections.emptyMap());

        addConverter(new ObisCodePropertyValueConverter());
        registerServices(bundleContext);
    }

    @Deactivate
    public void stop() {
        serviceRegistrations.forEach(ServiceRegistration::unregister);
        converters.forEach(propertyValueInfoService::removePropertyValueInfoConverter);
    }

    private void addConverter(PropertyValueConverter converter) {
        converters.add(converter);
        propertyValueInfoService.addPropertyValueInfoConverter(converter);
    }

    private void registerServices(BundleContext bundleContext) {
        registerInboundSoapEndpoint(bundleContext, () -> dataModel.getInstance(ExecuteMeterConfigEndpoint.class), CIM_MERER_CONFIG);
        registerInboundSoapEndpoint(bundleContext, () -> dataModel.getInstance(GetEndDeviceEventsEndpoint.class), CIM_GET_END_DEVICE_EVENTS);
        registerInboundSoapEndpoint(bundleContext, () -> dataModel.getInstance(ExecuteEndDeviceEventsEndpoint.class), CIM_END_DEVICE_EVENTS);
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

    @Reference
    public void setHsmEnergyService(HsmEnergyService hsmEnergyService) {
        this.hsmEnergyService = hsmEnergyService;
    }

    @Reference
    public void setSecurityManagementService(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
    }

	@Reference
	public void setDeviceLifeCycleConfigurationService(
			DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
		this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
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
