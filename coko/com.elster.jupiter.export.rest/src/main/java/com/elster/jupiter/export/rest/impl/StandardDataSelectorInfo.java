package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.ReadingTypeDataSelector;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.rest.RelativePeriodInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StandardDataSelectorInfo {

    public long id;
    public MeterGroupInfo deviceGroup;
    public RelativePeriodInfo exportPeriod;
    public boolean exportContinuousData;
    public boolean exportUpdate;
    public RelativePeriodInfo updatePeriod;
    public ValidatedDataOption validatedDataOption;
    public List<ReadingTypeInfo> readingTypes = new ArrayList<>();

    public StandardDataSelectorInfo() {
    }

    public StandardDataSelectorInfo(ReadingTypeDataSelector selector, Thesaurus thesaurus) {
        populate(selector, thesaurus);

        for (ReadingType readingType : selector.getReadingTypes()) {
            readingTypes.add(new ReadingTypeInfo(readingType));
        }
    }

    void populate(ReadingTypeDataSelector selector, Thesaurus thesaurus) {
        id = selector.getId();

        deviceGroup = new MeterGroupInfo(selector.getEndDeviceGroup());
        exportPeriod = new RelativePeriodInfo(selector.getExportPeriod(), thesaurus);
        exportContinuousData = selector.getStrategy().isExportContinuousData();
        exportUpdate = selector.getStrategy().isExportUpdate();
        Optional<RelativePeriod> dataExportTaskUpdatePeriod = selector.getUpdatePeriod();
        if (dataExportTaskUpdatePeriod.isPresent()) {
            updatePeriod = new RelativePeriodInfo(dataExportTaskUpdatePeriod.get(), thesaurus);
        }
        validatedDataOption = selector.getStrategy().getValidatedDataOption();
    }


}
