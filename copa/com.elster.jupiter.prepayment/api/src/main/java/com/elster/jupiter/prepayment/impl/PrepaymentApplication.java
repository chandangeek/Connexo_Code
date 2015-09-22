package com.elster.jupiter.prepayment.impl;

import com.elster.jupiter.license.License;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.ExceptionLogger;
import com.energyict.mdc.common.rest.TransactionWrapper;
import com.energyict.mdc.device.data.DeviceService;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@Component(name = "com.elster.jupiter.prepayment.rest",
        service = {Application.class, TranslationKeyProvider.class},
        immediate = true,
        property = {"alias=/rkn", "app=MDC", "name=" + PrepaymentApplication.COMPONENT_NAME, "version=v1.0"})
public class PrepaymentApplication extends Application implements TranslationKeyProvider {

    private final Logger logger = Logger.getLogger(PrepaymentApplication.class.getName());

    public static final String APP_KEY = "MDC";
    public static final String COMPONENT_NAME = "RKN"; // Redknee

    private volatile DeviceService deviceService;
    private volatile MeteringService meteringService;
    private volatile Thesaurus thesaurus;
    private volatile NlsService nlsService;
    private volatile TransactionService transactionService;
    private volatile Clock clock;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                TransactionWrapper.class,
                ExceptionLogger.class,
                UsagePointResource.class
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
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
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
        return Arrays.asList(TranslationSeeds.values());
    }

    @Reference(target = "(com.elster.jupiter.license.rest.key=" + APP_KEY + ")")
    public void setLicense(License license) {
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(deviceService).to(DeviceService.class);
            bind(meteringService).to(MeteringService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(nlsService).to(NlsService.class);
            bind(transactionService).to(TransactionService.class);
            bind(clock).to(Clock.class);

            bind(ExceptionFactory.class).to(ExceptionFactory.class);
        }
    }

}