package com.elster.jupiter.issue.impl.actions.webelements.factories;

import com.elster.jupiter.issue.impl.actions.webelements.PropertyAbstractFactory;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;

public abstract class DropdownFactory<T> extends PropertyAbstractFactory {

    /**
     * Array that contains possible values for dropdown
     */
    private T[] possibleValues;

    protected DropdownFactory(final PropertySpecService propertySpecService, final Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    @Override
    public PropertySpec getElement(final String name, final TranslationKey displayName, final TranslationKey description) {
        possibleValues = getPossibleValues();
        return this.propertySpecService
                .specForValuesOf(getValueFactory())
                .named(name, displayName)
                .describedAs(description)
                .fromThesaurus(thesaurus)
                .markRequired()
                .setDefaultValue(getDefaultValue())
                .addValues(getPossibleValues())
                .markExhaustive()
                .finish();
    }

    protected T getDefaultValue() {
        return possibleValues.length == 1 ? possibleValues[0] : null;
    }

    abstract ValueFactory<T> getValueFactory();

    abstract T[] getPossibleValues();
}
