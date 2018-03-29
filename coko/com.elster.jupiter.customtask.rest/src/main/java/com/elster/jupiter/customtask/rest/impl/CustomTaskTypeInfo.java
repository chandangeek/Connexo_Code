/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask.rest.impl;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.stream.Collectors;

@XmlRootElement
public class CustomTaskTypeInfo {

    public String name;
    public String displayName;
    public List<String> actions;
    public List<CustomTaskPropertiesInfo> properties;

    public CustomTaskTypeInfo() {
    }

    public CustomTaskTypeInfo(String name, String displayName, List<CustomTaskPropertiesInfo> properties, List<String> actions) {
        this.name = name;
        this.displayName = displayName;
        this.properties = properties;
        this.actions = actions.stream().map(s -> s.toString()).collect(Collectors.toList());
    }
}
