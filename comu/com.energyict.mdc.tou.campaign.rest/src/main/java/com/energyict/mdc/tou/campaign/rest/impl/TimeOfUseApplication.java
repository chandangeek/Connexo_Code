/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.rest.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignService;

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


@Component(name = "com.energyict.mdc.tou.campaign.rest",
        service = {Application.class, TranslationKeyProvider.class, MessageSeedProvider.class},
        immediate = true, property = {"alias=/tou", "app=MDC", "name=" + TimeOfUseApplication.COMPONENT_NAME})
public class TimeOfUseApplication extends Application implements TranslationKeyProvider, MessageSeedProvider {

    public static final String COMPONENT_NAME = "TUR";

    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile ServiceCallService serviceCallService;
    private volatile NlsService nlsService;
    private volatile TimeOfUseCampaignService timeOfUseCampaignService;
    private volatile Thesaurus thesaurus;
    private volatile DeviceService deviceService;
    private volatile Clock clock;
    private volatile CalendarService calendarService;
    private volatile TaskService taskService;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                TimeOfUseCampaignResource.class
        );
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new TimeOfUseApplication.HK2Binder());
        return Collections.unmodifiableSet(hashSet);
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
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(TranslationKeys.values());
    }

    @Reference
    public void setTimeOfUseCampaignService(TimeOfUseCampaignService timeOfUseCampaignService) {
        this.timeOfUseCampaignService = timeOfUseCampaignService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST)
                .join(nlsService.getThesaurus(ServiceCallService.COMPONENT_NAME, Layer.DOMAIN));
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setCalendarService(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(thesaurus).to(Thesaurus.class);
            bind(TimeOfUseCampaignResource.class).to(TimeOfUseCampaignResource.class);
            bind(serviceCallService).to(ServiceCallService.class);
            bind(nlsService).to(NlsService.class);
            bind(timeOfUseCampaignService).to(TimeOfUseCampaignService.class);
            bind(TimeOfUseCampaignInfoFactory.class).to(TimeOfUseCampaignInfoFactory.class);
            bind(deviceService).to(DeviceService.class);
            bind(clock).to(Clock.class);
            bind(DeviceInCampaignInfoFactory.class).to(DeviceInCampaignInfoFactory.class);
            bind(DeviceTypeAndOptionsInfoFactory.class).to(DeviceTypeAndOptionsInfoFactory.class);
            bind(deviceConfigurationService).to(DeviceConfigurationService.class);
            bind(ExceptionFactory.class).to(ExceptionFactory.class);
            bind(ConcurrentModificationExceptionFactory.class).to(ConcurrentModificationExceptionFactory.class);
            bind(calendarService).to(CalendarService.class);
            bind(taskService).to(TaskService.class);
        }
    }
}
