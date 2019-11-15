/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.configproperties;

import com.elster.jupiter.metering.impl.configproperties.ConfigPropertyImpl;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractConfigPropertiesProvider implements ConfigPropertiesProvider {

    protected volatile DataModel dataModel;
    //private List<ConfigProperty> properties = new ArrayList<>();
    private Map<String, Object> propertyValues = new HashMap<>();

    @Override
    public void update(){
        List<ConfigProperty> properties = getProperties();
        propertyValues.entrySet().stream()
                .forEach(entry -> {
                    properties.stream()
                            .filter(p -> p.getName().equals(entry.getKey()))
                            .findAny()
                            .map(p -> {
                                p.setValue(entry.getValue());
                                p.save();
                                return p;
                            })
                            .orElseGet(() -> {
                                ConfigProperty property = ConfigPropertyImpl.from(dataModel, this, entry.getKey(), entry.getValue());
                                property.save();
                                return property;
                            });
                });
    }

    @Override
    public void setProperty(String key, Object value){
        propertyValues.put(key, value);
        /*ConfigProperty configProperty = getProperties().stream()
                .filter(p -> p.getName().equals(key))
                .findFirst()
                .orElseGet(() -> {
                    ConfigProperty property = ConfigPropertyImpl.from(dataModel, this, key, value);
                    properties.add(property);
                    return property;
                });
        configProperty.setValue(value);*/
    }

    @Override
    public Map<String, Object> getPropertyValues() {
        return getProperties().stream()
                .map(p -> ConfigPropertyImpl.from(dataModel, this, p.getName(), p.getStringValue()))
                .collect(Collectors.toMap(ConfigProperty::getName, ConfigProperty::getValue));
    }

    @Override
    public Map<String, String> getPropertyStringValues() {
        return getProperties().stream()
                .map(p -> ConfigPropertyImpl.from(dataModel, this, p.getName(), p.getStringValue()))
                .collect(Collectors.toMap(ConfigProperty::getName, ConfigProperty::getStringValue));
    }

    private List<ConfigProperty> getProperties() {
        Condition condition = Where.where("scope").isEqualTo(getScope());
        return dataModel.mapper(ConfigProperty.class).select(condition);
    }
    /*private List<ConfigProperty> getProperties(){
        //if (properties.isEmpty()){
            Condition condition = Where.where("scope").isEqualTo(getScope());
            properties = dataModel.mapper(ConfigProperty.class).select(condition)
                    .stream()
                    .map(prop -> ConfigPropertyImpl.from(dataModel, this, prop.getName(), prop.getStringValue()))
                    .collect(Collectors.toList());
       // }
        return  properties;
    }*/
}
