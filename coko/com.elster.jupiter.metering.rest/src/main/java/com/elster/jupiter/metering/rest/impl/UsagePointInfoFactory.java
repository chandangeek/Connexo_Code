/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.InfoFactory;
import com.elster.jupiter.rest.util.PropertyDescriptionInfo;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory class to create Info objects. This class will register on the InfoFactoryWhiteboard and is used by DynamicSearch.
 * Created by bvn on 6/9/15.
 */
@Component(name = "usagepoint.info.factory", service = {InfoFactory.class}, immediate = true)
public class UsagePointInfoFactory implements InfoFactory<UsagePoint> {

    private volatile Clock clock;
    private volatile Thesaurus thesaurus;
    private volatile MeteringService meteringService;
    private volatile LicenseService licenseService;

    public UsagePointInfoFactory() {
    }

    @Inject
    public UsagePointInfoFactory(Clock clock,
                                 NlsService nlsService,
                                 MeteringService meteringService,
                                 LicenseService licenseService) {
        this();
        this.setClock(clock);
        this.setNlsService(nlsService);
        this.setMeteringService(meteringService);
        this.setLicenseService(licenseService);
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(MeteringApplication.COMPONENT_NAME, Layer.REST)
                .join(nlsService.getThesaurus(MeteringApplication.COMPONENT_NAME, Layer.DOMAIN));
    }

    @Reference
    public void setLicenseService(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @Override
    public UsagePointTranslatedInfo from(UsagePoint usagePoint) {
        UsagePointTranslatedInfo info = new UsagePointTranslatedInfo(usagePoint, clock);
        info.displayServiceCategory = usagePoint.getServiceCategory().getKind().getDisplayName(thesaurus);
        usagePoint.getCurrentEffectiveMetrologyConfiguration()
                .map(EffectiveMetrologyConfigurationOnUsagePoint::getMetrologyConfiguration)
                .ifPresent(metrologyConfiguration -> info.displayMetrologyConfiguration = metrologyConfiguration.getName());
        return info;
    }

    @Override
    public List<PropertyDescriptionInfo> modelStructure() {
        List<PropertyDescriptionInfo> infos = new ArrayList<>();
        infos.add(createDescription(TranslationSeeds.NAME, String.class));
        infos.add(createDescription(TranslationSeeds.SERVICECATEGORY_DISPLAY, String.class));
        infos.add(createDescription(TranslationSeeds.METROLOGY_CONFIGURATION_DISPLAY, String.class));
        infos.add(createDescription(TranslationSeeds.MRID, String.class));
        infos.add(createDescription(TranslationSeeds.INSTALLATION_TIME, Instant.class));
        return infos;
    }

    private PropertyDescriptionInfo createDescription(TranslationSeeds propertyName, Class<?> aClass) {
        return new PropertyDescriptionInfo(propertyName.getKey(), aClass, thesaurus.getString(propertyName.getKey(), propertyName.getDefaultFormat()));
    }

    @Override
    public Class getDomainClass() {
        if (licenseService.getLicenseForApplication("INS").isPresent()) {
            return EmptyDomain.class;
        }
        return UsagePoint.class;
    }

    public UsagePointBuilder newUsagePointBuilder(UsagePointInfo usagePointInfo) {
        return meteringService.getServiceCategory(usagePointInfo.serviceCategory)
                .orElseThrow(IllegalArgumentException::new)
                .newUsagePoint(
                        usagePointInfo.name,
                        usagePointInfo.installationTime != null ? Instant.ofEpochMilli(usagePointInfo.installationTime) : clock.instant());
    }

    static class EmptyDomain {
    }
}
