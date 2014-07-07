package com.energyict.mdc.dynamic.impl;

import java.util.ArrayList;
import java.util.List;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.energyict.mdc.dynamic.PropertySpecFactory;

/**
 * Provides code reuse opportunities for components that will implement the {@link PropertySpecFactory} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-01 (12:14)
 */
public abstract class PropertySpecFactoryImpl implements PropertySpecFactory {

    protected <T> PropertySpec<T> simpleOptionalPropertySpec(String name, ValueFactory<T> valueFactory) {
        return PropertySpecBuilderImpl.
                forClass(valueFactory).
                name(name).
                finish();
    }

    protected <T> PropertySpec<T> simpleRequiredPropertySpec(String name, ValueFactory<T> valueFactory) {
        return PropertySpecBuilderImpl.
                forClass(valueFactory).
                name(name).
                markRequired().
                finish();
    }

    @Override
    public List<PropertySpec> toPropertySpecs(List<String> keys) {
        List<PropertySpec> result = new ArrayList<>();
        for (String key : keys) {
            result.add(this.stringPropertySpec(key));
        }
        return result;
    }

}