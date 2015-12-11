package com.energyict.mdc.dynamic.impl;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.BasicPropertySpec;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.properties.TimeZoneFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.TimeService;
import com.energyict.mdc.common.HexString;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.HexStringFactory;
import com.energyict.mdc.dynamic.ObisCodeValueFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.TimeDurationValueFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.TimeZone;

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
        return new BasicPropertySpec(name, description, required, getValueFactory(valueFactoryClass));
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

    @Reference
    public void setBasicPropertySpecService(com.elster.jupiter.properties.PropertySpecService propertySpecService) {
        this.basicPropertySpecService = propertySpecService;
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

}