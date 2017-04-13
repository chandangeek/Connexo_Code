/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.processes.keyrenewal.api;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.license.License;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.*;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.common.rest.ExceptionLogger;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.processes.keyrenewal.api.servicecall.KeyRenewalCustomPropertySet;
import com.energyict.mdc.processes.keyrenewal.api.servicecall.ServiceCallCommands;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.time.Clock;
import java.util.*;

@Component(name = "com.energyict.mdc.processes.keyrenewal.rest",
        service = {Application.class, TranslationKeyProvider.class},
        immediate = true,
        property = {"alias=/pkr", "app=MDC", "name=" + KeyRenewalApplication.COMPONENT_NAME, "version=v1.0"})
public class KeyRenewalApplication extends Application implements TranslationKeyProvider, MessageSeedProvider {

    private static final String APP_KEY = "MDC";
    public static final String COMPONENT_NAME = "PKR";

    private volatile DataModel dataModel;
    private volatile MeteringService meteringService;
    private volatile DeviceService deviceService;
    private volatile Thesaurus thesaurus;
    private volatile NlsService nlsService;
    private volatile MessageService messageService;
    private volatile TransactionService transactionService;
    private volatile ServiceCallService serviceCallService;
    private volatile PropertySpecService propertySpecService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile DeviceMessageSpecificationService deviceMessageSpecificationService;
    private volatile UpgradeService upgradeService;
    private volatile JsonService jsonService;
    private volatile Clock clock;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                ExceptionLogger.class,
                DeviceResource.class
        );
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setDeviceMessageSpecificationService(DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel(COMPONENT_NAME, "Process key renewal");
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    /*@Reference(target = "(name=" + KeyRenewalCustomPropertySet.CUSTOM_PROPERTY_SET_NAME + ")")
       public void setCustomPropertySet(CustomPropertySet customPropertySet) {
           // PATCH; required for proper startup; do not delete
       }*/

       @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        this.dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(MessageService.class).toInstance(messageService);
                bind(ServiceCallService.class).toInstance(serviceCallService);
                bind(CustomPropertySetService.class).toInstance(customPropertySetService);
            }
        });
        upgradeService.register(InstallIdentifier.identifier(KeyRenewalChecklist.APPLICATION_NAME, COMPONENT_NAME), dataModel, Installer.class, Collections.emptyMap());
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(TranslationSeeds.values());
    }

    /*@Reference(target = "(com.elster.jupiter.license.rest.key=" + APP_KEY + ")")
    public void setLicense(License license) {
    }*/

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(meteringService).to(MeteringService.class);
            bind(deviceService).to(DeviceService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(nlsService).to(NlsService.class);
            bind(transactionService).to(TransactionService.class);
            bind(deviceMessageSpecificationService).to(DeviceMessageSpecificationService.class);
            bind(propertySpecService).to(PropertySpecService.class);
            bind(serviceCallService).to(ServiceCallService.class);
            bind(customPropertySetService).to(CustomPropertySetService.class);
            bind(messageService).to(MessageService.class);
            bind(jsonService).to(JsonService.class);
            bind(clock).to(Clock.class);
            bind(DataModel.class).to(DataModel.class);

            bind(ExceptionFactory.class).to(ExceptionFactory.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(ServiceCallCommands.class).to(ServiceCallCommands.class);
            bind(HeadEndController.class).to(HeadEndController.class);
            bind(KeyRenewalCustomPropertySet.class).to(KeyRenewalCustomPropertySet.class);
        }
    }
}