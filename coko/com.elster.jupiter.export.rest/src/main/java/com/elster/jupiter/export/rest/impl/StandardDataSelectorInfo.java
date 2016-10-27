package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportStrategy;
import com.elster.jupiter.export.EndDeviceEventTypeFilter;
import com.elster.jupiter.export.EventSelectorConfig;
import com.elster.jupiter.export.MeterReadingSelectorConfig;
import com.elster.jupiter.export.UsagePointReadingSelectorConfig;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.time.rest.RelativePeriodInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StandardDataSelectorInfo {

    public long id;
    public IdWithNameInfo deviceGroup;
    public IdWithNameInfo usagePointGroup;
    public RelativePeriodInfo exportPeriod;
    public boolean exportContinuousData;
    public boolean exportUpdate; // only used from FE to BE
    public boolean exportComplete;
    public boolean exportAdjacentData;
    public RelativePeriodInfo updatePeriod;
    public RelativePeriodInfo updateWindow;
    public ValidatedDataOption validatedDataOption;
    public List<ReadingTypeInfo> readingTypes = new ArrayList<>();
    public List<EventTypeInfo> eventTypeCodes = new ArrayList<>();

    public StandardDataSelectorInfo() {
    }

    public StandardDataSelectorInfo(MeterReadingSelectorConfig selector, Thesaurus thesaurus) {
        populateFrom(selector);
        ReadingTypeInfoFactory readingTypeInfoFactory = new ReadingTypeInfoFactory(thesaurus);
        readingTypes = selector.getReadingTypes()
                .stream()
                .map(readingTypeInfoFactory::from)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public StandardDataSelectorInfo(UsagePointReadingSelectorConfig selector, Thesaurus thesaurus) {
        populateFrom(selector);
        ReadingTypeInfoFactory readingTypeInfoFactory = new ReadingTypeInfoFactory(thesaurus);
        readingTypes = selector.getReadingTypes()
                .stream()
                .map(readingTypeInfoFactory::from)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public StandardDataSelectorInfo(EventSelectorConfig selector) {
        populateFrom(selector);
        eventTypeCodes = selector.getEventTypeFilters()
                .stream()
                .map(EndDeviceEventTypeFilter::getCode)
                .map(EventTypeInfo::of)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void populateFrom(EventSelectorConfig selector) {
        id = selector.getId();
        deviceGroup = new IdWithNameInfo(selector.getEndDeviceGroup().getId(), selector.getEndDeviceGroup().getName());
        exportPeriod = RelativePeriodInfo.withCategories(selector.getExportPeriod());
        exportContinuousData = selector.getStrategy().isExportContinuousData();
    }

    void populateFrom(MeterReadingSelectorConfig selector) {
        id = selector.getId();
        exportPeriod = RelativePeriodInfo.withCategories(selector.getExportPeriod());
        populateFrom(selector.getStrategy());
        deviceGroup = new IdWithNameInfo(selector.getEndDeviceGroup().getId(), selector.getEndDeviceGroup().getName());
    }

    void populateFrom(UsagePointReadingSelectorConfig selector) {
        id = selector.getId();
        exportPeriod = RelativePeriodInfo.withCategories(selector.getExportPeriod());
        populateFrom(selector.getStrategy());
        usagePointGroup = new IdWithNameInfo(selector.getUsagePointGroup().getId(), selector.getUsagePointGroup().getName());
    }

    private void populateFrom(DataExportStrategy strategy) {
        exportContinuousData = strategy.isExportContinuousData();
        exportComplete = strategy.isExportCompleteData();
        exportUpdate = strategy.isExportUpdate();
        strategy.getUpdatePeriod()
                .ifPresent(relativePeriod -> updatePeriod = RelativePeriodInfo.withCategories(relativePeriod));
        strategy.getUpdateWindow()
                .ifPresent(relativePeriod -> updateWindow = RelativePeriodInfo.withCategories(relativePeriod));
        validatedDataOption = strategy.getValidatedDataOption();
    }
}