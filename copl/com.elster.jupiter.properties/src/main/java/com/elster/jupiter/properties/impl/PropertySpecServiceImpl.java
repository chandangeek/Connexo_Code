package com.elster.jupiter.properties.impl;

import java.math.BigDecimal;
import java.util.logging.Logger;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import com.elster.jupiter.properties.BasicPropertySpec;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BoundedBigDecimalPropertySpecImpl;
import com.elster.jupiter.properties.FindById;
import com.elster.jupiter.properties.ListValueEntry;
import com.elster.jupiter.properties.ListValue;
import com.elster.jupiter.properties.ListValuePropertySpec;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ValueFactory;

/**
 * Provides an implementation for the {@link PropertySpecService} interface
 * and registers as an OSGi component to be used by other dependent modules.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-17 (10:59)
 */
@Component(name = "com.elster.jupiter.properties.propertyspecservice", service = PropertySpecService.class)
public class PropertySpecServiceImpl implements PropertySpecService {

    private static final Logger LOGGER = Logger.getLogger(PropertySpecServiceImpl.class.getName());

    @Activate
    public void activate(){

    }

    @Override
    public <T> PropertySpec<T> basicPropertySpec(String name, boolean required, ValueFactory<T> valueFactory) {
        return new BasicPropertySpec<>(name, required, valueFactory);
    }

    @Override
    public PropertySpec<String> stringPropertySpecWithValues(String name, boolean required, String... values) {
        PropertySpecBuilder<String> builder = PropertySpecBuilderImpl.forClass(new StringFactory());
        if (required) {
            builder.markRequired();
        }
        return builder.name(name).addValues(values).markExhaustive().finish();
    }

    @Override
    public PropertySpec<String> stringPropertySpec(String name, boolean required, String defaultValue) {
        PropertySpecBuilder<String> builder = PropertySpecBuilderImpl.forClass(new StringFactory());
        if (required) {
            builder.markRequired();
        }
        return builder.name(name).setDefaultValue(defaultValue).finish();
    }

    @Override
    public PropertySpec<String> stringPropertySpecWithValuesAndDefaultValue(String name, boolean required, String defaultValue, String... values) {
        PropertySpecBuilder<String> builder = PropertySpecBuilderImpl.forClass(new StringFactory());
        if (required) {
            builder.markRequired();
        }
        return builder.name(name).addValues(values).markExhaustive().setDefaultValue(defaultValue).finish();
    }

    @Override
    public PropertySpec<BigDecimal> bigDecimalPropertySpecWithValues(String name, boolean required, BigDecimal... values) {
        PropertySpecBuilder<BigDecimal> builder = PropertySpecBuilderImpl.forClass(new BigDecimalFactory());
        if (required) {
            builder.markRequired();
        }
        return builder.name(name).addValues(values).markExhaustive().finish();
    }

    @Override
    public PropertySpec<BigDecimal> bigDecimalPropertySpec(String name, boolean required, BigDecimal defaultValue) {
        PropertySpecBuilder<BigDecimal> builder = PropertySpecBuilderImpl.forClass(new BigDecimalFactory());
        if (required) {
            builder.markRequired();
        }
        return builder.name(name).setDefaultValue(defaultValue).finish();
    }

    @Override
    public PropertySpec<BigDecimal> positiveDecimalPropertySpec(String name, boolean required) {
        BoundedBigDecimalPropertySpecImpl propertySpec = new BoundedBigDecimalPropertySpecImpl(name, BigDecimal.ZERO, null);
        propertySpec.setRequired(required);
        return propertySpec;
    }

    @Override
    public PropertySpec<BigDecimal> boundedDecimalPropertySpec(String name, boolean required, BigDecimal lowerLimit, BigDecimal upperLimit) {
        BoundedBigDecimalPropertySpecImpl propertySpec = new BoundedBigDecimalPropertySpecImpl(name, lowerLimit, upperLimit);
        propertySpec.setRequired(required);
        return propertySpec;
    }

    @Override
    public <T extends ListValueEntry> PropertySpec<ListValue<T>> listValuePropertySpec(String name, boolean required, FindById<T> finder, T... values) {
        return new ListValuePropertySpec<>(name, required, finder, values);
    }

    @Override
    public <T> PropertySpecBuilder<T> newPropertySpecBuilder(ValueFactory<T> valueFactory) {
        return PropertySpecBuilderImpl.forClass(valueFactory);
    }
}