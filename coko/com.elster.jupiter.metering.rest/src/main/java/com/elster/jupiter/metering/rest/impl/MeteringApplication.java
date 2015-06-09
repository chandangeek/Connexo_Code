package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.google.common.collect.ImmutableSet;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.validation.MessageInterpolator;
import javax.ws.rs.core.Application;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.metering.rest", service = {Application.class, TranslationKeyProvider.class}, immediate = true, property = {"alias=/mtr", "app=SYS", "name=" + MeteringApplication.COMPONENT_NAME})
public class MeteringApplication extends Application implements TranslationKeyProvider {
    public static final String COMPONENT_NAME = "MTR";

    private volatile MeteringService meteringService;
    private volatile TransactionService transactionService;
    private volatile RestQueryService restQueryService;
    private volatile Thesaurus thesaurus;
    private volatile Clock clock;

    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                UsagePointResource.class,
                ReadingTypeResource.class,
                ReadingTypeFieldResource.class
        );
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setRestQueryService(RestQueryService restQueryService) {
        this.restQueryService = restQueryService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(getComponentName(), getLayer());
    }

    @Activate
    public void activate() {
    }

    @Deactivate
    public void deactivate() {
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
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
        keys.addAll(Arrays.asList(MessageSeeds.values()));
        Arrays.stream(TimeAttribute.values()).forEach(ta -> keys.add(new SimpleTranslationKey(MessageSeeds.Keys.TIME_ATTRIBUTE_KEY_PREFIX + ta.getId(), ta.getDescription())));
        Arrays.stream(MacroPeriod.values()).forEach(mp -> keys.add(new SimpleTranslationKey(MessageSeeds.Keys.MACRO_PERIOD_KEY_PREFIX + mp.getId(), mp.getDescription())));
        Arrays.stream(TranslationSeeds.values()).forEach(keys::add);
        return keys;
    }

    class HK2Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(restQueryService).to(RestQueryService.class);
            bind(transactionService).to(TransactionService.class);
            bind(meteringService).to(MeteringService.class);
            bind(clock).to(Clock.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(thesaurus).to(MessageInterpolator.class);
        }

        ;
    }
}
