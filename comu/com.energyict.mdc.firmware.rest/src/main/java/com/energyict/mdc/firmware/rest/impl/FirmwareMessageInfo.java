package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FirmwareMessageInfo {
    public String uploadOption;
    public String localizedValue;
    public List<PropertyInfo> properties;
    public Long releaseDate;

    public FirmwareMessageInfo() {}
}
