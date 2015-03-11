package com.energyict.mdc.firmware.rest;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.nls.*;
import com.elster.jupiter.rest.util.*;
import com.energyict.mdc.common.rest.TransactionWrapper;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.firmware.rest.impl.FirmwareVersionResource;
import com.energyict.mdc.firmware.rest.impl.MessageSeeds;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.omg.IOP.TransactionService;
import org.osgi.service.component.annotations.Component;

import javax.ws.rs.core.Application;
import java.util.*;

@Component(name = "com.energyict.mdc.firmware.rest", service = Application.class, immediate = true, property = {"alias=/frw", "app=MDC", "name=" + FirmwareApplication.COMPONENT_NAME})
public class FirmwareApplication extends Application implements BinderProvider, TranslationKeyProvider {
    public static final String APP_KEY = "MDC";
    public static final String COMPONENT_NAME = "FWC";

    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private volatile QueryService queryService;
    private volatile TransactionService transactionService;
    private volatile DeviceConfigurationService deviceConfigurationService;


    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                FirmwareVersionResource.class,
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
                bind(queryService).to(QueryService.class);
                bind(deviceConfigurationService).to(DeviceConfigurationService.class);
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
}
