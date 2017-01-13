package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(
        name = "com.elster.jupiter.estimation.rest",
        service = {Application.class, MessageSeedProvider.class, TranslationKeyProvider.class},
        immediate = true,
        property = {"alias=/est", "app=SYS", "name=" + EstimationApplication.COMPONENT_NAME})
public class EstimationApplication extends Application implements MessageSeedProvider, TranslationKeyProvider {
    static final String COMPONENT_NAME = "EST";

    private volatile EstimationService estimationService;
    private volatile TransactionService transactionService;
    private volatile RestQueryService restQueryService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile MetrologyConfigurationService metrologyConfigurationService;
    private volatile TimeService timeService;
    private volatile PropertyValueInfoService propertyValueInfoService;

    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;

    public EstimationApplication() {
    }

    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                EstimationResource.class,
                MeterGroupsResource.class,
                FieldResouce.class);
    }

    @Activate
    public void activate() {
        propertyValueInfoService.addPropertyValueInfoConverter(new AdvanceReadingsSettingsValueConverter());
    }

    @Reference
    public void setEstimationService(EstimationService estimationService) {
        this.estimationService = estimationService;
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
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(EstimationApplication.COMPONENT_NAME, Layer.REST)
                .join(nlsService.getThesaurus(MeteringService.COMPONENTNAME, Layer.DOMAIN));
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    @Reference
    public void setPropertyValueInfoService(PropertyValueInfoService propertyValueInfoService) {
        this.propertyValueInfoService = propertyValueInfoService;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
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
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(TranslationKeys.values());
    }

    class HK2Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(restQueryService).to(RestQueryService.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(nlsService).to(NlsService.class);
            bind(estimationService).to(EstimationService.class);
            bind(transactionService).to(TransactionService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(meteringGroupsService).to(MeteringGroupsService.class);
            bind(metrologyConfigurationService).to(MetrologyConfigurationService.class);
            bind(timeService).to(TimeService.class);
            bind(propertyValueInfoService).to(PropertyValueInfoService.class);
            bind(ReadingTypeInfoFactory.class).to(ReadingTypeInfoFactory.class);
            bind(EstimationRuleInfoFactory.class).to(EstimationRuleInfoFactory.class);
        }
    }
}
