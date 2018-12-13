/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;

import java.util.List;

public class CustomPropertySetInfo extends LinkInfo<Long> {

    public String name;
    public String domainClass;
    public Boolean isRequired;
    public Boolean isVersioned;
    public Boolean isEditable;
    public List<CustomPropertySetAttributeInfo> properties;
}
