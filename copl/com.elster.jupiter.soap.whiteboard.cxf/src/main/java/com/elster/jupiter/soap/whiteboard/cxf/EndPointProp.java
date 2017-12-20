package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.properties.HasDynamicProperties;
import com.elster.jupiter.properties.PropertySpec;

import java.util.ArrayList;
import java.util.List;

public interface EndPointProp extends HasDynamicProperties {

    default List<PropertySpec> getPropertySpecs() {
        return new ArrayList<>();
    }

}
