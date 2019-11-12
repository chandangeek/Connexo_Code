/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.configproperties;

import com.elster.jupiter.metering.configproperties.ConfigPropertiesProvider;
import com.elster.jupiter.metering.configproperties.ConfigProperty;
import com.elster.jupiter.metering.configproperties.PropertiesInfo;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.Checks;

import javax.inject.Inject;
import java.util.List;

public class ConfigPropertyImpl implements ConfigProperty, PersistenceAware {

    private final DataModel dataModel;

    String name;
    private String stringValue;
    ConfigPropertiesProvider provider;
    PropertySpec propertySpec;

    @Inject
    ConfigPropertyImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    ConfigPropertyImpl init(ConfigPropertiesProvider provider, PropertySpec propertySpec, Object value) {
        this.provider = provider;
        this.name = propertySpec.getName();
        this.propertySpec = propertySpec;
        setValue(value);
        return this;
    }

    public static ConfigPropertyImpl from(DataModel dataModel, ConfigPropertiesProvider provider, String name, Object value) {
        PropertySpec propertySpec = provider.getPropertyInfos().stream()
                .map(PropertiesInfo::getProperties)
                .flatMap(List::stream)
                .filter(info -> info.getName().equals(name))
                .findFirst()
                .get(); // some test is necessary here
        return dataModel.getInstance(ConfigPropertyImpl.class).init(provider, propertySpec, value);
    }

    @Override
    public Object getValue() {
        return propertySpec.getValueFactory().fromStringValue(stringValue);
    }

    @Override
    public String getStringValue() {
        return stringValue;
    }

    @Override
    public void setValue(Object value) {
        if (value != null && !(value instanceof String && Checks.is((String) value)
                .emptyOrOnlyWhiteSpace())) {
            this.stringValue = value.toString();
            return;
        }

        this.stringValue = toStringValue(value);
    }


    @SuppressWarnings("unchecked")
    private String toStringValue(Object object) {
        return propertySpec.getValueFactory().toStringValue(object);
    }

    @Override
    public void save() {
        dataModel.mapper(ConfigProperty.class).update(this);
    }

    @Override
    public String getName() {
        return name;
    }
}
