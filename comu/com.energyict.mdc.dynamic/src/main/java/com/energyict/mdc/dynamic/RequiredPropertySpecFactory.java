package com.energyict.mdc.dynamic;

import com.energyict.mdc.common.HexString;
import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.TimeOfDay;
import com.energyict.mdc.common.coordinates.SpatialCoordinates;
import com.energyict.mdc.common.ean.Ean13;
import com.energyict.mdc.common.ean.Ean18;
import com.energyict.mdc.dynamic.impl.BoundedBigDecimalPropertySpecImpl;
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
    public PropertySpec<String> stringPropertySpecWithValuesAndDefaultValue (String name, String defaultValue, String... values) {
        return PropertySpecBuilderImpl.
                forClass(new StringFactory()).
                name(name).
                markRequired().
                addValues(values).
                markExhaustive().
                setDefaultValue(defaultValue).
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
    public PropertySpec<BigDecimal> boundedDecimalPropertySpec (String name, BigDecimal lowerLimit, BigDecimal upperLimit) {
        return new BoundedBigDecimalPropertySpecImpl(name, lowerLimit, upperLimit);
    }

    @Override
    public PropertySpec<BigDecimal> positiveDecimalPropertySpec (String name) {
        return new BoundedBigDecimalPropertySpecImpl(name, BigDecimal.ZERO, null);
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
    public PropertySpec<TimeDuration> timeDurationPropertySpecWithValues(String name, TimeDuration... values) {
        return PropertySpecBuilderImpl.
                forClass(new TimeDurationValueFactory()).
                name(name).
                markRequired().
                addValues(values).
                finish();
    }

    @Override
    public PropertySpec<ObisCode> obisCodePropertySpec(String name) {
        return simpleRequiredPropertySpec(name, new ObisCodeValueFactory());
    }

    @Override
    public PropertySpec<ObisCode> obisCodePropertySpecWithValues(String name, ObisCode... values) {
        return PropertySpecBuilderImpl.
                forClass(new ObisCodeValueFactory()).
                name(name).
                markRequired().
                addValues(values).
                finish();
    }

    @Override
    public <D extends IdBusinessObject> PropertySpec<D> referencePropertySpec(String name, IdBusinessObjectFactory<D> factory) {
        return PropertySpecBuilderImpl.forReference(factory).markRequired().name(name).finish();
    }

    @Override
    public PropertySpec<Ean13> ean13PropertySpec(String name) {
        return simpleRequiredPropertySpec(name, new Ean13Factory());
    }

    @Override
    public PropertySpec<Ean18> ean18PropertySpec(String name) {
        return simpleRequiredPropertySpec(name, new Ean18Factory());
    }

    @Override
    public PropertySpec<String> encryptedStringPropertySpec(String name) {
        return simpleRequiredPropertySpec(name, new EncryptedStringFactory());
    }

    @Override
    public PropertySpec<SpatialCoordinates> spatialCoordinatesPropertySpec(String name) {
        return simpleRequiredPropertySpec(name, new SpatialCoordinatesFactory());
    }

    // Hide utility class constructor
    private RequiredPropertySpecFactory () {
    }

}