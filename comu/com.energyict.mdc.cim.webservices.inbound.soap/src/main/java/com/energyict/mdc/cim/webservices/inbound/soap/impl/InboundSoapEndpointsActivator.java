/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.impl.MeteringDataModelService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.InboundSoapEndPointProvider;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.cim.webservices.inbound.soap.enddeviceevents.ExecuteEndDeviceEventsEndpoint;
import com.energyict.mdc.cim.webservices.inbound.soap.getenddeviceevents.GetEndDeviceEventsEndpoint;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.ExecuteMeterConfigEndpoint;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

@Singleton
@Component(
        name = "com.energyict.mdc.cim.webservices.inbound.soap.impl.InboundSoapEndpointsActivator",
        service = {MessageSeedProvider.class},
        property = {"name=" + InboundSoapEndpointsActivator.COMPONENT_NAME},
        immediate = true)
public class InboundSoapEndpointsActivator implements MessageSeedProvider {

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

    private List<ServiceRegistration> serviceRegistrations = new ArrayList<>();
    private List<PropertyValueConverter> converters = new ArrayList<>();

    public InboundSoapEndpointsActivator() {
        // for OSGI purposes
    }

    @Inject
    public InboundSoapEndpointsActivator(BundleContext bundleContext, Clock clock, ThreadPrincipalService threadPrincipalService,
                                         TransactionService transactionService, MeteringService meteringService, NlsService nlsService,
                                         UpgradeService upgradeService, MetrologyConfigurationService metrologyConfigurationService,
                                         DeviceLifeCycleService deviceLifeCycleService, DeviceConfigurationService deviceConfigurationService,
                                         DeviceService deviceService, UserService userService,
                                         PropertySpecService propertySpecService, PropertyValueInfoService propertyValueInfoService, LogBookService logBookService,
                                         EndPointConfigurationService endPointConfigurationService) {
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
                bind(MetrologyConfigurationService.class).toInstance(metrologyConfigurationService);
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
            }
        };
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        dataModel = upgradeService.newNonOrmDataModel();
        dataModel.register(getModule());
        addConverter(new ObisCodePropertyValueConverter());
        registerServices(bundleContext);
    }

    @Deactivate
    public void stop() {
        serviceRegistrations.forEach(ServiceRegistration::unregister);
        this.converters.forEach(this.propertyValueInfoService::removePropertyValueInfoConverter);
    }

    private void addConverter(PropertyValueConverter converter) {
        this.converters.add(converter);
        this.propertyValueInfoService.addPropertyValueInfoConverter(converter);
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

    @Override
    public Layer getLayer() {
        return Layer.SOAP;
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