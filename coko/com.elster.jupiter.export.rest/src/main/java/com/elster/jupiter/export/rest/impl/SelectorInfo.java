/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;


import com.elster.jupiter.properties.rest.PropertyInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class SelectorInfo {

    public String name;
    public String displayName;
    public List<PropertyInfo> properties = new ArrayList<>();
    public SelectorType selectorType;

    public SelectorInfo(String name, String displayName, List<PropertyInfo> properties, SelectorType selectorType) {
        this.name = name;
        this.displayName = displayName;
        this.properties = properties;
        this.selectorType = selectorType;
    }

    public SelectorInfo(String name, String displayName, SelectorType selectorType) {
        this.name = name;
        this.displayName = displayName;
        this.selectorType = selectorType;
    }

    public SelectorInfo() {
    }
}