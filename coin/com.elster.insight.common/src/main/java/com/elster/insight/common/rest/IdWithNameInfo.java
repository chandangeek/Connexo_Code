package com.elster.insight.common.rest;

import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.HasId;

/**
 */
public class IdWithNameInfo {
    public Object id;
    public String name;

    public IdWithNameInfo() {
    }

    public <H extends HasId & HasName> IdWithNameInfo(H object) {
        this.id = object.getId();
        this.name = object.getName();
    }
    
    public IdWithNameInfo(Object id, String name) {
        this.id = id;
        this.name = name;
    }
}
