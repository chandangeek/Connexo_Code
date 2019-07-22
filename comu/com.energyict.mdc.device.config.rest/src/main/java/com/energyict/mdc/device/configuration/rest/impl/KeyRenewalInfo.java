/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KeyRenewalInfo {
    public long id;
    public long version;
    public Long wrapperAccessorId;
    public IdWithNameInfo keyRenewalCommandSpecification;
    public List<PropertyInfo> properties;
}
