/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.export.DataFormatter;
import com.elster.jupiter.export.DataFormatterFactory;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;

import java.util.Collections;
import java.util.List;
import java.util.Map;

class NullDataFormatterFactory implements DataFormatterFactory {
    private static final String KEY = "No operation data formatter";
    private static final String DEFAULT_DISPLAY_NAME = "Not applicable (for ''Web service'' destination)";
    private static final TranslationKey NAME = new SimpleTranslationKey(KEY, DEFAULT_DISPLAY_NAME);
    private final Thesaurus thesaurus;

    NullDataFormatterFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    static TranslationKey getNameTranslationKey() {
        return NAME;
    }

    @Override
    public DataFormatter createDataFormatter(Map<String, Object> properties) {
        return new NullDataFormatter();
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
        return NAME.getKey();
    }

    @Override
    public String getDisplayName() {
        return thesaurus.getFormat(NAME).format();
    }
}
