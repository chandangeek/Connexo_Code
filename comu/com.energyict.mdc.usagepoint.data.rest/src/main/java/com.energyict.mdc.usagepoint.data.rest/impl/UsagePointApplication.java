/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.ValidationRuleInfoFactory;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;

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

@Component(name = "com.energyict.mdc.usagepoint.data.rest", service = {Application.class, MessageSeedProvider.class}, immediate = true,
        property = {"alias=/upr", "app=MDC", "name=" + UsagePointApplication.COMPONENT_NAME})
public class UsagePointApplication extends Application implements MessageSeedProvider {
    public static final String COMPONENT_NAME = "UPR";

    private volatile DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private volatile DeviceService deviceService;
    private volatile MeteringService meteringService;
    private volatile TransactionService transactionService;
    private volatile Thesaurus thesaurus;
    private volatile NlsService nlsService;
    private volatile ValidationService validationService;
    private volatile Clock clock;

    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(UsagePointResource.class);
    }

    @Reference
    public void setDeviceLifeCycleConfigurationService(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, getLayer())
                .join(nlsService.getThesaurus("DLR", Layer.REST));
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
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

    class HK2Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(deviceLifeCycleConfigurationService).to(DeviceLifeCycleConfigurationService.class);
            bind(deviceService).to(DeviceService.class);
            bind(meteringService).to(MeteringService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(nlsService).to(NlsService.class);
            bind(transactionService).to(TransactionService.class);
            bind(validationService).to(ValidationService.class);
            bind(clock).to(Clock.class);
            bind(ExceptionFactory.class).to(ExceptionFactory.class);
            bind(MeterInfoFactory.class).to(MeterInfoFactory.class);
            bind(UsagePointChannelInfoFactory.class).to(UsagePointChannelInfoFactory.class);
            bind(ReadingTypeInfoFactory.class).to(ReadingTypeInfoFactory.class);
            bind(ResourceHelper.class).to(ResourceHelper.class);
            bind(ChannelDataInfoFactory.class).to(ChannelDataInfoFactory.class);
            bind(ValidationRuleInfoFactory.class).to(ValidationRuleInfoFactory.class);
        }
    }
}
