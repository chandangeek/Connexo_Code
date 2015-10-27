package com.elster.insight.usagepoint.config.rest.impl;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.core.Application;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.insight.common.rest.ExceptionFactory;
import com.elster.insight.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.collect.ImmutableSet;

@Component(name = "com.elster.insight.ucr.rest", service = {Application.class, TranslationKeyProvider.class}, immediate = true, property = {"alias=/ucr", "app=INS", "name=" + UsagePointConfigurationApplication.COMPONENT_NAME})
public class UsagePointConfigurationApplication extends Application implements TranslationKeyProvider {

    private final Logger logger = Logger.getLogger(UsagePointConfigurationApplication.class.getName());

    public static final String APP_KEY = "INS";
    public static final String COMPONENT_NAME = "UCR";


    private volatile TransactionService transactionService;
    private volatile NlsService nlsService;
    private volatile JsonService jsonService;
    private volatile Thesaurus thesaurus;
    private volatile Clock clock;
    private volatile UsagePointConfigurationService usagePointConfigurationService;
    private volatile ValidationService validationService;

    @Override
    public Set<Class<?>> getClasses() {
                return ImmutableSet.of(
                        MetrologyConfigurationResource.class
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
        Set<String> uniqueIds = new HashSet<>();
        List<TranslationKey> keys = new ArrayList<>();
        keys.addAll(Arrays.asList(DefaultTranslationKey.values()));
        return keys;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }
    
    @Reference
    public void setUsagePointConfigurationService(UsagePointConfigurationService usagePointConfigurationService) {
        this.usagePointConfigurationService = usagePointConfigurationService;
    }

    @Reference
    public void setClockService(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(MetrologyConfigurationResource.class).to(MetrologyConfigurationResource.class);
            bind(ExceptionFactory.class).to(ExceptionFactory.class);
            bind(transactionService).to(TransactionService.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(nlsService).to(NlsService.class);
            bind(jsonService).to(JsonService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(clock).to(Clock.class);
            bind(usagePointConfigurationService).to(UsagePointConfigurationService.class);
            bind(validationService).to(ValidationService.class);
        }
    }
}