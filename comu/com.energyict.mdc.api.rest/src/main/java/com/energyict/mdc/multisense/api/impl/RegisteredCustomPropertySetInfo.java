/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;

import java.util.List;

public class RegisteredCustomPropertySetInfo extends LinkInfo<Long> {

    public String name;

    public List<PropertyInfo> customAttributes;
}