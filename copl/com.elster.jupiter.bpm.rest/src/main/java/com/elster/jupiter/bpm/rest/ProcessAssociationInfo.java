/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest;

import com.elster.jupiter.properties.rest.PropertyInfo;

import java.util.Collections;
import java.util.List;

/**
 * Created by dragos on 2/26/2016.
 */
public class ProcessAssociationInfo {
    public String name;
    public String type;
    public List<PropertyInfo> properties = Collections.emptyList();

    public ProcessAssociationInfo() {

    }

    public ProcessAssociationInfo(String name, String type, List<PropertyInfo> properties) {
        this.name = name;
        this.type = type;
        this.properties = properties;
    }
}
