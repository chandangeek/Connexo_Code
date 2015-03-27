package com.energyict.mdc.firmware.rest;

import com.elster.jupiter.nls.*;
import com.elster.jupiter.rest.util.*;
import com.energyict.mdc.common.rest.TransactionWrapper;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.rest.impl.FirmwareFieldResource;
import com.energyict.mdc.firmware.rest.impl.FirmwareVersionResource;
import com.energyict.mdc.firmware.rest.impl.MessageSeeds;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import com.elster.jupiter.transaction.TransactionService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.*;

@Component(name = "com.energyict.mdc.firmware.rest", service = {Application.class, TranslationKeyProvider.class}, immediate = true, property = {"alias=/fwc", "app=MDC", "name=" + FirmwareApplication.COMPONENT_NAME})
public class FirmwareApplication extends Application implements BinderProvider, TranslationKeyProvider {
    public static final String APP_KEY = "MDC";
    public static final String COMPONENT_NAME = "FWR";

    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private volatile RestQueryService restQueryService;
    private volatile TransactionService transactionService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile FirmwareService firmwareService;


    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                FirmwareVersionResource.class,
                FirmwareFieldResource.class,
                TransactionWrapper.class,
                ConstraintViolationExceptionMapper.class,
                LocalizedFieldValidationExceptionMapper.class,
                JsonMappingExceptionMapper.class,
                LocalizedExceptionMapper.class
        );
    }

    @Override
    public Binder getBinder() {
        return new AbstractBinder() {
            @Override
            protected void configure() {
                bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
                bind(transactionService).to(TransactionService.class);
                bind(nlsService).to(NlsService.class);
                bind(thesaurus).to(Thesaurus.class);
                bind(restQueryService).to(RestQueryService.class);
                bind(deviceConfigurationService).to(DeviceConfigurationService.class);
                bind(firmwareService).to(FirmwareService.class);
            }
        };
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
    public List<TranslationKey> getKeys() {
        List<TranslationKey> keys = new ArrayList<>();
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
                keys.add(messageSeed);
        }
        return keys;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
    }

    @Reference
    public void setRestQueryService(RestQueryService restQueryService) {
        this.restQueryService = restQueryService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setFirmwareService(FirmwareService firmwareService) {
        this.firmwareService = firmwareService;
    }
}
