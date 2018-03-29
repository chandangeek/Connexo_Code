/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask.rest.impl;

import com.elster.jupiter.customtask.CustomTaskFactory;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@XmlRootElement
public class CustomTaskTypeInfos {

    public int total;
    public List<CustomTaskTypeInfo> propertiesInfo = new ArrayList<CustomTaskTypeInfo>();

    public CustomTaskTypeInfos() {
    }

    public CustomTaskTypeInfo add(String name, String displayName, List<CustomTaskPropertiesInfo> propertiesInfos, List<String> actions) {
        CustomTaskTypeInfo result = new CustomTaskTypeInfo(name, displayName, propertiesInfos, actions);
        propertiesInfo.add(result);
        total++;
        return result;
    }

    public CustomTaskTypeInfos from(PropertyValueInfoService propertyValueInfoService, List<CustomTaskFactory> customTaskFactories, List<String> actions) {

        customTaskFactories.stream()
                .forEach(customTaskFactory -> {

                    List<CustomTaskPropertiesInfo> customTaskPropertiesInfos = customTaskFactory.getProperties().stream().map(propertiesInfo1 -> {
                        return new CustomTaskPropertiesInfo(propertiesInfo1.getName(),
                                propertiesInfo1.getDisplayName(),
                                propertyValueInfoService.getPropertyInfos(propertiesInfo1.getProperties()));
                    }).collect(Collectors.toList());


                    this.add(customTaskFactory.getName(),
                            customTaskFactory.getDisplayName(),
                            customTaskPropertiesInfos,
                            actions);
                });
        return this;

    }
}
