package com.elster.jupiter.metering.imports.impl.usagepoint;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.fileimport.csvimport.exceptions.ProcessorException;
import com.elster.jupiter.metering.LocationBuilder;
import com.elster.jupiter.metering.LocationTemplate;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.imports.impl.CustomPropertySetRecord;
import com.elster.jupiter.metering.imports.impl.MessageSeeds;
import com.elster.jupiter.metering.imports.impl.MeteringDataImporterContext;
import com.elster.jupiter.util.geo.SpatialCoordinatesFactory;

import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UsagePointImportHelper implements OutOfTheBoxCategoryForImport.ServiceProvider {

    private MeteringDataImporterContext context;
    private Clock clock;

    public UsagePointImportHelper(MeteringDataImporterContext context, Clock clock) {
        this.context = context;
        this.clock = clock;
    }

    @Override
    public CalendarService calendarService() {
        return this.context.getCalendarService();
    }

    public UsagePoint createUsagePointForInsight(UsagePointBuilder usagePointBuilder, UsagePointImportRecord data) {
        setLocation(usagePointBuilder, data);
        usagePointBuilder.withOutageRegion(data.getOutageRegion());
        usagePointBuilder.withReadRoute(data.getReadRoute());
        usagePointBuilder.withServicePriority(data.getServicePriority());
        usagePointBuilder.withServiceDeliveryRemark(data.getServiceDeliveryRemark());
        addCustomPropertySetsValues(usagePointBuilder, data);
        UsagePoint usagePoint = usagePointBuilder.create();
        this.addCalendars(data, usagePoint);
        this.applyMetrologyConfiguration(data, usagePoint);
        return usagePoint;
    }

    public UsagePoint createUsagePointForMultiSense(UsagePointBuilder usagePointBuilder, UsagePointImportRecord data) {
        usagePointBuilder.withIsSdp(false);
        usagePointBuilder.withIsVirtual(true);
        setLocation(usagePointBuilder, data);
        UsagePoint usagePoint = usagePointBuilder.create();
        usagePoint.addDetail(usagePoint.getServiceCategory().newUsagePointDetail(usagePoint, clock.instant()));
        usagePoint.update();
        this.addCalendars(data, usagePoint);
        this.applyMetrologyConfiguration(data, usagePoint);
        return usagePoint;
    }

    private void applyMetrologyConfiguration(UsagePointImportRecord data, UsagePoint usagePoint) {
        data.getMetrologyConfigurationName().ifPresent(metrologyConfigurationName -> {
            validateMetrologyConfiguration(metrologyConfigurationName, usagePoint, data);
            setMetrologyConfigurationForUsagePoint(data, usagePoint);
        });
    }

    private void addCalendars(UsagePointImportRecord data, UsagePoint usagePoint) {
        Stream
            .of(OutOfTheBoxCategoryForImport.values())
            .forEach(each -> each.addCalendar(data, usagePoint, this));
    }

    public UsagePoint updateUsagePointForMultiSense(UsagePoint usagePoint, UsagePointImportRecord data) {
        updateLocation(usagePoint, data);
        usagePoint.update();
        this.addCalendars(data, usagePoint);
        return usagePoint;
    }

    public UsagePoint updateUsagePointForInsight(UsagePoint usagePoint, UsagePointImportRecord data) {
        updateLocation(usagePoint, data);
        usagePoint.setOutageRegion(data.getOutageRegion());
        usagePoint.setReadRoute(data.getReadRoute());
        usagePoint.setServicePriority(data.getServicePriority());
        usagePoint.setServiceDeliveryRemark(data.getServiceDeliveryRemark());
        updateCustomPropertySetsValues(usagePoint, data);
        usagePoint.update();
        this.addCalendars(data, usagePoint);
        return usagePoint;
    }

    private LocationBuilder.LocationMemberBuilder setLocationAttributes(LocationBuilder.LocationMemberBuilder builder, UsagePointImportRecord data, Map<String, Integer> ranking) {
        List<String> location = data.getLocation();
        String locale;
        if (   data.getLocation().get(ranking.get("locale")) == null
            || "".equals(data.getLocation().get(ranking.get("locale")))) {
            locale = "en";
        } else {
            locale = data.getLocation().get(ranking.get("locale"));
        }
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
                .setLocale(locale);
        return builder;
    }


    private void setMetrologyConfigurationForUsagePoint(UsagePointImportRecord data, UsagePoint usagePoint) {
        data.getMetrologyConfigurationName().ifPresent(metrologyConfigurationName ->
            context.getMetrologyConfigurationService()
                    .findMetrologyConfiguration(metrologyConfigurationName)
                    .map(UsagePointMetrologyConfiguration.class::cast)
                    .ifPresent(configuration ->
                            usagePoint
                                    .apply(
                                        configuration,
                                        data.getMetrologyConfigurationApplyTime().get())));
    }

    private void addCustomPropertySetsValues(UsagePointBuilder usagePointBuilder, UsagePointImportRecord data) {
        data.getRegisteredCustomPropertySets().forEach((customPropertySetId, customPropertySetRecord) -> {
                    CustomPropertySetValues values = null;
                    Optional<RegisteredCustomPropertySet> customPropertySet = context.getCustomPropertySetService()
                            .findActiveCustomPropertySet(customPropertySetId);
                    if (customPropertySet.isPresent()) {
                        if ((customPropertySet.get().getCustomPropertySet()).isVersioned()) {
                            Range<Instant> rangeToCreate = getRangeToCreate(customPropertySetRecord);
                            if (!rangeToCreate.hasLowerBound()) {
                                rangeToCreate = Range.atLeast(data.getInstallationTime().orElse(clock.instant()))
                                        .intersection(rangeToCreate);
                            }
                            values = CustomPropertySetValues.emptyDuring(rangeToCreate);
                            copyValues(customPropertySetRecord.getCustomPropertySetValues(), values);
                        } else {
                            values = customPropertySetRecord.getCustomPropertySetValues();
                        }
                        usagePointBuilder.addCustomPropertySetValues(customPropertySet.get(), values);
                    }
                }
        );
    }

    private void updateCustomPropertySetsValues(UsagePoint usagePoint, UsagePointImportRecord data) {
        data.getRegisteredCustomPropertySets().forEach((customPropertySetId, customPropertySetRecord) -> {
                    CustomPropertySetValues values = null;
                    Optional<RegisteredCustomPropertySet> customPropertySet = context.getCustomPropertySetService()
                            .findActiveCustomPropertySet(customPropertySetId);
                    if (customPropertySet.isPresent()) {
                        if ((customPropertySet.get().getCustomPropertySet()).isVersioned()) {
                            Range<Instant> rangeToCreate = getRangeToCreate(customPropertySetRecord);
                            if (!rangeToCreate.hasLowerBound()) {
                                rangeToCreate = Range.atLeast(data.getInstallationTime().orElse(clock.instant()))
                                        .intersection(rangeToCreate);
                            }
                            values = CustomPropertySetValues.emptyDuring(rangeToCreate);
                            copyValues(customPropertySetRecord.getCustomPropertySetValues(), values);
                        } else {
                            values = customPropertySetRecord.getCustomPropertySetValues();
                        }

                        final CustomPropertySetValues valuesFinal = values;
                        values.propertyNames().forEach(name -> {
                            usagePoint.forCustomProperties().getAllPropertySets()
                                    .stream()
                                    .filter(s -> s.getValues().propertyNames().stream().anyMatch(p -> p.equals(name)))
                                    .forEach(usagePointPropertySet -> usagePointPropertySet.setValues(valuesFinal));
                        });
                        System.out.println("test");
                    }
                }
        );
    }

    private void copyValues(CustomPropertySetValues source, CustomPropertySetValues target) {
        source.propertyNames().forEach(propertyName -> {
            Object propertyValue = source.getProperty(propertyName);
            target.setProperty(propertyName, propertyValue);
        });
    }

    private Range<Instant> getRangeToCreate(CustomPropertySetRecord customPropertySetRecord) {
        if ((!customPropertySetRecord.getStartTime().isPresent() || customPropertySetRecord.getStartTime()
                .get()
                .equals(Instant.EPOCH))
                && (!customPropertySetRecord.getEndTime().isPresent() || customPropertySetRecord.getEndTime()
                .get()
                .equals(Instant.EPOCH))) {
            return Range.all();
        } else if (!customPropertySetRecord.getStartTime().isPresent() || customPropertySetRecord.getStartTime()
                .get()
                .equals(Instant.EPOCH)) {
            return Range.lessThan(customPropertySetRecord.getEndTime().get());
        } else if (!customPropertySetRecord.getEndTime().isPresent() || customPropertySetRecord.getEndTime()
                .get()
                .equals(Instant.EPOCH)) {
            return Range.atLeast(customPropertySetRecord.getStartTime().get());
        } else {
            return Range.closedOpen(
                    customPropertySetRecord.getStartTime().get(),
                    customPropertySetRecord.getEndTime().get());
        }
    }

    public void validateMetrologyConfiguration(String metrologyConfigurationName, UsagePoint usagePoint, UsagePointImportRecord data) {
        MetrologyConfiguration metrologyConfiguration = context.getMetrologyConfigurationService()
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
    }

    void setLocation(UsagePointBuilder usagePointBuilder, UsagePointImportRecord data) {
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
    }

    void updateLocation(UsagePoint usagePoint, UsagePointImportRecord data) {
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
    }
}
