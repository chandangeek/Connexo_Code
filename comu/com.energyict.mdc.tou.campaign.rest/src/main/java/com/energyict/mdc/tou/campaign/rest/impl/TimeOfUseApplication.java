/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.rest.impl;

import com.elster.jupiter.cbo.I18N;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignService;

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


@Component(name = "com.energyict.mdc.tou.campaign.rest", service = {Application.class, TranslationKeyProvider.class, MessageSeedProvider.class}, immediate = true, property = {"alias=/tou", "app=MDC", "name=" + TimeOfUseApplication.COMPONENT_NAME})
public class TimeOfUseApplication extends Application implements TranslationKeyProvider, MessageSeedProvider {

    public static final String COMPONENT_NAME = "TUR";

    private volatile ServiceCallService serviceCallService;
    private volatile NlsService nlsService;
    private volatile TimeOfUseCampaignService timeOfUseCampaignService;
    private volatile Thesaurus thesaurus;

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
        return Collections.emptyList();
    }

    @Reference
    public void setTimeOfUseCampaignService(TimeOfUseCampaignService timeOfUseCampaignService) {
        this.timeOfUseCampaignService = timeOfUseCampaignService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST)
                .join(nlsService.getThesaurus(I18N.COMPONENT_NAME, Layer.DOMAIN))
                .join(nlsService.getThesaurus(DeviceMessageSpecificationService.COMPONENT_NAME, Layer.DOMAIN))
                .join(nlsService.getThesaurus(MeteringService.COMPONENTNAME, Layer.DOMAIN))
                .join(nlsService.getThesaurus(TopologyService.COMPONENT_NAME, Layer.DOMAIN))
                .join(nlsService.getThesaurus(SecurityManagementService.COMPONENTNAME, Layer.DOMAIN));
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
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
        }
    }
}
