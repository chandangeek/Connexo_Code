package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.util.hypermedia.LinkInfo;
import com.energyict.mdc.common.rest.IntervalInfo;

import java.util.List;

public class DeviceSecurityPropertySetInfo extends LinkInfo<Long> {

    public LinkInfo configuredSecurityPropertySet;
    public IntervalInfo effectivePeriod;
    public Boolean complete;
    public List<PropertyInfo> properties;
    public LinkInfo device;
}
