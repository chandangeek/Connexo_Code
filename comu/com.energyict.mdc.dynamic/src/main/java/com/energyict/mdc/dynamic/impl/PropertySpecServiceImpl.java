package com.energyict.mdc.dynamic.impl;

import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.BigDecimalFactory;
import com.energyict.mdc.dynamic.ObisCodeValueFactory;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecBuilder;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.ReferencePropertySpecFinderProvider;
import com.energyict.mdc.dynamic.StringFactory;
import com.energyict.mdc.dynamic.ValueFactory;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * Provides an implementation for the {@link PropertySpecService} interface
 * and registers as an OSGi component to be used by other dependent modules.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-17 (10:59)
 */
@Component(name = "com.energyict.mdc.dynamic.propertyspecservice", service = PropertySpecService.class)
public class PropertySpecServiceImpl implements PropertySpecService {

    private static final Logger LOGGER = Logger.getLogger(PropertySpecServiceImpl.class.getName());

    private volatile List<ReferencePropertySpecFinderProvider> factoryProviders = new CopyOnWriteArrayList<>();

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
    public PropertySpec<ObisCode> obisCodePropertySpecWithValues(String name, boolean required, ObisCode... values) {
        PropertySpecBuilder<ObisCode> builder = PropertySpecBuilderImpl.forClass(new ObisCodeValueFactory());
        if (required) {
            builder.markRequired();
        }
        return builder.name(name).addValues(values).finish();
    }

    @Override
    public <T extends IdBusinessObject> PropertySpec<T> referencePropertySpec(String name, boolean required, IdBusinessObjectFactory<T> valueFactory) {
        return new LegacyReferencePropertySpec<>(name, required, valueFactory);
    }

    @Override
    public PropertySpec referencePropertySpec(String name, boolean required, FactoryIds factoryId) {
        return new JupiterReferencePropertySpec(name, required, this.finderFor(factoryId));
    }

    private CanFindByLongPrimaryKey<? extends HasId> finderFor(FactoryIds factoryId) {
        for (ReferencePropertySpecFinderProvider factoryProvider : this.factoryProviders) {
            for (CanFindByLongPrimaryKey<? extends HasId> finder : factoryProvider.finders()) {
                if (factoryId.equals(finder.factoryId())) {
                    return finder;
                }
            }
        }
        throw new RuntimeException("No finder component registered for factory " + factoryId.name());
    }

    @Override
    public <T> PropertySpecBuilder<T> newPropertySpecBuilder(ValueFactory<T> valueFactory) {
        return PropertySpecBuilderImpl.forClass(valueFactory);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addFactoryProvider(ReferencePropertySpecFinderProvider factoryProvider) {
        this.validateUnique(factoryProvider);
        this.factoryProviders.add(factoryProvider);
    }

    private void validateUnique(ReferencePropertySpecFinderProvider factoryProvider) {
        Set<FactoryIds> existingFactoryIds = this.existingFactoryIds();
        for (CanFindByLongPrimaryKey<? extends HasId> finder : factoryProvider.finders()) {
            if (existingFactoryIds.contains(finder.factoryId())) {
                LOGGER.severe("Factory " + finder.factoryId().name() + " already registered, ignoring ReferencePropertySpecFinderProvider " + factoryProvider.toString());
            }
        }
    }

    private Set<FactoryIds> existingFactoryIds() {
        EnumSet<FactoryIds> factoryIds = EnumSet.noneOf(FactoryIds.class);
        for (ReferencePropertySpecFinderProvider factoryProvider : this.factoryProviders) {
            for (CanFindByLongPrimaryKey<? extends HasId> finder : factoryProvider.finders()) {
                factoryIds.add(finder.factoryId());
            }

        }
        return factoryIds;
    }

    public void removeFactoryProvider(ReferencePropertySpecFinderProvider factoryProvider) {
        this.factoryProviders.remove(factoryProvider);
    }

}