/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FirmwareMessageInfo {

    public String uploadOption;
    public String localizedValue;

    private List<PropertyInfoWithEditableOption> properties;
    public Instant releaseDate;
    public long version; // device version here!

    public FirmwareMessageInfo() {
    }

    @JsonGetter("properties")
    @SuppressWarnings("unused")
    public List<PropertyInfoWithEditableOption> getWrappedProperties() {
        return properties;
    }

    public List<PropertyInfo> getProperties() {
        return this.properties.stream().map(x -> x.propertyInfo).collect(Collectors.toList());
    }

    public void setProperties(List<PropertyInfo> properties) {
        this.properties = new ArrayList<>();
        this.properties.addAll(properties.stream().map(PropertyInfoWithEditableOption::new).collect(Collectors.toList()));
    }

    public Optional<PropertyInfo> getPropertyInfo(String key) {
        return this.properties.stream().map(x -> x.propertyInfo).filter(y -> y.key.equals(key)).findFirst();
    }

    public void setPropertyEditable(String key, boolean editable) {
        properties.stream().filter(x -> x.propertyInfo.key.equals(key)).findFirst().ifPresent(y -> y.canBeOverridden = editable);
    }

    public class PropertyInfoWithEditableOption {

        @JsonUnwrapped
        public PropertyInfo propertyInfo;
        public Boolean canBeOverridden;

        PropertyInfoWithEditableOption(PropertyInfo propertyInfo) {
            this(propertyInfo, true);
        }

        PropertyInfoWithEditableOption(PropertyInfo propertyInfo, boolean canBeOverridden) {
            this.propertyInfo = propertyInfo;
            this.canBeOverridden = canBeOverridden;
        }
    }
}
