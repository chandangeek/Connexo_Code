package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.data.DeviceService;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(name = "com.energyict.mdc.usagepoint.data.rest", service = {Application.class, TranslationKeyProvider.class}, immediate = true,
        property = {"alias=/upr", "app=MDC", "name=" + UsagePointApplication.COMPONENT_NAME})
public class UsagePointApplication extends Application implements TranslationKeyProvider {
    public static final String COMPONENT_NAME = "UPR";

    private volatile DeviceService deviceService;
    private volatile MeteringService meteringService;
    private volatile TransactionService transactionService;
    private volatile Thesaurus thesaurus;
    private volatile NlsService nlsService;

    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(UsagePointResource.class);
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(getComponentName(), getLayer()).join(nlsService.getThesaurus(getComponentName(), Layer.DOMAIN))
                .join(nlsService.getThesaurus("DLR", Layer.REST))
                .join(nlsService.getThesaurus("MTR", Layer.REST))
                .join(nlsService.getThesaurus("MTR", Layer.DOMAIN));
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
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
        return keys;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    class HK2Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(deviceService).to(DeviceService.class);
            bind(meteringService).to(MeteringService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(nlsService).to(NlsService.class);
            bind(transactionService).to(TransactionService.class);
            bind(ExceptionFactory.class).to(ExceptionFactory.class);
            bind(MeterInfoFactory.class).to(MeterInfoFactory.class);
            bind(UsagePointChannelInfoFactory.class).to(UsagePointChannelInfoFactory.class);
            bind(ReadingTypeInfoFactory.class).to(ReadingTypeInfoFactory.class);
        }
    }
}