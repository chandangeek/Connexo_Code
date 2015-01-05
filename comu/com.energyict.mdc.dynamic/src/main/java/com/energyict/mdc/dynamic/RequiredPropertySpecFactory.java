package com.energyict.mdc.dynamic;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ThreeStateFactory;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.impl.PropertySpecBuilderImpl;
import com.energyict.mdc.dynamic.impl.PropertySpecFactoryImpl;
import java.math.BigDecimal;

/**
 * Provides factory services for required {@link PropertySpec}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-29 (16:46)
 */
@Deprecated
public class RequiredPropertySpecFactory extends PropertySpecFactoryImpl {

    public static PropertySpecFactory newInstance () {
        return new RequiredPropertySpecFactory();
    }

    @Override
    public PropertySpec stringPropertySpec(String name) {
        return simpleRequiredPropertySpec(name, new StringFactory());
    }

    @Override
    public PropertySpec stringPropertySpec (String name, String defaultValue) {
        return PropertySpecBuilderImpl.
                forClass(new StringFactory()).
                name(name).
                markRequired().
                setDefaultValue(defaultValue).
                finish();
    }

    @Override
    public PropertySpec stringPropertySpecWithValues (String name, String... values) {
        return PropertySpecBuilderImpl.
                forClass(new StringFactory()).
                name(name).
                markRequired().
                addValues(values).
                markExhaustive().
                finish();
    }

    @Override
    public PropertySpec largeStringPropertySpec(String name) {
        return simpleRequiredPropertySpec(name, new LargeStringFactory());
    }

    @Override
    public PropertySpec hexStringPropertySpec(String name) {
        return simpleRequiredPropertySpec(name, new HexStringFactory());
    }

    @Override
    public PropertySpec bigDecimalPropertySpec(String name) {
        return simpleRequiredPropertySpec(name, new BigDecimalFactory());
    }

    @Override
    public PropertySpec bigDecimalPropertySpec(String name, BigDecimal defaultValue) {
        return PropertySpecBuilderImpl.
                forClass(new BigDecimalFactory()).
                name(name).
                markRequired().
                setDefaultValue(defaultValue).
                finish();
    }

    @Override
    public PropertySpec bigDecimalPropertySpecWithValues (String name, BigDecimal... values) {
        return PropertySpecBuilderImpl.
                forClass(new BigDecimalFactory()).
                name(name).
                markRequired().
                addValues(values).
                markExhaustive().
                finish();
    }

    @Override
    public PropertySpec booleanPropertySpec(String name) {
        return simpleRequiredPropertySpec(name, new ThreeStateFactory());
    }

    @Override
    public PropertySpec notNullableBooleanPropertySpec (String name) {
        return simpleRequiredPropertySpec(name, new BooleanFactory());
    }

    @Override
    public PropertySpec datePropertySpec(String name) {
        return simpleRequiredPropertySpec(name, new DateFactory());
    }

    @Override
    public PropertySpec timeOfDayPropertySpec(String name) {
        return simpleRequiredPropertySpec(name, new TimeOfDayFactory());
    }

    @Override
    public PropertySpec dateTimePropertySpec(String name) {
        return simpleRequiredPropertySpec(name, new DateAndTimeFactory());
    }

    @Override
    public PropertySpec timeDurationPropertySpec(String name) {
        return simpleRequiredPropertySpec(name, new TimeDurationValueFactory());
    }

    @Override
    public PropertySpec timeDurationPropertySpec (String name, TimeDuration defaultValue) {
        return PropertySpecBuilderImpl.
                forClass(new TimeDurationValueFactory()).
                name(name).
                markRequired().
                setDefaultValue(defaultValue).
                finish();
    }

    @Override
    public PropertySpec obisCodePropertySpec(String name) {
        return simpleRequiredPropertySpec(name, new ObisCodeValueFactory());
    }

    @Override
    public PropertySpec obisCodePropertySpecWithValuesExhaustive(String name, ObisCode... values) {
        return PropertySpecBuilderImpl.
                forClass(new ObisCodeValueFactory()).
                name(name).
                markRequired().
                markExhaustive().
                addValues(values).
                finish();
    }

    // Hide utility class constructor
    private RequiredPropertySpecFactory () {
    }

}