/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.configproperties;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.impl.configproperties.ConfigPropertyImpl;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;

import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractConfigPropertiesProvider implements ConfigPropertiesProvider {

    protected volatile DataModel dataModel;
    private List<ConfigProperty> properties = new ArrayList<>();

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.getDataModel(MeteringService.COMPONENTNAME).get();
    }

    @Override
    public void update(){
        properties.forEach(ConfigProperty::save);
    }

    @Override
    public void setProperty(String key, Object value){
        ConfigProperty configProperty = properties.stream()
                .filter(p -> p.getName().equals(key))
                .findFirst()
                .orElseGet(() -> {
                    ConfigPropertyImpl property = ConfigPropertyImpl.from(dataModel, this, key, value);
                    properties.add(property);
                    return property;
                });
        configProperty.setValue(value);
    }

    @Override
    public Map<String, Object> getPropertyValues() {
        return properties.stream()
                .collect(Collectors.toMap(ConfigProperty::getName, ConfigProperty::getValue));
    }

}
