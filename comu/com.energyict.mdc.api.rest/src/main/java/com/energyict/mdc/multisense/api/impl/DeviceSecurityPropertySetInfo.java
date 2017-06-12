/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;

import java.util.List;

public class DeviceSecurityPropertySetInfo extends LinkInfo<Long> {

    public LinkInfo configuredSecurityPropertySet;
    public PropertyInfo client;
    public List<LinkInfo> properties;
    public LinkInfo device;
}
