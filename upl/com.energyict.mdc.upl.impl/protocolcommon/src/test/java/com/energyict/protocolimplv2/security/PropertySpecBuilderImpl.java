/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySelectionMode;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;

import java.util.List;

class PropertySpecBuilderImpl implements PropertySpecBuilder, PropertySpecBuilderWizard.NlsOptions, PropertySpecBuilderWizard.ThesaurusBased, PropertySpecBuilderWizard.HardCoded {

    private PropertySpecImpl propertySpec;

    public PropertySpecImpl getPropertySpec() {
        if (propertySpec == null) {
            propertySpec = new PropertySpecImpl();
        }
        return propertySpec;
    }

    @Override
    public PropertySpecBuilder setDefaultValue(Object defaultValue) {
        return this;
    }

    @Override
    public PropertySpecBuilder markExhaustive() {
        return this;
    }

    @Override
    public PropertySpecBuilder markEditable() {
        return this;
    }

    @Override
    public PropertySpecBuilder markExhaustive(PropertySelectionMode selectionMode) {
        return this;
    }

    @Override
    public PropertySpecBuilder markMultiValued(String separator) {
        return this;
    }

    @Override
    public PropertySpecBuilder markRequired() {
        getPropertySpec().setRequired(true);
        return this;
    }

    @Override
    public PropertySpecBuilder addValues(Object[] values) {
        return this;
    }

    @Override
    public PropertySpecBuilder addValues(List values) {
        return this;
    }

    @Override
    public PropertySpec finish() {
        PropertySpecImpl result = new PropertySpecImpl();
        result.setRequired(getPropertySpec().isRequired());
        result.setDescription(getPropertySpec().getDescription());
        result.setDisplayName(getPropertySpec().getDisplayName());
        result.setName(getPropertySpec().getName());
        result.setValueFactory(getPropertySpec().getValueFactory());
        propertySpec = null;
        return result;
    }

    @Override
    public PropertySpecBuilderWizard.ThesaurusBased named(TranslationKey nameTranslationKey) {
        getPropertySpec().setName(nameTranslationKey.getDefaultFormat());
        getPropertySpec().setDisplayName(nameTranslationKey.getDefaultFormat());
        return this;
    }

    @Override
    public PropertySpecBuilderWizard.ThesaurusBased named(String name, TranslationKey displayNameTranslationKey) {
        getPropertySpec().setName(name);
        getPropertySpec().setDisplayName(displayNameTranslationKey.getDefaultFormat());
        return this;
    }

    @Override
    public PropertySpecBuilderWizard.HardCoded named(String name, String displayName) {
        getPropertySpec().setName(name);
        getPropertySpec().setDisplayName(displayName);
        return this;
    }

    @Override
    public PropertySpecBuilder describedAs(TranslationKey descriptionTranslationKey) {
        getPropertySpec().setDescription(descriptionTranslationKey.getDefaultFormat());
        return this;
    }

    @Override
    public PropertySpecBuilder describedAs(String description) {
        getPropertySpec().setDescription(description);
        return this;
    }
}