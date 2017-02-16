package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.LocationBuilder;
import com.elster.jupiter.metering.LocationService;
import com.elster.jupiter.metering.LocationTemplate;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.util.InfoFactory;
import com.elster.jupiter.rest.util.PropertyDescriptionInfo;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.util.geo.SpatialCoordinates;
import com.elster.jupiter.util.geo.SpatialCoordinatesFactory;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private volatile LocationService locationService;
    private volatile ThreadPrincipalService threadPrincipalService;

    public UsagePointInfoFactory() {
    }

    @Inject
    public UsagePointInfoFactory(Clock clock,
                                 NlsService nlsService,
                                 MeteringService meteringService,
                                 LicenseService licenseService,
                                 ThreadPrincipalService threadPrincipalService,
                                 LocationService locationService) {
        this();
        this.setClock(clock);
        this.setNlsService(nlsService);
        this.setMeteringService(meteringService);
        this.setLicenseService(licenseService);
        this.setLocationService(locationService);
        this.setThreadPrincipalService(threadPrincipalService);
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

    @Reference
    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
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
        infos.add(createDescription(TranslationSeeds.LOCATION, String.class));
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

    SpatialCoordinates getGeoCoordinates(UsagePointInfo usagePointInfo) {
        if ((usagePointInfo.extendedGeoCoordinates != null) && (usagePointInfo.extendedGeoCoordinates.spatialCoordinates != null)) {
            return new SpatialCoordinatesFactory().fromStringValue(usagePointInfo.extendedGeoCoordinates.spatialCoordinates);
        }
        return null;
    }

    public Location getLocation(UsagePointInfo usagePointInfo) {
        if ((usagePointInfo.extendedLocation.locationId != null) && (usagePointInfo.extendedLocation.locationId == -1)
                && (usagePointInfo.extendedLocation.properties != null) && (usagePointInfo.extendedLocation.properties.length > 0)) {
            List<PropertyInfo> propertyInfoList = Arrays.asList(usagePointInfo.extendedLocation.properties);
            List<String> locationData = propertyInfoList.stream()
                    .map(d -> d.propertyValueInfo.value.toString())
                    .collect(Collectors.toList());
            LocationBuilder builder = meteringService.getServiceCategory(ServiceKind.valueOf(usagePointInfo.serviceCategory.name()))
                    .orElseThrow(IllegalArgumentException::new)
                    .newUsagePoint(
                            usagePointInfo.name,
                            usagePointInfo.installationTime != null ? Instant.ofEpochMilli(usagePointInfo.installationTime) : clock.instant()).newLocationBuilder();
            Map<String, Integer> ranking = meteringService
                    .getLocationTemplate()
                    .getTemplateMembers()
                    .stream()
                    .collect(Collectors.toMap(LocationTemplate.TemplateField::getName,
                            LocationTemplate.TemplateField::getRanking));
            Optional<LocationBuilder.LocationMemberBuilder> memberBuilder = builder.getMemberBuilder(threadPrincipalService.getLocale().getLanguage());
            if (memberBuilder.isPresent()) {
                setLocationAttributes(memberBuilder.get(), locationData, ranking);
            } else {
                setLocationAttributes(builder.member(), locationData, ranking).add();
            }
            return builder.create();
        } else if ((usagePointInfo.extendedLocation.locationId != null) && (usagePointInfo.extendedLocation.locationId > 0)) {
            return locationService.findLocationById(usagePointInfo.extendedLocation.locationId).get();
        }
        return null;
    }

    private LocationBuilder.LocationMemberBuilder setLocationAttributes(LocationBuilder.LocationMemberBuilder builder, List<String> locationData, Map<String, Integer> ranking) {
        builder.setCountryCode(locationData.get(ranking.get("countryCode")))
                .setCountryName(locationData.get(ranking.get("countryName")))
                .setAdministrativeArea(locationData.get(ranking.get("administrativeArea")))
                .setLocality(locationData.get(ranking.get("locality")))
                .setSubLocality(locationData.get(ranking.get("subLocality")))
                .setStreetType(locationData.get(ranking.get("streetType")))
                .setStreetName(locationData.get(ranking.get("streetName")))
                .setStreetNumber(locationData.get(ranking.get("streetNumber")))
                .setEstablishmentType(locationData.get(ranking.get("establishmentType")))
                .setEstablishmentName(locationData.get(ranking.get("establishmentName")))
                .setEstablishmentNumber(locationData.get(ranking.get("establishmentNumber")))
                .setAddressDetail(locationData.get(ranking.get("addressDetail")))
                .setZipCode(locationData.get(ranking.get("zipCode")))
                .isDaultLocation(true)
                .setLocale(threadPrincipalService.getLocale().getLanguage());

        return builder;
    }
}
