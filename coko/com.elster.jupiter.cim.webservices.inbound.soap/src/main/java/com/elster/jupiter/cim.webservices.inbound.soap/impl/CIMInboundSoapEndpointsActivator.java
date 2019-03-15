/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.cim.webservices.inbound.soap.masterdatalinkageconfig.ExecuteMasterDataLinkageConfigEndpoint;
import com.elster.jupiter.cim.webservices.inbound.soap.meterreadings.ExecuteMeterReadingsEndpoint;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.getmeterreadings.ParentGetMeterReadingsCustomPropertySet;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.getmeterreadings.ParentGetMeterReadingsServiceCallHandler;
import com.elster.jupiter.cim.webservices.inbound.soap.usagepointconfig.ExecuteUsagePointConfigEndpoint;
import com.elster.jupiter.cim.webservices.outbound.soap.SendMeterReadingsProvider;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.impl.MeteringDataModelService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.InboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;

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
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

@Singleton
@Component(
        name = "Kore-CIMInboundEndpointsActivator",
        service = {MessageSeedProvider.class},
        property = {"name=" + CIMInboundSoapEndpointsActivator.COMPONENT_NAME},
        immediate = true)
public class CIMInboundSoapEndpointsActivator implements MessageSeedProvider {
    public static final String COMPONENT_NAME = "WS1";

    private volatile DataModel dataModel;
    private volatile UpgradeService upgradeService;
    private volatile Clock clock;
    private volatile Thesaurus thesaurus;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile MeteringService meteringService;
    private volatile MetrologyConfigurationService metrologyConfigurationService;
    private volatile UserService userService;
    private volatile UsagePointLifeCycleService usagePointLifeCycleService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile EndPointConfigurationService endPointConfigurationService;
    private volatile WebServicesService webServicesService;
    private volatile ServiceCallService serviceCallService;
    private volatile MessageService messageService;
    private volatile JsonService jsonService;
    private volatile SendMeterReadingsProvider sendMeterReadingsProvider;

    private List<ServiceRegistration> serviceRegistrations = new ArrayList<>();

    public CIMInboundSoapEndpointsActivator() {
        // for OSGI purposes
    }

    @Inject
    public CIMInboundSoapEndpointsActivator(BundleContext bundleContext, Clock clock, ThreadPrincipalService threadPrincipalService,
                                            TransactionService transactionService, MeteringService meteringService, NlsService nlsService,
                                            UpgradeService upgradeService, MetrologyConfigurationService metrologyConfigurationService,
                                            UserService userService, UsagePointLifeCycleService usagePointLifeCycleService,
                                            CustomPropertySetService customPropertySetService, EndPointConfigurationService endPointConfigurationService,
                                            WebServicesService webServicesService, ServiceCallService serviceCallService,
                                            MessageService messageService, JsonService jsonService,
                                            SendMeterReadingsProvider sendMeterReadingsProvider) {
        this();
        setClock(clock);
        setThreadPrincipalService(threadPrincipalService);
        setTransactionService(transactionService);
        setMeteringService(meteringService);
        setNlsService(nlsService);
        setUpgradeService(upgradeService);
        setMetrologyConfigurationService(metrologyConfigurationService);
        setUserService(userService);
        setUsagePointLifeCycleService(usagePointLifeCycleService);
        setCustomPropertySetService(customPropertySetService);
        setEndPointConfigurationService(endPointConfigurationService);
        setWebServicesService(webServicesService);
        setServiceCallService(serviceCallService);
        setMessageService(messageService);
        setJsonService(jsonService);
        setSendMeterReadingsProvider(sendMeterReadingsProvider);
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
                bind(UserService.class).toInstance(userService);
                bind(UsagePointLifeCycleService.class).toInstance(usagePointLifeCycleService);
                bind(CustomPropertySetService.class).toInstance(customPropertySetService);
                bind(EndPointConfigurationService.class).toInstance(endPointConfigurationService);
                bind(WebServicesService.class).toInstance(webServicesService);
                bind(ServiceCallService.class).toInstance(serviceCallService);
                bind(MessageService.class).toInstance(messageService);
                bind(SendMeterReadingsProvider.class).toInstance(sendMeterReadingsProvider);
                bind(JsonService.class).toInstance(jsonService);
            }
        };
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        dataModel = upgradeService.newNonOrmDataModel();
        dataModel.register(getModule());

        upgradeService.register(InstallIdentifier.identifier("MultiSense", COMPONENT_NAME), dataModel, Installer.class, Collections.emptyMap());
        registerHandlers();
        registerServices(bundleContext);
    }

    @Deactivate
    public void stop() {
        serviceRegistrations.forEach(ServiceRegistration::unregister);
    }

    private void registerHandlers() {
        Map<String, Object> options = ImmutableMap.of("name", ParentGetMeterReadingsServiceCallHandler.SERVICE_CALL_HANDLER_NAME);
        serviceCallService.addServiceCallHandler(dataModel.getInstance(ParentGetMeterReadingsServiceCallHandler.class),
                options);
    }

    private void registerServices(BundleContext bundleContext) {
        registerInboundSoapEndpoint(bundleContext,
                () -> dataModel.getInstance(ExecuteMasterDataLinkageConfigEndpoint.class),
                "CIM MasterDataLinkageConfig");
        registerInboundSoapEndpoint(bundleContext,
                () -> dataModel.getInstance(ExecuteMeterReadingsEndpoint.class),
                "CIM MeterReadings");
        registerInboundSoapEndpoint(bundleContext,
                () -> dataModel.getInstance(ExecuteUsagePointConfigEndpoint.class),
                "CIM UsagePointConfig");
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
    public void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
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
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setUsagePointLifeCycleService(UsagePointLifeCycleService usagePointLifeCycleService) {
        this.usagePointLifeCycleService = usagePointLifeCycleService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setEndPointConfigurationService(EndPointConfigurationService endPointConfigurationService) {
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Reference
    public void setWebServicesService(WebServicesService webServicesService) {
        this.webServicesService = webServicesService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference(target = "(name=" + ParentGetMeterReadingsCustomPropertySet.CUSTOM_PROPERTY_SET_NAME + ")")
    public void setParentGetMeterReadingsCustomPropertySet(CustomPropertySet customPropertySet) {
        // PATCH; required for proper startup; do not delete
    }

    @Reference
    public final void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setSendMeterReadingsProvider(SendMeterReadingsProvider sendMeterReadingsProvider) {
        this.sendMeterReadingsProvider = sendMeterReadingsProvider;
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
