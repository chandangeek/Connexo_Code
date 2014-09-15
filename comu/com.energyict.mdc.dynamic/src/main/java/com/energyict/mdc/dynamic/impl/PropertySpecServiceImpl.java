package com.energyict.mdc.dynamic.impl;

import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.dynamic.NoFinderComponentFoundException;
import com.energyict.mdc.dynamic.ObisCodeValueFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.ReferencePropertySpecFinderProvider;
import com.energyict.mdc.dynamic.TimeDurationValueFactory;

import com.elster.jupiter.properties.FindById;
import com.elster.jupiter.properties.ListValue;
import com.elster.jupiter.properties.ListValueEntry;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.properties.ValueFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides an implementation for the {@link PropertySpecService} interface
 * and registers as an OSGi component to be used by other dependent modules.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-17 (10:59)
 */
@Component(name = "com.energyict.mdc.dynamic.propertyspecservice", service = PropertySpecService.class)
public class PropertySpecServiceImpl implements PropertySpecService {

    private volatile Map<Class<? extends CanFindByLongPrimaryKey>, CanFindByLongPrimaryKey<? extends HasId>> finders = new ConcurrentHashMap<>();
    private volatile com.elster.jupiter.properties.PropertySpecService basicPropertySpecService;

    public PropertySpecServiceImpl() {
    }

    @Inject
    public PropertySpecServiceImpl(com.elster.jupiter.properties.PropertySpecService basicPropertySpec) {
        this();
        this.setBasicPropertySpecService(basicPropertySpec);
    }

    @Override
    public <T> PropertySpec<T> basicPropertySpec(String name, boolean required, ValueFactory<T> valueFactory) {
        return basicPropertySpecService.basicPropertySpec(name, required, valueFactory);
    }

    @Override
    public PropertySpec<String> stringPropertySpecWithValues(String name, boolean required, String... values) {
        return basicPropertySpecService.stringPropertySpecWithValues(name, required, values);
    }

    @Override
    public PropertySpec<String> stringPropertySpec(String name, boolean required, String defaultValue) {
        return basicPropertySpecService.stringPropertySpec(name, required, defaultValue);
    }

    @Override
    public PropertySpec<String> stringPropertySpecWithValuesAndDefaultValue(String name, boolean required, String defaultValue, String... values) {
        return basicPropertySpecService.stringPropertySpecWithValuesAndDefaultValue(name, required, defaultValue, values);
    }

    @Override
    public PropertySpec<BigDecimal> bigDecimalPropertySpecWithValues(String name, boolean required, BigDecimal... values) {
        return basicPropertySpecService.bigDecimalPropertySpecWithValues(name, required, values);
    }

    @Override
    public PropertySpec<BigDecimal> bigDecimalPropertySpec(String name, boolean required, BigDecimal defaultValue) {
        return basicPropertySpecService.bigDecimalPropertySpec(name, required, defaultValue);
    }

    @Override
    public PropertySpec<BigDecimal> positiveDecimalPropertySpec(String name, boolean required) {
        return basicPropertySpecService.positiveDecimalPropertySpec(name, required);
    }

    @Override
    public PropertySpec<BigDecimal> boundedDecimalPropertySpec(String name, boolean required, BigDecimal lowerLimit, BigDecimal upperLimit) {
        return basicPropertySpecService.boundedDecimalPropertySpec(name, required, lowerLimit, upperLimit);
    }

    @Override
    public PropertySpec<TimeDuration> timeDurationPropertySpec(String name, boolean required, TimeDuration defaultValue) {
        return PropertySpecBuilderImpl.
                forClass(new TimeDurationValueFactory()).
                name(name).
                setDefaultValue(defaultValue).
                finish();
    }

    @Override
    public PropertySpec<ObisCode> obisCodePropertySpecWithValues(String name, boolean required, ObisCode... values) {
        return this.obisCodePropertySpecWithValues(name, required, false, values);
    }

    @Override
    public PropertySpec<ObisCode> obisCodePropertySpecWithValuesExhaustive(String name, boolean required, ObisCode... values) {
        return this.obisCodePropertySpecWithValues(name, required, true, values);
    }

    private PropertySpec<ObisCode> obisCodePropertySpecWithValues(String name, boolean required, boolean exhaustive, ObisCode... values) {
        PropertySpecBuilder<ObisCode> builder = PropertySpecBuilderImpl.forClass(new ObisCodeValueFactory());
        if (required) {
            builder.markRequired();
        }
        if (exhaustive) {
            builder.markExhaustive();
        }
        return builder.name(name).addValues(values).finish();
    }

    @Override
    public PropertySpec referencePropertySpec(String name, boolean required, FactoryIds factoryId) {
        return new JupiterReferencePropertySpec<>(name, required, this.finderFor(factoryId));
    }

    private CanFindByLongPrimaryKey<? extends HasId> finderFor(FactoryIds factoryId) {
        for (CanFindByLongPrimaryKey<? extends HasId> finder : finders.values()) {
            if(factoryId.equals(finder.factoryId())){
                return finder;
            }
        }
        throw new NoFinderComponentFoundException(factoryId);
    }

    @Override
    public <T> PropertySpecBuilder<T> newPropertySpecBuilder(ValueFactory<T> valueFactory) {
        return PropertySpecBuilderImpl.forClass(valueFactory);
    }

    @Override
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addFactoryProvider(ReferencePropertySpecFinderProvider factoryProvider) {
        for (CanFindByLongPrimaryKey<? extends HasId> finder : factoryProvider.finders()) {
            finders.put(finder.getClass(), finder);
        }
    }

    @Reference
    public void setBasicPropertySpecService(com.elster.jupiter.properties.PropertySpecService propertySpecService) {
        this.basicPropertySpecService = propertySpecService;
    }

    public void removeFactoryProvider(ReferencePropertySpecFinderProvider factoryProvider) {
        for (CanFindByLongPrimaryKey<? extends HasId> finder : factoryProvider.finders()) {
            this.finders.remove(finder);
        }
    }

    @Override
    public <T extends ListValueEntry> PropertySpec<ListValue<T>> listValuePropertySpec(String name, boolean required, FindById<T> finder, T... values) {
        return basicPropertySpecService.listValuePropertySpec(name, required, finder, values);
    }

}