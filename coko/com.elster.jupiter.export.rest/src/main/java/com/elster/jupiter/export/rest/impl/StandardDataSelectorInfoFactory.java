/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportStrategy;
import com.elster.jupiter.export.EndDeviceEventTypeFilter;
import com.elster.jupiter.export.EventSelectorConfig;
import com.elster.jupiter.export.MeterReadingSelectorConfig;
import com.elster.jupiter.export.UsagePointReadingSelectorConfig;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.time.rest.RelativePeriodInfo;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class StandardDataSelectorInfoFactory {

    private final ReadingTypeInfoFactory readingTypeInfoFactory;

    @Inject
    public StandardDataSelectorInfoFactory(ReadingTypeInfoFactory readingTypeInfoFactory) {
        this.readingTypeInfoFactory = readingTypeInfoFactory;
    }

    public StandardDataSelectorInfo asInfo(MeterReadingSelectorConfig selector) {
        StandardDataSelectorInfo info = new StandardDataSelectorInfo();
        info.id = selector.getId();
        info.deviceGroup = new IdWithNameInfo(selector.getEndDeviceGroup().getId(), selector.getEndDeviceGroup().getName());
        info.exportPeriod = RelativePeriodInfo.withCategories(selector.getExportPeriod());
        info.readingTypes = selector.getReadingTypes()
                .stream()
                .map(readingTypeInfoFactory::from)
                .collect(Collectors.toCollection(ArrayList::new));
        populateFromExportStrategy(selector.getStrategy(), info);
        return info;
    }

    public StandardDataSelectorInfo asInfo(UsagePointReadingSelectorConfig selector) {
        StandardDataSelectorInfo info = new StandardDataSelectorInfo();
        info.id = selector.getId();
        info.usagePointGroup = new IdWithNameInfo(selector.getUsagePointGroup().getId(), selector.getUsagePointGroup().getName());
        info.exportPeriod = RelativePeriodInfo.withCategories(selector.getExportPeriod());
        info.readingTypes = selector.getReadingTypes()
                .stream()
                .map(readingTypeInfoFactory::from)
                .collect(Collectors.toCollection(ArrayList::new));
        populateFromExportStrategy(selector.getStrategy(), info);
        return info;
    }

    private void populateFromExportStrategy(DataExportStrategy strategy, StandardDataSelectorInfo info) {
        info.exportContinuousData = strategy.isExportContinuousData();
        info.exportComplete = strategy.isExportCompleteData();
        info.exportUpdate = strategy.isExportUpdate();
        strategy.getUpdatePeriod()
                .ifPresent(relativePeriod -> info.updatePeriod = RelativePeriodInfo.withCategories(relativePeriod));
        strategy.getUpdateWindow()
                .ifPresent(relativePeriod -> info.updateWindow = RelativePeriodInfo.withCategories(relativePeriod));
        info.validatedDataOption = strategy.getValidatedDataOption();
    }

    public StandardDataSelectorInfo asInfo(EventSelectorConfig selector) {
        StandardDataSelectorInfo info = new StandardDataSelectorInfo();
        info.id = selector.getId();
        info.deviceGroup = new IdWithNameInfo(selector.getEndDeviceGroup().getId(), selector.getEndDeviceGroup().getName());
        info.exportPeriod = RelativePeriodInfo.withCategories(selector.getExportPeriod());
        info.exportContinuousData = selector.getStrategy().isExportContinuousData();
        info.eventTypeCodes = selector.getEventTypeFilters()
                .stream()
                .map(EndDeviceEventTypeFilter::getCode)
                .map(EventTypeInfo::of)
                .collect(Collectors.toList());
        return info;
    }
}
