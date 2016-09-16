package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.*;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.rest.RelativePeriodInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StandardDataSelectorInfo {

    public long id;
    public MeterGroupInfo deviceGroup;
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

    public StandardDataSelectorInfo(StandardDataSelector selector, Thesaurus thesaurus) {
        populateFrom(selector, thesaurus);
        ReadingTypeInfoFactory readingTypeInfoFactory = new ReadingTypeInfoFactory(thesaurus);
        readingTypes = selector.getReadingTypes()
                .stream()
                .map(readingTypeInfoFactory::from)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public StandardDataSelectorInfo(EventDataSelector selector, Thesaurus thesaurus) {
        populateFrom(selector, thesaurus);
        eventTypeCodes = selector.getEventTypeFilters()
                .stream()
                .map(EndDeviceEventTypeFilter::getCode)
                .map(EventTypeInfo::of)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void populateFrom(EventDataSelector selector, Thesaurus thesaurus) {
        id = selector.getId();
        deviceGroup = new MeterGroupInfo(selector.getEndDeviceGroup());
        exportPeriod = new RelativePeriodInfo(selector.getExportPeriod(), thesaurus);
        EventDataExportStrategy strategy = selector.getEventStrategy();
        exportContinuousData = strategy.isExportContinuousData();
    }

    void populateFrom(StandardDataSelector selector, Thesaurus thesaurus) {
        id = selector.getId();

        deviceGroup = new MeterGroupInfo(selector.getEndDeviceGroup());
        exportPeriod = new RelativePeriodInfo(selector.getExportPeriod(), thesaurus);
        DataExportStrategy strategy = selector.getStrategy();
        populateFrom(strategy, thesaurus);
    }

    private void populateFrom(DataExportStrategy strategy, Thesaurus thesaurus) {
        exportContinuousData = strategy.isExportContinuousData();
        exportComplete = strategy.isExportCompleteData();
        exportUpdate = strategy.isExportUpdate();
        strategy.getUpdatePeriod()
                .ifPresent(relativePeriod -> updatePeriod = new RelativePeriodInfo(relativePeriod, thesaurus));
        strategy.getUpdateWindow()
                .ifPresent(relativePeriod -> updateWindow = new RelativePeriodInfo(relativePeriod, thesaurus));
        validatedDataOption = strategy.getValidatedDataOption();
    }


}
