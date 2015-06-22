package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataSelector;
import com.elster.jupiter.export.DataSelectorFactory;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.transaction.TransactionService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class StandardDataSelectorFactory implements DataSelectorFactory {

    private final TransactionService transactionService;
    private final Thesaurus thesaurus;
    private final MeteringService meteringService;

    public StandardDataSelectorFactory(TransactionService transactionService, MeteringService meteringService, Thesaurus thesaurus) {
        this.transactionService = transactionService;
        this.meteringService = meteringService;
        this.thesaurus = thesaurus;
    }

    @Override
    public DataSelector createDataSelector(Map<String, Object> properties, Logger logger) {
        return DelegatingDataSelector.INSTANCE;
    }

    @Override
    public void validateProperties(List<DataExportProperty> properties) {
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return DataExportService.STANDARD_DATA_SELECTOR;
    }

    @Override
    public String getDisplayName() {
        return thesaurus.getString(getNlsKey().getKey(), getName());
    }

    @Override
    public boolean isDefault() {
        return true;
    }

    private NlsKey getNlsKey() {
        return SimpleNlsKey.key(DataExportService.COMPONENTNAME, Layer.DOMAIN, ReadingTypeDataSelectorImpl.class.getName());
    }

    private enum DelegatingDataSelector implements DataSelector {

        INSTANCE;

        @Override
        public Stream<ExportData> selectData(DataExportOccurrence dataExportOccurrence) {
            return dataExportOccurrence.getTask().getReadingTypeDataSelector().orElseThrow(IllegalStateException::new).selectData(dataExportOccurrence);
        }
    }
}
