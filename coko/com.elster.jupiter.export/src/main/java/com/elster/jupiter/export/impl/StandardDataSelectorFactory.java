package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataSelector;
import com.elster.jupiter.export.DataSelectorFactory;
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

public class StandardDataSelectorFactory implements DataSelectorFactory {

    private final TransactionService transactionService;
    private final Thesaurus thesaurus;

    public StandardDataSelectorFactory(TransactionService transactionService, Thesaurus thesaurus) {
        this.transactionService = transactionService;
        this.thesaurus = thesaurus;
    }

    @Override
    public DataSelector createDataSelector(Map<String, Object> properties, Logger logger) {
        return new ReadingTypeDataSelector(transactionService, logger);
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

    private NlsKey getNlsKey() {
        return SimpleNlsKey.key(DataExportService.COMPONENTNAME, Layer.DOMAIN, ReadingTypeDataSelector.class.getName());
    }

}
