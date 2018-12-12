/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.propertyspec;

import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySelectionMode;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.ValueFactory;
import com.energyict.protocolimpl.properties.PropertySpecPossibleValuesImpl;

import java.util.Arrays;
import java.util.List;

public class MockPropertySpecBuilderImpl implements PropertySpecBuilder, PropertySpecBuilderWizard.NlsOptions, PropertySpecBuilderWizard.ThesaurusBased, PropertySpecBuilderWizard.HardCoded {

    private final ValueFactory valueFactory;

    public MockPropertySpecBuilderImpl(ValueFactory valueFactory) {
        this.valueFactory = valueFactory;
    }

    private MockPropertySpecImpl propertySpec;

    public MockPropertySpecImpl getPropertySpec() {
        if (propertySpec == null) {
            propertySpec = new MockPropertySpecImpl();
            propertySpec.setValueFactory(valueFactory);
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
        PropertySpecPossibleValuesImpl propertySpecPossibleValues = new PropertySpecPossibleValuesImpl();
        propertySpecPossibleValues.setAllValues(Arrays.asList(values));
        getPropertySpec().setPropertySpecPossibleValues(propertySpecPossibleValues);
        return this;
    }

    @Override
    public PropertySpecBuilder addValues(List values) {
        PropertySpecPossibleValuesImpl propertySpecPossibleValues = new PropertySpecPossibleValuesImpl();
        propertySpecPossibleValues.setAllValues(values);
        getPropertySpec().setPropertySpecPossibleValues(propertySpecPossibleValues);
        return this;
    }

    @Override
    public PropertySpec finish() {
        MockPropertySpecImpl result = new MockPropertySpecImpl();
        result.setRequired(getPropertySpec().isRequired());
        result.setDescription(getPropertySpec().getDescription());
        result.setDisplayName(getPropertySpec().getDisplayName());
        result.setName(getPropertySpec().getName());
        result.setValueFactory(getPropertySpec().getValueFactory());
        result.setPropertySpecPossibleValues(getPropertySpec().getPossibleValues());
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