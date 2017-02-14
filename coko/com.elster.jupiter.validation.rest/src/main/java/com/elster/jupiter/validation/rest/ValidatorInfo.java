/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.rest;

import com.elster.jupiter.properties.rest.PropertyInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class ValidatorInfo {

    public String implementation;
    public String displayName;
    public List<PropertyInfo> properties;

    public ValidatorInfo(String implementation, String displayName, List<PropertyInfo> properties) {
        this.implementation = implementation;
        this.displayName = displayName;
        this.properties = properties;
    }

    public ValidatorInfo() {
    }
}
