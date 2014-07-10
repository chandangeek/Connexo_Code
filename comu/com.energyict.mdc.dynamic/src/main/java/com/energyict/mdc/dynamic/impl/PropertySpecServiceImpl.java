package com.energyict.mdc.dynamic.impl;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import javax.inject.Inject;

import com.energyict.mdc.dynamic.NoFinderComponentFoundException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.properties.ValueFactory;
import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.ObisCodeValueFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.ReferencePropertySpecFinderProvider;

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
    private volatile com.elster.jupiter.properties.PropertySpecService basicPropertySpecService;
    
    public PropertySpecServiceImpl() {
    }

    @Inject
    public PropertySpecServiceImpl(com.elster.jupiter.properties.PropertySpecService basicPropertySpec) {
        super();
        this.setBasicPropertySpecService(basicPropertySpec);
    }
    
    @Activate
    public void activate(){

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
        throw new NoFinderComponentFoundException(factoryId);
    }

    @Override
    public <T> PropertySpecBuilder<T> newPropertySpecBuilder(ValueFactory<T> valueFactory) {
        return PropertySpecBuilderImpl.forClass(valueFactory);
    }

    @Override
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addFactoryProvider(ReferencePropertySpecFinderProvider factoryProvider) {
        if(this.validateUnique(factoryProvider)){
            this.factoryProviders.add(factoryProvider);
        }
    }
    
    @Reference
    public void setBasicPropertySpecService(com.elster.jupiter.properties.PropertySpecService propertySpecService) {
        this.basicPropertySpecService = propertySpecService;
    }

    private boolean validateUnique(ReferencePropertySpecFinderProvider factoryProvider) {
        Set<FactoryIds> existingFactoryIds = this.existingFactoryIds();
        for (CanFindByLongPrimaryKey<? extends HasId> finder : factoryProvider.finders()) {
            if (existingFactoryIds.contains(finder.factoryId())) {
                LOGGER.warning("Factory " + finder.factoryId().name() + " already registered, ignoring ReferencePropertySpecFinderProvider " + factoryProvider.toString());
                return false;
            }
        }
        return true;
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