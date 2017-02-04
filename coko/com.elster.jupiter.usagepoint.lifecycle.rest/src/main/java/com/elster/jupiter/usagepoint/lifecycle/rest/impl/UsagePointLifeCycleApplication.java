/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.rest.impl;

import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.RestValidationExceptionMapper;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointMicroActionFactory;
import com.elster.jupiter.usagepoint.lifecycle.rest.BusinessProcessInfoFactory;
import com.elster.jupiter.usagepoint.lifecycle.rest.MicroActionAndCheckInfoFactory;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleInfoFactory;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCyclePrivilegeInfoFactory;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleStageInfoFactory;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleStateInfoFactory;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleTransitionInfoFactory;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointTransitionInfoFactory;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(name = "UsagePointLifeCycleApplication",
        service = {Application.class, TranslationKeyProvider.class, MessageSeedProvider.class},
        immediate = true,
        property = {"alias=/upl", "app=SYS", "name=" + UsagePointLifeCycleApplication.COMPONENT_NAME})
public class UsagePointLifeCycleApplication extends Application implements TranslationKeyProvider, MessageSeedProvider {
    public static final String COMPONENT_NAME = "UPL";

    private Thesaurus thesaurus;
    private PropertyValueInfoService propertyValueInfoService;
    private UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService;
    private UsagePointLifeCycleService usagePointLifeCycleService;
    private FiniteStateMachineService finiteStateMachineService;
    private MeteringService meteringService;
    private UsagePointMicroActionFactory usagePointMicroActionFactory;

    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                RestValidationExceptionMapper.class,
                UsagePointLifeCycleResource.class,
                UsagePointLifeCycleStatesResource.class,
                UsagePointLifeCycleTransitionsResource.class,
                UsagePointStateChangeRequestResource.class);
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
        this.thesaurus = nlsService.getThesaurus(UsagePointLifeCycleApplication.COMPONENT_NAME, Layer.REST)
                .join(nlsService.getThesaurus(UsagePointLifeCycleConfigurationService.COMPONENT_NAME, Layer.DOMAIN));
    }

    @Reference
    public void setPropertyValueInfoService(PropertyValueInfoService propertyValueInfoService) {
        this.propertyValueInfoService = propertyValueInfoService;
    }

    @Reference
    public void setUsagePointLifeCycleConfigurationService(UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService) {
        this.usagePointLifeCycleConfigurationService = usagePointLifeCycleConfigurationService;
    }

    @Reference
    public void setUsagePointLifeCycleService(UsagePointLifeCycleService usagePointLifeCycleService) {
        this.usagePointLifeCycleService = usagePointLifeCycleService;
    }

    @Reference
    public void setFiniteStateMachineService(FiniteStateMachineService finiteStateMachineService) {
        this.finiteStateMachineService = finiteStateMachineService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setUsagePointMicroActionFactory(UsagePointMicroActionFactory usagePointMicroActionFactory) {
        this.usagePointMicroActionFactory = usagePointMicroActionFactory;
    }

    class HK2Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(UsagePointLifeCycleInfoFactory.class).to(UsagePointLifeCycleInfoFactory.class);
            bind(UsagePointLifeCycleStateInfoFactory.class).to(UsagePointLifeCycleStateInfoFactory.class);
            bind(UsagePointLifeCycleStageInfoFactory.class).to(UsagePointLifeCycleStageInfoFactory.class);
            bind(BusinessProcessInfoFactory.class).to(BusinessProcessInfoFactory.class);
            bind(UsagePointLifeCyclePrivilegeInfoFactory.class).to(UsagePointLifeCyclePrivilegeInfoFactory.class);
            bind(MicroActionAndCheckInfoFactory.class).to(MicroActionAndCheckInfoFactory.class);
            bind(UsagePointLifeCycleTransitionInfoFactory.class).to(UsagePointLifeCycleTransitionInfoFactory.class);
            bind(UsagePointTransitionInfoFactory.class).to(UsagePointTransitionInfoFactory.class);
            bind(UsagePointStateChangeRequestInfoFactory.class).to(UsagePointStateChangeRequestInfoFactory.class);
            bind(ExceptionFactory.class).to(ExceptionFactory.class);
            bind(ResourceHelper.class).to(ResourceHelper.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(propertyValueInfoService).to(PropertyValueInfoService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(usagePointLifeCycleConfigurationService).to(UsagePointLifeCycleConfigurationService.class);
            bind(usagePointLifeCycleService).to(UsagePointLifeCycleService.class);
            bind(finiteStateMachineService).to(FiniteStateMachineService.class);
            bind(meteringService).to(MeteringService.class);
            bind(usagePointMicroActionFactory).to(UsagePointMicroActionFactory.class);
        }
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
        return Arrays.asList(TranslationKeys.values());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }
}

