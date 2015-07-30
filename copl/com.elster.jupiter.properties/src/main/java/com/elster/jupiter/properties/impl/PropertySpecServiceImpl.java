package com.elster.jupiter.properties.impl;

import com.elster.jupiter.properties.BasicPropertySpec;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BoundedBigDecimalPropertySpecImpl;
import com.elster.jupiter.properties.BoundedLongPropertySpecImpl;
import com.elster.jupiter.properties.CanFindByStringKey;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.ListValuePropertySpec;
import com.elster.jupiter.properties.LongFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.RelativePeriodFactory;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.StringReferenceFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.math.BigDecimal;

/**
 * Provides an implementation for the {@link PropertySpecService} interface
 * and registers as an OSGi component to be used by other dependent modules.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-17 (10:59)
 */
@Component(name = "com.elster.jupiter.properties.propertyspecservice", service = PropertySpecService.class)
public class PropertySpecServiceImpl implements PropertySpecService {

    private volatile TimeService timeService;

    // For OSGi purposes
    public PropertySpecServiceImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public PropertySpecServiceImpl(TimeService timeService) {
        this();
        this.setTimeService(timeService);
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    @Override
    public PropertySpec basicPropertySpec(String name, boolean required, ValueFactory valueFactory) {
        return new BasicPropertySpec(name, required, valueFactory);
    }

    @Override
    public PropertySpec stringPropertySpecWithValues(String name, boolean required, String... values) {
        PropertySpecBuilder builder = PropertySpecBuilderImpl.forClass(new StringFactory());
        if (required) {
            builder.markRequired();
        }
        return builder.name(name).addValues(values).markExhaustive().finish();
    }

    @Override
    public PropertySpec stringPropertySpec(String name, boolean required, String defaultValue) {
        PropertySpecBuilder builder = PropertySpecBuilderImpl.forClass(new StringFactory());
        if (required) {
            builder.markRequired();
        }
        return builder.name(name).setDefaultValue(defaultValue).finish();
    }

    @Override
    public PropertySpec stringPropertySpecWithValuesAndDefaultValue(String name, boolean required, String defaultValue, String... values) {
        PropertySpecBuilder builder = PropertySpecBuilderImpl.forClass(new StringFactory());
        if (required) {
            builder.markRequired();
        }
        return builder.name(name).addValues(values).markExhaustive().setDefaultValue(defaultValue).finish();
    }

    @Override
    public PropertySpec bigDecimalPropertySpecWithValues(String name, boolean required, BigDecimal... values) {
        PropertySpecBuilder builder = PropertySpecBuilderImpl.forClass(new BigDecimalFactory());
        if (required) {
            builder.markRequired();
        }
        return builder.name(name).addValues(values).markExhaustive().finish();
    }

    @Override
    public PropertySpec bigDecimalPropertySpec(String name, boolean required, BigDecimal defaultValue) {
        PropertySpecBuilder builder = PropertySpecBuilderImpl.forClass(new BigDecimalFactory());
        if (required) {
            builder.markRequired();
        }
        return builder.name(name).setDefaultValue(defaultValue).finish();
    }

    @Override
    public PropertySpec relativePeriodPropertySpec(String name, boolean required, RelativePeriod defaultRelativePeriod) {
        PropertySpecBuilder builder = PropertySpecBuilderImpl.forClass(new RelativePeriodFactory(timeService));
        if (required) {
            builder.markRequired();
        }
        return builder.name(name).setDefaultValue(defaultRelativePeriod).finish();
    }

    /*@Override
    public PropertySpec newPropertySpec(ValueFactory valueFactory, String name, boolean required, Object defaultObject) {
        PropertySpecBuilder builder = PropertySpecBuilderImpl.forClass(valueFactory);
        if (required) {
            builder.markRequired();
        }
        return builder.name(name).setDefaultValue(defaultObject).finish();
    }*/

    @Override
    public PropertySpec positiveDecimalPropertySpec(String name, boolean required) {
        BoundedBigDecimalPropertySpecImpl propertySpec = new BoundedBigDecimalPropertySpecImpl(name, BigDecimal.ZERO, null);
        propertySpec.setRequired(required);
        return propertySpec;
    }

    @Override
    public PropertySpec boundedDecimalPropertySpec(String name, boolean required, BigDecimal lowerLimit, BigDecimal upperLimit) {
        BoundedBigDecimalPropertySpecImpl propertySpec = new BoundedBigDecimalPropertySpecImpl(name, lowerLimit, upperLimit);
        propertySpec.setRequired(required);
        return propertySpec;
    }

    @Override
    public <T extends HasIdAndName> PropertySpec listValuePropertySpec(String name, boolean required, CanFindByStringKey<T> finder, T... values) {
        return new ListValuePropertySpec<>(name, required, finder, values);
    }

    @Override
    public PropertySpecBuilder newPropertySpecBuilder(ValueFactory valueFactory) {
        return PropertySpecBuilderImpl.forClass(valueFactory);
    }

    @Override
    public PropertySpec longPropertySpec(String name, boolean required, Long defaultValue) {
        PropertySpecBuilder builder = PropertySpecBuilderImpl.forClass(new LongFactory());
        if (required) {
            builder.markRequired();
        }
        return builder.name(name).setDefaultValue(defaultValue).finish();
    }

    @Override
    public PropertySpec longPropertySpecWithValues(String name, boolean required, Long... values) {
        PropertySpecBuilder builder = PropertySpecBuilderImpl.forClass(new LongFactory());
        if (required) {
            builder.markRequired();
        }
        return builder.name(name).addValues(values).markExhaustive().finish();
    }

    @Override
    public PropertySpec positiveLongPropertySpec(String name, boolean required) {
        BoundedLongPropertySpecImpl propertySpec = new BoundedLongPropertySpecImpl(name, 0L, null);
        propertySpec.setRequired(required);
        return propertySpec;
    }

    @Override
    public PropertySpec boundedLongPropertySpec(String name, boolean required, Long lowerLimit, Long upperLimit) {
        BoundedLongPropertySpecImpl propertySpec = new BoundedLongPropertySpecImpl(name, lowerLimit, upperLimit);
        propertySpec.setRequired(required);
        return propertySpec;
    }

    public <T extends HasIdAndName> PropertySpec stringReferencePropertySpec(String name, boolean required, CanFindByStringKey<T> finder, T[] values) {
        PropertySpecBuilder builder = PropertySpecBuilderImpl.forClass(new StringReferenceFactory<T>(finder));
        if (required) {
            builder.markRequired();
        }
        return builder.name(name).addValues(values).markExhaustive().finish();
    };

}