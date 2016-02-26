package com.elster.jupiter.bpm.rest;

import com.elster.jupiter.rest.util.properties.PropertyInfo;

import java.util.Collections;
import java.util.List;

/**
 * Created by dragos on 2/26/2016.
 */
public class ProcessAssociationInfo {
    public String name;
    public List<PropertyInfo> properties = Collections.emptyList();

    public ProcessAssociationInfo() {

    }

    public ProcessAssociationInfo(String name, List<PropertyInfo> properties) {
        this.name = name;
        this.properties = properties;
    }
}
