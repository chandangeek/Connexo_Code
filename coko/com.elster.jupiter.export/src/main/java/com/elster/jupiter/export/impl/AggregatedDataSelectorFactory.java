package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataSelector;
import com.elster.jupiter.export.DataSelectorFactory;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class AggregatedDataSelectorFactory implements DataSelectorFactory {

    static final String TRANSLATION_KEY = DataExportService.STANDARD_AGGREGATED_DATA_SELECTOR;
    static final String DISPLAY_NAME = "Usage point readings data selector";

    private final Thesaurus thesaurus;

    public AggregatedDataSelectorFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public String getName() {
        return DataExportService.STANDARD_AGGREGATED_DATA_SELECTOR;
    }

    @Override
    public String getDisplayName() {
        return thesaurus.getString(getNlsKey().getKey(), DISPLAY_NAME);
    }

    private NlsKey getNlsKey() {
        return SimpleNlsKey.key(DataExportService.COMPONENTNAME, Layer.DOMAIN, TRANSLATION_KEY);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public void validateProperties(List<DataExportProperty> properties) {
        // no dynamic properties expected
    }

    @Override
    public boolean isDefault() {
        return true;
    }

    @Override
    public DataSelector createDataSelector(Map<String, Object> properties, Logger logger) {
        return new DelegatingDataSelector(logger);
    }

    private class DelegatingDataSelector implements DataSelector {

        private final Logger logger;

        private DelegatingDataSelector(Logger logger) {
            this.logger = logger;
        }

        @Override
        public Stream<ExportData> selectData(DataExportOccurrence dataExportOccurrence) {
            return dataExportOccurrence.getTask().getAggregatedDataSelector()
                    .map(IStandardDataSelector.class::cast)
                    .map(readingTypeDataSelector -> readingTypeDataSelector.asAggregatedDataSelector(logger, thesaurus))
                    .orElseThrow(IllegalStateException::new).selectData(dataExportOccurrence);
        }
    }
}
