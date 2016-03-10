package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.cbo.*;
import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.*;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.rest.util.RestValidationExceptionMapper;
import com.elster.jupiter.transaction.TransactionService;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.validation.MessageInterpolator;
import javax.ws.rs.core.Application;
import java.time.Clock;
import java.util.*;

@Component(name = "com.elster.jupiter.metering.rest", service = {Application.class, TranslationKeyProvider.class}, immediate = true,
        property = {"alias=/mtr", "app=SYS", "name=" + MeteringApplication.COMPONENT_NAME})
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
                DeviceResource.class,
                ReadingTypeResource.class,
                MeteringFieldResource.class,
                ServiceCategoryResource.class,
                EndDeviceEventTypeResource.class,
                RestValidationExceptionMapper.class);
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
        this.thesaurus = nlsService.getThesaurus(getComponentName(), getLayer()).join(nlsService.getThesaurus(getComponentName(), Layer.DOMAIN));
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
        List<TranslationKey> keys = new ArrayList<>(Arrays.asList(TranslationKeys.values()));
        for (int i = 1; i < TimeAttribute.values().length; i++) {
            TimeAttribute ta = TimeAttribute.values()[i];
            keys.add(new SimpleTranslationKey(TranslationKeys.Keys.TIME_ATTRIBUTE_KEY_PREFIX + ta.getId(), ta.getDescription()));
        }
        for (int i = 1; i < MacroPeriod.values().length; i++) {
            MacroPeriod mp = MacroPeriod.values()[i];
            keys.add(new SimpleTranslationKey(TranslationKeys.Keys.MACRO_PERIOD_KEY_PREFIX + mp.getId(), mp.getDescription()));
        }
        for (EndDeviceType type : EndDeviceType.values()) {
            keys.add(new SimpleTranslationKey(type.name(), type.getMnemonic()));
        }
        for (EndDeviceDomain domain : EndDeviceDomain.values()) {
            keys.add(new SimpleTranslationKey(domain.name(), domain.getMnemonic()));
        }
        for (EndDeviceSubDomain subDomain : EndDeviceSubDomain.values()) {
            keys.add(new SimpleTranslationKey(subDomain.name(), subDomain.getMnemonic()));
        }
        for (EndDeviceEventOrAction eventOrAction : EndDeviceEventOrAction.values()) {
            keys.add(new SimpleTranslationKey(eventOrAction.name(), eventOrAction.getMnemonic()));
        }
        keys.addAll(Arrays.asList(TranslationSeeds.values()));
        return keys;
    }

    class HK2Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(restQueryService).to(RestQueryService.class);
            bind(transactionService).to(TransactionService.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(meteringService).to(MeteringService.class);
            bind(clock).to(Clock.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(thesaurus).to(MessageInterpolator.class);
            bind(ExceptionFactory.class).to(ExceptionFactory.class);
            bind(ReadingTypeInfoFactory.class).to(ReadingTypeInfoFactory.class);
            bind(CustomPropertySetInfoFactory.class).to(CustomPropertySetInfoFactory.class);
            bind(UsagePointInfoFactory.class).to(UsagePointInfoFactory.class);
        }
    }
}