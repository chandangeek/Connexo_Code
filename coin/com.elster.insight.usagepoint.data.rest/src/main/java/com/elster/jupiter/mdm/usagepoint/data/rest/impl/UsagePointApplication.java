package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.rest.PropertyUtils;
import com.elster.jupiter.license.License;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.*;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.rest.util.RestValidationExceptionMapper;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.rest.ServiceCallInfoFactory;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.ValidationRuleInfoFactory;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.time.Clock;
import java.util.*;

@Component(name = "com.elster.insight.udr.rest",
        service = {Application.class, TranslationKeyProvider.class},
        immediate = true,
        property = {"alias=/udr", "app=INS", "name=" + UsagePointApplication.COMPONENT_NAME})
public class UsagePointApplication extends Application implements TranslationKeyProvider {
    public static final String APP_KEY = "INS";
    public static final String COMPONENT_NAME = "UDR";

    private volatile TransactionService transactionService;
    private volatile Thesaurus thesaurus;
    private volatile MeteringService meteringService;
    private volatile RestQueryService restQueryService;
    private volatile Clock clock;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile UsagePointConfigurationService usagePointConfigurationService;
    private volatile ValidationService validationService;
    private volatile EstimationService estimationService;
    private volatile UsagePointDataService usagePointDataService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile ServiceCallService serviceCallService;
    private volatile ServiceCallInfoFactory serviceCallInfoFactory;
    private volatile License license;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                ChannelResource.class,
                UsagePointResource.class,
                RegisterResource.class,
                DeviceResource.class,
                UsagePointGroupResource.class,
                UsagePointValidationResource.class,
                RegisterDataResource.class,
                UsagePointCustomPropertySetResource.class,
                RestValidationExceptionMapper.class
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
        return Arrays.asList(DefaultTranslationKey.values());
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setMeteringGroupService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }


    @Reference
    public void setRestQueryService(RestQueryService restQueryService) {
        this.restQueryService = restQueryService;
    }

    @Reference
    public void setClockService(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setUsagePointConfigurationService(UsagePointConfigurationService usagePointConfigurationService) {
        this.usagePointConfigurationService = usagePointConfigurationService;
    }

    @Reference
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Reference
    public void setEstimationService(EstimationService estimationService) {
        this.estimationService = estimationService;
    }

    @Reference
    public void setUsagePointDataService(UsagePointDataService usagePointDataService) {
        this.usagePointDataService = usagePointDataService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setServiceCallInfoFactory(ServiceCallInfoFactory serviceCallInfoFactory) {
        this.serviceCallInfoFactory = serviceCallInfoFactory;
    }

    @Reference(target = "(com.elster.jupiter.license.application.key=" + APP_KEY + ")")
    public void setLicense(License license) {
        this.license = license;
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(transactionService).to(TransactionService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(meteringService).to(MeteringService.class);
            bind(meteringGroupsService).to(MeteringGroupsService.class);
            bind(restQueryService).to(RestQueryService.class);
            bind(clock).to(Clock.class);
            bind(usagePointConfigurationService).to(UsagePointConfigurationService.class);
            bind(validationService).to(ValidationService.class);
            bind(estimationService).to(EstimationService.class);
            bind(usagePointDataService).to(UsagePointDataService.class);
            bind(customPropertySetService).to(CustomPropertySetService.class);
            bind(serviceCallService).to(ServiceCallService.class);
            bind(serviceCallInfoFactory).to(ServiceCallInfoFactory.class);

            bind(ExceptionFactory.class).to(ExceptionFactory.class);
            bind(ResourceHelper.class).to(ResourceHelper.class);
            bind(ChannelResourceHelper.class).to(ChannelResourceHelper.class);
            bind(RegisterResourceHelper.class).to(RegisterResourceHelper.class);
            bind(UsagePointDataInfoFactory.class).to(UsagePointDataInfoFactory.class);
            bind(UsagePointGroupInfoFactory.class).to(UsagePointGroupInfoFactory.class);
            bind(com.elster.jupiter.validation.rest.PropertyUtils.class).to(com.elster.jupiter.validation.rest.PropertyUtils.class);
            bind(ValidationInfoFactory.class).to(ValidationInfoFactory.class);
            bind(EstimationRuleInfoFactory.class).to(EstimationRuleInfoFactory.class);
            bind(ValidationRuleInfoFactory.class).to(ValidationRuleInfoFactory.class);
            bind(PropertyUtils.class).to(PropertyUtils.class);
            bind(CustomPropertySetInfoFactory.class).to(CustomPropertySetInfoFactory.class);
            bind(UsagePointInfoFactory.class).to(UsagePointInfoFactory.class);
        }
    }
}