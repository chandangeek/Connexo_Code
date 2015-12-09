package com.energyict.mdc.dynamic.impl;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.CanFindByStringKey;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.properties.RelativePeriodFactory;
import com.elster.jupiter.properties.TimeZoneFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.HasId;
import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.HexString;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.HexStringFactory;
import com.energyict.mdc.dynamic.ObisCodeValueFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.ReferencePropertySpecFinderProvider;
import com.energyict.mdc.dynamic.TimeDurationValueFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Map;
import java.util.TimeZone;
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

    private volatile DataModel dataModel;
    private volatile DataVaultService dataVaultService;
    private volatile TimeService timeService;
    private volatile com.elster.jupiter.properties.PropertySpecService basicPropertySpecService;
    private Map<Class<? extends CanFindByLongPrimaryKey>, CanFindByLongPrimaryKey<? extends HasId>> finders = new ConcurrentHashMap<>();

    // For OSGi purposes
    public PropertySpecServiceImpl() {
    }

    // For testing purposes
    @Inject
    public PropertySpecServiceImpl(com.elster.jupiter.properties.PropertySpecService basicPropertySpec, DataVaultService dataVaultService, TimeService timeService, OrmService ormService) {
        this();
        this.setBasicPropertySpecService(basicPropertySpec);
        this.setOrmService(ormService);
        this.setDataVaultService(dataVaultService);
        this.setTimeService(timeService);
        this.activate();
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel("DYN", "MDC Dynamic Services");
    }

    @Reference
    public void setDataVaultService(DataVaultService dataVaultService) {
        this.dataVaultService = dataVaultService;
    }


    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(DataVaultService.class).toInstance(dataVaultService);
            }
        };
    }

    @Override
    public PropertySpec basicPropertySpec(String name, boolean required, ValueFactory valueFactory) {
        return basicPropertySpecService.basicPropertySpec(name, required, valueFactory);
    }

    @Override
    public PropertySpec basicPropertySpec(String name, boolean required, Class<? extends ValueFactory> valueFactoryClass) {
        return this.basicPropertySpec(name, "", required, valueFactoryClass);
    }

    @Override
    public PropertySpec basicPropertySpec(String name, String description, boolean required, Class<? extends ValueFactory> valueFactoryClass) {
        return new com.elster.jupiter.properties.BasicPropertySpec(name, description, required, getValueFactory(valueFactoryClass));
    }

    @Override
    public PropertySpec stringPropertySpecWithValues(String name, boolean required, String... values) {
        return basicPropertySpecService.stringPropertySpecWithValues(name, required, values);
    }

    @Override
    public PropertySpec stringPropertySpec(String name, boolean required, String defaultValue) {
        return basicPropertySpecService.stringPropertySpec(name, required, defaultValue);
    }

    @Override
    public PropertySpec stringPropertySpecWithValuesAndDefaultValue(String name, boolean required, String defaultValue, String... values) {
        return basicPropertySpecService.stringPropertySpecWithValuesAndDefaultValue(name, required, defaultValue, values);
    }

    @Override
    public PropertySpec bigDecimalPropertySpecWithValues(String name, boolean required, BigDecimal... values) {
        return basicPropertySpecService.bigDecimalPropertySpecWithValues(name, required, values);
    }

    @Override
    public PropertySpec bigDecimalPropertySpec(String name, boolean required, BigDecimal defaultValue) {
        return basicPropertySpecService.bigDecimalPropertySpec(name, required, defaultValue);
    }

    @Override
    public PropertySpec positiveDecimalPropertySpec(String name, boolean required) {
        return basicPropertySpecService.positiveDecimalPropertySpec(name, required);
    }

    @Override
    public PropertySpec boundedDecimalPropertySpec(String name, boolean required, BigDecimal lowerLimit, BigDecimal upperLimit) {
        return basicPropertySpecService.boundedDecimalPropertySpec(name, required, lowerLimit, upperLimit);
    }

    @Override
    public PropertySpec timeDurationPropertySpec(String name, boolean required, TimeDuration defaultValue) {
        return this.timeDurationPropertySpec(name, "", required, defaultValue);
    }

    @Override
    public PropertySpec timeDurationPropertySpec(String name, String description, boolean required, TimeDuration defaultValue) {
        PropertySpecBuilder builder = PropertySpecBuilderImpl.forClass(new TimeDurationValueFactory());
        if (required) {
            builder.markRequired();
        }
        return builder
                .name(name)
                .description(description)
                .setDefaultValue(defaultValue)
                .finish();
    }

    @Override
    public PropertySpec obisCodePropertySpecWithValues(String name, boolean required, ObisCode... values) {
        return this.obisCodePropertySpecWithValues(name, "", required, false, values);
    }

    @Override
    public PropertySpec obisCodePropertySpecWithValues(String name, String description, boolean required, ObisCode... values) {
        return this.obisCodePropertySpecWithValues(name, description, required, false, values);
    }

    @Override
    public PropertySpec obisCodePropertySpecWithValuesExhaustive(String name, boolean required, ObisCode... values) {
        return this.obisCodePropertySpecWithValues(name, "", required, true, values);
    }

    @Override
    public PropertySpec obisCodePropertySpecWithValuesExhaustive(String name, String description, boolean required, ObisCode... values) {
        return this.obisCodePropertySpecWithValues(name, description, required, true, values);
    }

    private PropertySpec obisCodePropertySpecWithValues(String name, String description, boolean required, boolean exhaustive, ObisCode... values) {
        PropertySpecBuilder builder = PropertySpecBuilderImpl.forClass(new ObisCodeValueFactory());
        if (required) {
            builder.markRequired();
        }
        if (exhaustive) {
            builder.markExhaustive();
        }
        return builder.name(name).description(description).addValues(values).finish();
    }

    @Override
    public PropertySpecBuilder specForValuesOf(ValueFactory valueFactory) {
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

    @SuppressWarnings("unused")
    public void removeFactoryProvider(ReferencePropertySpecFinderProvider factoryProvider) {
        for (CanFindByLongPrimaryKey<? extends HasId> finder : factoryProvider.finders()) {
            this.finders.remove(finder);
        }
    }

    @Override
    public <T extends HasIdAndName> PropertySpec listValuePropertySpec(String name, boolean required, CanFindByStringKey<T> finder, T... values) {
        return basicPropertySpecService.listValuePropertySpec(name, required, finder, values);
    }

    public PropertySpecBuilder specForValuesOf(Class<? extends ValueFactory> valueFactoryClass) {
        return PropertySpecBuilderImpl.forClass(getValueFactory(valueFactoryClass));
    }

    @Override
    public ValueFactory getValueFactory(Class<? extends ValueFactory> valueFactoryClass) {
        return dataModel.getInstance(valueFactoryClass);
    }

    @Override
    public PropertySpec hexStringPropertySpec(String name, String description, boolean required, HexString defaultValue) {
        final PropertySpecBuilder propertySpecBuilder = PropertySpecBuilderImpl.forClass(new HexStringFactory())
                .name(name)
                .description(description)
                .setDefaultValue(defaultValue);
        if (required) {
            propertySpecBuilder.markRequired();
        }
        return propertySpecBuilder.finish();
    }

    @Override
    public PropertySpec booleanPropertySpec(String name, boolean required, Boolean defaultValue) {
        return this.booleanPropertySpec(name, "", required, defaultValue);
    }

    @Override
    public PropertySpec booleanPropertySpec(String name, String description, boolean required, Boolean defaultValue) {
        PropertySpecBuilder booleanPropertySpecBuilder =
                PropertySpecBuilderImpl
                        .forClass(new BooleanFactory())
                        .name(name)
                        .description(description)
                        .setDefaultValue(defaultValue);
        if (required) {
            booleanPropertySpecBuilder.markRequired();
        }
        return booleanPropertySpecBuilder.finish();
    }

    @Override
    public PropertySpec timeZonePropertySpec(String name, boolean required, TimeZone defaultValue) {
        return this.timeZonePropertySpec(name, "", required, defaultValue);
    }

    @Override
    public PropertySpec timeZonePropertySpec(String name, String description, boolean required, TimeZone defaultValue) {
        TimeZone[] possibleValues = {
                TimeZone.getTimeZone("GMT"),
                TimeZone.getTimeZone("Europe/Brussels"),
                TimeZone.getTimeZone("EST"),
                TimeZone.getTimeZone("Europe/Moscow")};
        PropertySpecBuilder timeZonePropertySpecBuilder =
                PropertySpecBuilderImpl
                        .forClass(new TimeZoneFactory())
                        .name(name)
                        .description(description)
                        .setDefaultValue(defaultValue)
                        .markExhaustive()
                        .addValues(possibleValues);
        if (required) {
            timeZonePropertySpecBuilder.markRequired();
        }
        return timeZonePropertySpecBuilder.finish();
    }

    @Override
    public PropertySpec relativePeriodPropertySpec(String name, boolean required, RelativePeriod defaultRelativePeriod) {
        PropertySpecBuilder builder = PropertySpecBuilderImpl.forClass(new RelativePeriodFactory(timeService));
        if (required) {
            builder.markRequired();
        }
        return builder.name(name).setDefaultValue(defaultRelativePeriod).finish();
    }

    @Override
    public PropertySpec longPropertySpec(String name, boolean required, Long defaultValue) {
        return basicPropertySpecService.longPropertySpec(name, required, defaultValue);
    }

    @Override
    public PropertySpec longPropertySpecWithValues(String name, boolean required, Long... values) {
        return basicPropertySpecService.longPropertySpecWithValues(name, required, values);
    }

    @Override
    public PropertySpec positiveLongPropertySpec(String name, boolean required) {
        return basicPropertySpecService.positiveLongPropertySpec(name, required);
    }

    @Override
    public PropertySpec boundedLongPropertySpec(String name, boolean required, Long lowerLimit, Long upperLimit) {
        return basicPropertySpecService.boundedLongPropertySpec(name, required, lowerLimit, upperLimit);
    }

    @Override
    public <T extends HasIdAndName> PropertySpec stringReferencePropertySpec(String name, boolean required, CanFindByStringKey<T> finder, T... values) {
        return basicPropertySpecService.stringReferencePropertySpec(name, required, finder, values);
    }
}