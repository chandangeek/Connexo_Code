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

class StandardDataSelectorFactory implements DataSelectorFactory {
    static final String TRANSLATION_KEY = ReadingTypeDataSelectorImpl.class.getName();
    static final String DISPLAYNAME = "Device readings data selector";
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
        return new DelegatingDataSelector(logger);
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
        return thesaurus.getString(getNlsKey().getKey(), DISPLAYNAME);
    }

    @Override
    public boolean isDefault() {
        return true;
    }

    private NlsKey getNlsKey() {
        return SimpleNlsKey.key(DataExportService.COMPONENTNAME, Layer.DOMAIN, TRANSLATION_KEY);
    }

    private class DelegatingDataSelector implements DataSelector {

        private final Logger logger;

        private DelegatingDataSelector(Logger logger) {
            this.logger = logger;
        }


        @Override
        public Stream<ExportData> selectData(DataExportOccurrence dataExportOccurrence) {
            return dataExportOccurrence.getTask().getReadingTypeDataSelector()
                    .map(IReadingTypeDataSelector.class::cast)
                    .map(readingTypeDataSelector -> readingTypeDataSelector.asDataSelector(logger, thesaurus))
                    .orElseThrow(IllegalStateException::new).selectData(dataExportOccurrence);
        }
    }
}
