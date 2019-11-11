/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.custom.export;

import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataSelector;
import com.elster.jupiter.export.DataSelectorFactory;
import com.elster.jupiter.export.SelectorType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.sap.soap.custom.TranslationInstaller;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Component(name = "CustomDataSelectorFactory",
        property = {DataExportService.DATA_TYPE_PROPERTY + "=" + DataExportService.STANDARD_READING_DATA_TYPE},
        service = DataSelectorFactory.class,
        immediate = true)
public class CustomDataSelectorFactory implements DataSelectorFactory {
    static final String CUSTOM_READING_DATA_SELECTOR_KEY = "Custom Data Selector";
    static final String CUSTOM_READING_DATA_SELECTOR_NAME = "Device readings data selector [CST]";

    private volatile Thesaurus thesaurus;

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
        return CUSTOM_READING_DATA_SELECTOR_KEY;
    }

    @Override
    public String getDisplayName() {
        return thesaurus.getFormat(TranslationKeys.CUSTOM_DATA_SELECTOR_FACTORY).format();
    }

    @Override
    public boolean isDefault() {
        return false;
    }

    @Override
    public SelectorType getSelectorType() {
        return SelectorType.DEFAULT_READINGS;
    }

    @Override
    public List<String> targetApplications() {
        return Collections.singletonList("MDC");
    }

    @Reference
    public void setThesaurus(TranslationInstaller translationInstaller) {
        this.thesaurus = translationInstaller.getThesaurus();
    }
}
