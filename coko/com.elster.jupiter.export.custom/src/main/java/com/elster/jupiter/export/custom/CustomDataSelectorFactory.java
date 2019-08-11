/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.custom;

import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataSelector;
import com.elster.jupiter.export.DataSelectorFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.elster.jupiter.export.custom.CustomDataSelectorFactory",
        property = {DataExportService.DATA_TYPE_PROPERTY + "=" + DataExportService.STANDARD_READING_DATA_TYPE},
        service = DataSelectorFactory.class,
        immediate = true)
public class CustomDataSelectorFactory implements DataSelectorFactory {
    static final String TRANSLATION_KEY = DataExportService.CUSTOM_READINGTYPE_DATA_SELECTOR;
    public static final String DISPLAY_NAME = "Device readings data selector [CST]";

    private volatile Thesaurus thesaurus;

    static final String NAME = DataExportService.CUSTOM_READINGTYPE_DATA_SELECTOR;


    public CustomDataSelectorFactory() {
    }

    @Inject
    public CustomDataSelectorFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public DataSelector createDataSelector(Map<String, Object> properties, Logger logger) {
        return new CustomDelegatingDataSelector(logger);
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
        return DataExportService.CUSTOM_READINGTYPE_DATA_SELECTOR;
    }

    @Override
    public String getDisplayName() {
        return thesaurus.getString(getNlsKey().getKey(), DISPLAY_NAME);
    }

    @Override
    public boolean isDefault() {
        return false;
    }

    private NlsKey getNlsKey() {
        return SimpleNlsKey.key(DataExportService.COMPONENTNAME, Layer.DOMAIN, TRANSLATION_KEY);
    }

    @Override
    public List<String> targetApplications() {
        return Collections.singletonList("MDC");
    }

    @Reference
    public void setThesaurus(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(NAME, Layer.DOMAIN);
    }

}
