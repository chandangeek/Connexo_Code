/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;


import com.elster.jupiter.properties.rest.PropertyInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class ProcessorInfo {

    public String name;
    public String displayName;
    public List<PropertyInfo> properties;

    public ProcessorInfo(String name, String displayName, List<PropertyInfo> properties) {
        this.name = name;
        this.displayName = displayName;
        this.properties = properties;
    }

    public ProcessorInfo() {
    }
}