/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl.configProperties;

import com.elster.jupiter.metering.configproperties.ConfigPropertiesProvider;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigPropertiesInfoFactory {

    private final PropertyValueInfoService propertyValueInfoService;
    public List<ConfigPropertiesPropertiesInfo> properties;

    @Inject
    public ConfigPropertiesInfoFactory(PropertyValueInfoService propertyValueInfoService){
        this.propertyValueInfoService = propertyValueInfoService;
    }

    ConfigPropertiesInfo from(ConfigPropertiesProvider configPropertiesProvider){
        ConfigPropertiesInfo info = new ConfigPropertiesInfo();

        info.type = configPropertiesProvider.getScope();
        info.properties = configPropertiesProvider.getPropertyInfos().stream()
                .map(propertiesInfo -> {
                    return new ConfigPropertiesPropertiesInfo(propertiesInfo.getName(),
                            propertiesInfo.getDisplayName(),
                            propertyValueInfoService.getPropertyInfos(propertiesInfo.getProperties(), configPropertiesProvider.getPropertyValues()));

                })
                .collect(Collectors.toList());
        return info;
    }
}
