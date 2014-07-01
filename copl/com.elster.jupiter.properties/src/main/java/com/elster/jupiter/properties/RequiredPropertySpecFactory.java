package com.elster.jupiter.properties;

import java.math.BigDecimal;

public class RequiredPropertySpecFactory extends PropertySpecFactoryImpl {

    public static PropertySpecFactory newInstance () {
        return new RequiredPropertySpecFactory();
    }

    @Override
    public PropertySpec<String> stringPropertySpec(String name) {
        return simpleRequiredPropertySpec(name, new StringFactory());
    }

    @Override
    public PropertySpec<String> stringPropertySpec (String name, String defaultValue) {
        return PropertySpecBuilderImpl.
                forClass(new StringFactory()).
                name(name).
                markRequired().
                setDefaultValue(defaultValue).
                finish();
    }

    @Override
    public PropertySpec<String> stringPropertySpecWithValues (String name, String... values) {
        return PropertySpecBuilderImpl.
                forClass(new StringFactory()).
                name(name).
                markRequired().
                addValues(values).
                markExhaustive().
                finish();
    }

    @Override
    public PropertySpec<BigDecimal> bigDecimalPropertySpec(String name) {
        return simpleRequiredPropertySpec(name, new BigDecimalFactory());
    }

    @Override
    public PropertySpec<BigDecimal> bigDecimalPropertySpec(String name, BigDecimal defaultValue) {
        return PropertySpecBuilderImpl.
                forClass(new BigDecimalFactory()).
                name(name).
                markRequired().
                setDefaultValue(defaultValue).
                finish();
    }

    @Override
    public PropertySpec<BigDecimal> bigDecimalPropertySpecWithValues (String name, BigDecimal... values) {
        return PropertySpecBuilderImpl.
                forClass(new BigDecimalFactory()).
                name(name).
                markRequired().
                addValues(values).
                markExhaustive().
                finish();
    }

    @Override
    public PropertySpec<Boolean> booleanPropertySpec(String name) {
        return simpleRequiredPropertySpec(name, new ThreeStateFactory());
    }
    
    @Override
    public PropertySpec<Boolean> notNullableBooleanPropertySpec (String name) {
        return simpleRequiredPropertySpec(name, new BooleanFactory());
    }

    // Hide utility class constructor
    protected RequiredPropertySpecFactory () {
    }
}