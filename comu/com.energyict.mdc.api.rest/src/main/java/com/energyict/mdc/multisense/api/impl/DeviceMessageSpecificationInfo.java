package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.properties.PropertyInfo;

import java.util.List;

/**
 * Created by bvn on 10/28/15.
 */
public class DeviceMessageSpecificationInfo extends LinkInfo {
    public String name;
    public String deviceMessageId;
    public List<PropertyInfo> propertySpecs;
    public LinkInfo category;
}
