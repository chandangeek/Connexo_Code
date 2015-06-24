package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportStrategy;
import com.elster.jupiter.export.ReadingTypeDataSelector;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.rest.RelativePeriodInfo;

import java.util.ArrayList;
import java.util.List;

public class StandardDataSelectorInfo {

    public long id;
    public MeterGroupInfo deviceGroup;
    public RelativePeriodInfo exportPeriod;
    public boolean exportContinuousData;
    public boolean exportUpdate;
    public boolean exportComplete;
    public RelativePeriodInfo updatePeriod;
    public RelativePeriodInfo updateWindow;
    public ValidatedDataOption validatedDataOption;
    public List<ReadingTypeInfo> readingTypes = new ArrayList<>();

    public StandardDataSelectorInfo() {
    }

    public StandardDataSelectorInfo(ReadingTypeDataSelector selector, Thesaurus thesaurus) {
        populateFrom(selector, thesaurus);

        for (ReadingType readingType : selector.getReadingTypes()) {
            readingTypes.add(new ReadingTypeInfo(readingType));
        }
    }

    void populateFrom(ReadingTypeDataSelector selector, Thesaurus thesaurus) {
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
