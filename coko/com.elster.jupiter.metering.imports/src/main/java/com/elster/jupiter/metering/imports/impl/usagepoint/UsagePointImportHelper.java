package com.elster.jupiter.metering.imports.impl.usagepoint;

import com.elster.jupiter.metering.LocationBuilder;
import com.elster.jupiter.metering.LocationTemplate;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.imports.impl.MessageSeeds;
import com.elster.jupiter.metering.imports.impl.MeteringDataImporterContext;
import com.elster.jupiter.metering.imports.impl.exceptions.ProcessorException;
import com.elster.jupiter.util.geo.SpatialCoordinatesFactory;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class UsagePointImportHelper {

    private MeteringDataImporterContext context;
    private Clock clock;

    public UsagePointImportHelper(MeteringDataImporterContext context, Clock clock) {
        this.context = context;
        this.clock = clock;
    }

    public UsagePoint createUsagePointForInsight(UsagePointBuilder usagePointBuilder, UsagePointImportRecord data) {
        usagePointBuilder.withIsSdp(data.isSdp());
        boolean isVirtual = data.isVirtual();
        List<String> locationData = data.getLocation();
        List<String> geoCoordinatesData = data.getGeoCoordinates();

        if (locationData.stream().anyMatch(s -> s != null)) {
            context.getMeteringService()
                    .getLocationTemplate()
                    .getTemplateMembers()
                    .stream()
                    .filter(LocationTemplate.TemplateField::isMandatory)
                    .forEach(field -> {
                        if (locationData.get(field.getRanking()) == null) {
                            throw new ProcessorException(MessageSeeds.LINE_MISSING_LOCATION_VALUE, data.getLineNumber(), field
                                    .getName());
                        } else if (locationData.get(field.getRanking()).isEmpty()) {
                            throw new ProcessorException(MessageSeeds.LINE_MISSING_LOCATION_VALUE, data.getLineNumber(), field
                                    .getName());
                        }
                    });
            LocationBuilder builder = usagePointBuilder.newLocationBuilder();
            Map<String, Integer> ranking = context.getMeteringService()
                    .getLocationTemplate()
                    .getTemplateMembers()
                    .stream()
                    .collect(Collectors.toMap(LocationTemplate.TemplateField::getName, LocationTemplate.TemplateField::getRanking));

            Optional<LocationBuilder.LocationMemberBuilder> memberBuilder = builder.getMemberBuilder(locationData.get(ranking
                    .get("locale")));
            if (memberBuilder.isPresent()) {
                setLocationAttributes(memberBuilder.get(), data, ranking);
            } else {
                setLocationAttributes(builder.member(), data, ranking).add();
            }
            usagePointBuilder.withLocation(builder.create());
            isVirtual = false;
        }
        if (geoCoordinatesData != null && !geoCoordinatesData.isEmpty() && !geoCoordinatesData.contains(null)) {
            usagePointBuilder.withGeoCoordinates(new SpatialCoordinatesFactory().fromStringValue((geoCoordinatesData.stream()
                    .collect(Collectors.joining(":")))));
            isVirtual = false;
        }
        usagePointBuilder.withIsVirtual(isVirtual);
        usagePointBuilder.withOutageRegion(data.getOutageRegion());
        usagePointBuilder.withReadRoute(data.getReadRoute());
        usagePointBuilder.withServicePriority(data.getServicePriority());
        usagePointBuilder.withServiceDeliveryRemark(data.getServiceDeliveryRemark());
        return usagePointBuilder.create();
    }

    public UsagePoint createUsagePointForMultiSense(UsagePointBuilder usagePointBuilder, UsagePointImportRecord data) {
        usagePointBuilder.withIsSdp(false);
        usagePointBuilder.withIsVirtual(true);
        UsagePoint usagePoint = usagePointBuilder.create();
        usagePoint.addDetail(usagePoint.getServiceCategory().newUsagePointDetail(usagePoint, clock.instant()));
        usagePoint.update();
        setMetrologyConfigurationForUsagePoint(data, usagePoint);
        return usagePoint;
    }

    public UsagePoint updateUsagePoint(UsagePoint usagePoint, UsagePointImportRecord data) {
        List<String> locationData = data.getLocation();
        List<String> geoCoordinatesData = data.getGeoCoordinates();

        if (locationData.stream().anyMatch(s -> s != null)) {
            context.getMeteringService().getLocationTemplate().getTemplateMembers().stream()
                    .filter(LocationTemplate.TemplateField::isMandatory)
                    .forEach(field -> {
                        if (locationData.get(field.getRanking()) == null) {
                            throw new ProcessorException(MessageSeeds.LINE_MISSING_LOCATION_VALUE, data.getLineNumber(), field
                                    .getName());
                        } else if (locationData.get(field.getRanking()).isEmpty()) {
                            throw new ProcessorException(MessageSeeds.LINE_MISSING_LOCATION_VALUE, data.getLineNumber(), field
                                    .getName());
                        }
                    });
            LocationBuilder builder = usagePoint.updateLocation();
            Map<String, Integer> ranking = context.getMeteringService()
                    .getLocationTemplate()
                    .getTemplateMembers()
                    .stream()
                    .collect(Collectors.toMap(LocationTemplate.TemplateField::getName, LocationTemplate.TemplateField::getRanking));

            Optional<LocationBuilder.LocationMemberBuilder> memberBuilder = builder.getMemberBuilder(locationData.get(ranking
                    .get("locale")));
            if (memberBuilder.isPresent()) {
                setLocationAttributes(memberBuilder.get(), data, ranking);
            } else {
                setLocationAttributes(builder.member(), data, ranking).add();
            }
            usagePoint.setLocation(builder.create().getId());
        }
        if (geoCoordinatesData != null && !geoCoordinatesData.isEmpty() && !geoCoordinatesData.contains(null)) {
            usagePoint.setSpatialCoordinates(new SpatialCoordinatesFactory().fromStringValue(geoCoordinatesData.stream()
                    .reduce((s, t) -> s + ":" + t)
                    .get()));
        }
        usagePoint.setOutageRegion(data.getOutageRegion());
        usagePoint.setReadRoute(data.getReadRoute());
        usagePoint.setServicePriority(data.getServicePriority());
        usagePoint.setServiceDeliveryRemark(data.getServiceDeliveryRemark());
        usagePoint.update();
        return usagePoint;
    }

    private LocationBuilder.LocationMemberBuilder setLocationAttributes(LocationBuilder.LocationMemberBuilder builder, UsagePointImportRecord data, Map<String, Integer> ranking) {
        List<String> location = data.getLocation();
        builder.setCountryCode(location.get(ranking.get("countryCode")))
                .setCountryName(location.get(ranking.get("countryName")))
                .setAdministrativeArea(location.get(ranking.get("administrativeArea")))
                .setLocality(location.get(ranking.get("locality")))
                .setSubLocality(location.get(ranking.get("subLocality")))
                .setStreetType(location.get(ranking.get("streetType")))
                .setStreetName(location.get(ranking.get("streetName")))
                .setStreetNumber(location.get(ranking.get("streetNumber")))
                .setEstablishmentType(location.get(ranking.get("establishmentType")))
                .setEstablishmentName(location.get(ranking.get("establishmentName")))
                .setEstablishmentNumber(location.get(ranking.get("establishmentNumber")))
                .setAddressDetail(location.get(ranking.get("addressDetail")))
                .setZipCode(location.get(ranking.get("zipCode")))
                .isDaultLocation(true)
                .setLocale(data.getLocation().get(ranking.get("locale")) == null || data.getLocation()
                        .get(ranking.get("locale"))
                        .equals("") ? "en" : data.getLocation().get(ranking.get("locale")));
        return builder;
    }

    public void setMetrologyConfigurationForUsagePoint(UsagePointImportRecord data, UsagePoint usagePoint) {
        data.getMetrologyConfiguration().ifPresent(metrologyConfigurationName -> {
            UsagePointMetrologyConfiguration metrologyConfiguration = context.getMetrologyConfigurationService()
                    .findMetrologyConfiguration(metrologyConfigurationName)
                    .filter(mc -> mc instanceof UsagePointMetrologyConfiguration)
                    .map(UsagePointMetrologyConfiguration.class::cast)
                    .filter(UsagePointMetrologyConfiguration::isActive)
                    .orElseThrow(() -> new ProcessorException(MessageSeeds.BAD_METROLOGY_CONFIGURATION, data.getLineNumber()));
            if (!metrologyConfiguration.getServiceCategory().equals(usagePoint.getServiceCategory())) {
                throw new ProcessorException(MessageSeeds.SERVICE_CATEGORIES_DO_NOT_MATCH, data.getLineNumber());
            }
            if (!data.getMetrologyConfigurationApplyTime().isPresent()) {
                throw new ProcessorException(MessageSeeds.EMPTY_METROLOGY_CONFIGURATION_TIME, data.getLineNumber());
            }
            usagePoint.apply(metrologyConfiguration, data.getMetrologyConfigurationApplyTime().get());
        });
    }
}
