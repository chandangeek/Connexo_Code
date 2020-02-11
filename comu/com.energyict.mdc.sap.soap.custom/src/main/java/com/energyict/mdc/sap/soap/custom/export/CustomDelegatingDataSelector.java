/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.custom.export;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataSelector;
import com.elster.jupiter.export.DataSelectorConfig;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.MeterReadingSelectorConfig;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;

import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class CustomDelegatingDataSelector implements DataSelector {

    private final SAPCustomPropertySets sapCustomPropertySets;
    private final Logger logger;

    CustomDelegatingDataSelector(SAPCustomPropertySets sapCustomPropertySets, Logger logger) {
        this.sapCustomPropertySets = sapCustomPropertySets;
        this.logger = logger;
    }

    @Override
    public Stream<ExportData> selectData(DataExportOccurrence dataExportOccurrence) {
        Optional<DataSelectorConfig> standardDataSelectorConfig = dataExportOccurrence.getRetryTime().isPresent() ?
                dataExportOccurrence.getTask().getStandardDataSelectorConfig(dataExportOccurrence.getRetryTime().get()) :
                dataExportOccurrence.getTask().getStandardDataSelectorConfig();

        return CustomMeterReadingSelector.from(standardDataSelectorConfig.get().getDataModel(), (MeterReadingSelectorConfig) standardDataSelectorConfig.get(), sapCustomPropertySets, logger).selectData(dataExportOccurrence);
    }
}
