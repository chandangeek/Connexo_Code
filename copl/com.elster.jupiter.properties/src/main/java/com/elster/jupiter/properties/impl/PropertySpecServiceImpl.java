package com.elster.jupiter.properties.impl;

import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.BoundedLongPropertySpecImpl;
import com.elster.jupiter.properties.CanFindByStringKey;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.ListValuePropertySpec;
import com.elster.jupiter.properties.LongFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.properties.PropertySpecBuilderWizard;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.TimeZoneFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.time.TimeService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.TimeZone;

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
    private volatile OrmService ormService;

    // For OSGi purposes
    public PropertySpecServiceImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public PropertySpecServiceImpl(TimeService timeService, OrmService ormService) {
        this();
        this.setTimeService(timeService);
        this.setOrmService(ormService);
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }


    @Override
    public <T> PropertySpecBuilderWizard.NlsOptions<T> specForValuesOf(ValueFactory<T> valueFactory) {
        return new PropertySpecBuilderNlsOptions<>(valueFactory);
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<String> stringSpec() {
        return this.specForValuesOf(new StringFactory());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<Boolean> booleanSpec() {
        return this.specForValuesOf(new BooleanFactory());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<Long> longSpec() {
        return this.specForValuesOf(new LongFactory());
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<BigDecimal> bigDecimalSpec() {
        return this.specForValuesOf(new BigDecimalFactory());
    }

    @Override
    public <T> PropertySpecBuilderWizard.NlsOptions<TimeZone> timezoneSpec() {
        return this.specForValuesOf(new TimeZoneFactory());
    }

    @Override
    public <T> PropertySpecBuilderWizard.NlsOptions<T> referenceSpec(Class<T> apiClass) {
        ReferenceValueFactory<T> valueFactory = new ReferenceValueFactory<T>(this.ormService).init(apiClass);
        return this.specForValuesOf(valueFactory);
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<BigDecimal> boundedBigDecimalSpec(BigDecimal lowerLimit, BigDecimal upperLimit) {
        return new PartiallyInitializedPropertySpecBuilderNlsOptions<>(
                new BigDecimalFactory(),
                new BoundedBigDecimalPropertySpecImpl(lowerLimit, upperLimit));
    }

    @Override
    public <T extends HasIdAndName> PropertySpec listValuePropertySpec(String name, boolean required, CanFindByStringKey<T> finder, T... values) {
        return new ListValuePropertySpec<>(name, required, finder, values);
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

}