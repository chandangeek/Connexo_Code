package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;

import java.util.List;

/**
 * Created by bvn on 10/28/15.
 */
public class DeviceMessageSpecificationInfo extends LinkInfo<Long> {
    public String name;
    public String deviceMessageId;
    public List<PropertyInfo> propertySpecs;
    public LinkInfo category;
}
