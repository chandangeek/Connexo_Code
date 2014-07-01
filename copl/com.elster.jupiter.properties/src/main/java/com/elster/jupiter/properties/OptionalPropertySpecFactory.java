package com.elster.jupiter.properties;

import java.math.BigDecimal;

public class OptionalPropertySpecFactory extends PropertySpecFactoryImpl {

    public static PropertySpecFactory newInstance() {
        return new OptionalPropertySpecFactory();
    }

    @Override
    public PropertySpec<String> stringPropertySpec (String name) {
        return simpleOptionalPropertySpec(name, new StringFactory());
    }

    @Override
    public PropertySpec<String> stringPropertySpec (String name, String defaultValue) {
        return PropertySpecBuilderImpl.
                forClass(new StringFactory()).
                name(name).
                setDefaultValue(defaultValue).
                finish();
    }

    @Override
    public PropertySpec<String> stringPropertySpecWithValues (String name, String... values) {
        return PropertySpecBuilderImpl.
                forClass(new StringFactory()).
                name(name).addValues(values).
                markExhaustive().
                finish();
    }

    @Override
    public PropertySpec<BigDecimal> bigDecimalPropertySpec (String name) {
        return simpleOptionalPropertySpec(name, new BigDecimalFactory());
    }

    @Override
    public PropertySpec<BigDecimal> bigDecimalPropertySpec (String name, BigDecimal defaultValue) {
        return PropertySpecBuilderImpl.
                forClass(new BigDecimalFactory()).
                name(name).
                setDefaultValue(defaultValue).
                finish();
    }

    @Override
    public PropertySpec<BigDecimal> bigDecimalPropertySpecWithValues (String name, BigDecimal... values) {
        return PropertySpecBuilderImpl.
                forClass(new BigDecimalFactory()).
                name(name).addValues(values).
                markExhaustive().
                finish();
    }

    @Override
    public PropertySpec<Boolean> booleanPropertySpec (String name) {
        return simpleOptionalPropertySpec(name, new ThreeStateFactory());
    }

    @Override
    public PropertySpec<Boolean> notNullableBooleanPropertySpec (String name) {
        return simpleOptionalPropertySpec(name, new BooleanFactory());
    }

    // Hide utility class constructor
    protected OptionalPropertySpecFactory () {
    }
}