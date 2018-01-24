/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.properties.HasDynamicProperties;
import com.elster.jupiter.properties.PropertySpec;

import aQute.bnd.annotation.ProviderType;

import java.util.ArrayList;
import java.util.List;

@ProviderType
public interface EndPointProp extends HasDynamicProperties {

    default List<PropertySpec> getPropertySpecs() {
        return new ArrayList<>();
    }

}
