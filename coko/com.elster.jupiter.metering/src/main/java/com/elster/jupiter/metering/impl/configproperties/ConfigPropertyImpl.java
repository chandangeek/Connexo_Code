/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.configproperties;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.configproperties.ConfigPropertiesProvider;
import com.elster.jupiter.metering.configproperties.ConfigProperty;
import com.elster.jupiter.metering.configproperties.PropertiesInfo;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.properties.PropertySpec;

import javax.inject.Inject;
import java.time.Instant;
import java.util.List;

public class ConfigPropertyImpl implements ConfigProperty, PersistenceAware {

    //managed by orm
    private long id;
    private String name;
    private String scope;
    private String stringValue;
    private ConfigPropertiesProvider provider;
    private PropertySpec propertySpec;
    private final DataModel dataModel;

    // Audit fields
    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;

    @Inject
    public ConfigPropertyImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public ConfigPropertyImpl init(ConfigPropertiesProvider provider, PropertySpec propertySpec, Object value) {
        this.provider = provider;
        this.name = propertySpec.getName();
        this.propertySpec = propertySpec;
        this.scope = provider.getScope();
        this.stringValue = value.toString();
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

    public ConfigPropertyImpl setPropertySpec(PropertySpec propertySpec){
        this.propertySpec = propertySpec;
        return this;
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
        this.stringValue = propertySpec.getValueFactory().toStringValue(value);
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }


    @SuppressWarnings("unchecked")
    private String toStringValue(Object object) {
        return propertySpec.getValueFactory().toStringValue(object);
    }

    @Override
    public void save() {
        if (id == 0) {
            Save.CREATE.save(dataModel, this);
        } else {
            Save.UPDATE.save(dataModel, this);
        }
    }

    @Override
    public String getName() {
        return name;
    }
}
