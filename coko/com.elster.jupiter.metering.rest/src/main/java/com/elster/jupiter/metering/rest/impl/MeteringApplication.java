/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventOrAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.EndDeviceType;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.metering.LocationService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.rest.util.RestValidationExceptionMapper;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.validation.MessageInterpolator;
import javax.ws.rs.core.Application;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Component(name = "com.elster.jupiter.metering.rest", service = {Application.class, TranslationKeyProvider.class, MessageSeedProvider.class}, immediate = true,
        property = {"alias=/mtr", "app=SYS", "name=" + MeteringApplication.COMPONENT_NAME})
public class MeteringApplication extends Application implements TranslationKeyProvider, MessageSeedProvider {
    public static final String COMPONENT_NAME = "MTR";

    private volatile MeteringService meteringService;
    private volatile TransactionService transactionService;
    private volatile RestQueryService restQueryService;
    private volatile ServiceCallService serviceCallService;
    private volatile Thesaurus thesaurus;
    private volatile Clock clock;
    private volatile NlsService nlsService;
    private volatile MetrologyConfigurationService metrologyConfigurationService;
    private volatile LicenseService licenseService;
    private volatile PropertyValueInfoService propertyValueInfoService;
    private volatile LocationService locationService;
    private volatile ThreadPrincipalService threadPrincipalService;

    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                MetrologyConfigurationResource.class,
                UsagePointResource.class,
                DeviceResource.class,
                ReadingTypeResource.class,
                MeteringFieldResource.class,
                ServiceCategoryResource.class,
                EndDeviceEventTypeResource.class,
                RestValidationExceptionMapper.class,
                GasDayResource.class
                );
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
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
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(getComponentName(), getLayer()).join(nlsService.getThesaurus(getComponentName(), Layer.DOMAIN));
    }

    @Reference
    public void setLicenseService(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @Reference
    public void setPropertyValueInfoService(PropertyValueInfoService propertyValueInfoService) {
        this.propertyValueInfoService = propertyValueInfoService;
    }

    @Reference
    public void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Reference
    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
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
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
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
        keys.addAll(Arrays.asList(LocationTranslationKeys.values()));
        return keys;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    class HK2Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(restQueryService).to(RestQueryService.class);
            bind(nlsService).to(NlsService.class);
            bind(transactionService).to(TransactionService.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(meteringService).to(MeteringService.class);
            bind(metrologyConfigurationService).to(MetrologyConfigurationService.class);
            bind(clock).to(Clock.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(thesaurus).to(MessageInterpolator.class);
            bind(ExceptionFactory.class).to(ExceptionFactory.class);
            bind(ReadingTypeInfoFactory.class).to(ReadingTypeInfoFactory.class);
            bind(CustomPropertySetInfoFactory.class).to(CustomPropertySetInfoFactory.class);
            bind(UsagePointInfoFactory.class).to(UsagePointInfoFactory.class);
            bind(serviceCallService).to(ServiceCallService.class);
            bind(MetrologyConfigurationInfoFactory.class).to(MetrologyConfigurationInfoFactory.class);
            bind(ResourceHelper.class).to(ResourceHelper.class);
            bind(licenseService).to(LicenseService.class);
            bind(propertyValueInfoService).to(PropertyValueInfoService.class);
            bind(ReadingTypeFilterFactory.class).to(ReadingTypeFilterFactory.class);
            bind(locationService).to(LocationService.class);
            bind(threadPrincipalService).to(ThreadPrincipalService.class);
        }
    }
}