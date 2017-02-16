/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ReadingTypeFilter;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.rest.util.JsonQueryFilter;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReadingTypeFilterFactory {

    private final MetrologyConfigurationService metrologyConfigurationService;

    @Inject
    public ReadingTypeFilterFactory(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    public ReadingTypeFilter from(JsonQueryFilter jsonQueryFilter) {
        ReadingTypeFilter filter = new ReadingTypeFilter();

        if (jsonQueryFilter.hasProperty("mRID")) {
            filter.addMRIDCondition(jsonQueryFilter.getString("mRID"));
        }

        if (jsonQueryFilter.hasProperty("selectedreadingtypes")) {
            filter.addSelectedReadingTypesCondition(jsonQueryFilter.getStringList("selectedreadingtypes"));
        }

        if (jsonQueryFilter.hasProperty("fullAliasName")) {
            filter.addFullAliasNameCondition(jsonQueryFilter.getString("fullAliasName"));
        }

        if (jsonQueryFilter.hasProperty("equidistant")) {
            filter.addEquidistantCondition(jsonQueryFilter.getBoolean("equidistant"));
        }

        if (jsonQueryFilter.hasProperty("active")) {
            filter.addActiveCondition(jsonQueryFilter.getBoolean("active"));
        }

        getMetrologyConfigurationFromFilter(jsonQueryFilter).ifPresent(metrologyConfiguration -> {
            List<String> readingTypes = getReadingTypesOfMetrologyConfiguration(metrologyConfiguration);
            filter.addMRIDsCondition(readingTypes);
        });

        if (jsonQueryFilter.hasProperty("metrologyPurpose")) {
            Long metrologyPurposeId = jsonQueryFilter.getLong("metrologyPurpose");
            metrologyConfigurationService.findMetrologyPurpose(metrologyPurposeId).ifPresent(metrologyPurpose -> {
                List<String> readingTypes = getReadingTypesOfMetrologyPurpose(metrologyPurpose, jsonQueryFilter);
                filter.addMRIDsCondition(readingTypes);
            });
        }

        Arrays.stream(ReadingTypeFilter.ReadingTypeFields.values())
                .filter(e -> jsonQueryFilter.hasProperty(e.getName()) && !jsonQueryFilter.getPropertyList(e.getName()).isEmpty())
                .forEach(e -> filter.addCodedValueCondition(e.getName(), jsonQueryFilter.getPropertyList(e.getName())));

        Arrays.stream(ReadingTypeFilter.ReadingTypeFields.values())
                .filter(e -> jsonQueryFilter.hasProperty(e.getName()) && jsonQueryFilter.getPropertyList(e.getName()).isEmpty())
                .forEach(e -> filter.addCodedValueCondition(e.getName(), jsonQueryFilter.getComplexProperty(e.getName())));

        return filter;
    }

    private List<String> getReadingTypesOfMetrologyConfiguration(MetrologyConfiguration metrologyConfiguration) {
        return metrologyConfiguration.getDeliverables().stream()
                .map(ReadingTypeDeliverable::getReadingType)
                .map(ReadingType::getMRID)
                .collect(Collectors.toList());
    }

    private List<String> getReadingTypesOfMetrologyPurpose(MetrologyPurpose metrologyPurpose, JsonQueryFilter jsonQueryFilter) {
        return getMetrologyConfigurationFromFilter(jsonQueryFilter)
                .map(Stream::of)
                .orElseGet(() -> metrologyConfigurationService.findAllMetrologyConfigurations().stream())
                .flatMap(mc -> mc.getContracts().stream())
                .filter(metrologyContract -> metrologyContract.getMetrologyPurpose().equals(metrologyPurpose))
                .flatMap(metrologyContract -> metrologyContract.getDeliverables().stream())
                .map(ReadingTypeDeliverable::getReadingType)
                .distinct()
                .map(ReadingType::getMRID)
                .collect(Collectors.toList());
    }

    private Optional<MetrologyConfiguration> getMetrologyConfigurationFromFilter(JsonQueryFilter jsonQueryFilter) {
        if (jsonQueryFilter.hasProperty("metrologyConfiguration")) {
            Long metrologyConfigurationId = jsonQueryFilter.getLong("metrologyConfiguration");
            return metrologyConfigurationService.findMetrologyConfiguration(metrologyConfigurationId);
        }
        return Optional.empty();
    }
}
