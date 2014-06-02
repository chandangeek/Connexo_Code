package com.energyict.mdc.dynamic;

import com.energyict.mdc.common.HexString;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.TimeOfDay;
import com.energyict.mdc.dynamic.impl.PropertySpecBuilderImpl;
import com.energyict.mdc.dynamic.impl.PropertySpecFactoryImpl;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Provides factory services for required {@link PropertySpec}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-29 (16:46)
 */
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
    public PropertySpec<String> largeStringPropertySpec(String name) {
        return simpleRequiredPropertySpec(name, new LargeStringFactory());
    }

    @Override
    public PropertySpec<HexString> hexStringPropertySpec(String name) {
        return simpleRequiredPropertySpec(name, new HexStringFactory());
    }

    @Override
    public PropertySpec<Password> passwordPropertySpec(String name) {
        return simpleRequiredPropertySpec(name, new PasswordFactory());
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

    @Override
    public PropertySpec<Date> datePropertySpec(String name) {
        return simpleRequiredPropertySpec(name, new DateFactory());
    }

    @Override
    public PropertySpec<TimeOfDay> timeOfDayPropertySpec(String name) {
        return simpleRequiredPropertySpec(name, new TimeOfDayFactory());
    }

    @Override
    public PropertySpec<Date> dateTimePropertySpec(String name) {
        return simpleRequiredPropertySpec(name, new DateAndTimeFactory());
    }

    @Override
    public PropertySpec<TimeDuration> timeDurationPropertySpec(String name) {
        return simpleRequiredPropertySpec(name, new TimeDurationValueFactory());
    }

    @Override
    public PropertySpec<TimeDuration> timeDurationPropertySpec (String name, TimeDuration defaultValue) {
        return PropertySpecBuilderImpl.
                forClass(new TimeDurationValueFactory()).
                name(name).
                markRequired().
                setDefaultValue(defaultValue).
                finish();
    }

    @Override
    public PropertySpec<ObisCode> obisCodePropertySpec(String name) {
        return simpleRequiredPropertySpec(name, new ObisCodeValueFactory());
    }

    @Override
    public PropertySpec<ObisCode> obisCodePropertySpecWithValuesExhaustive(String name, ObisCode... values) {
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