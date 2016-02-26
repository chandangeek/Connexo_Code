package com.elster.jupiter.mdm.common.rest;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

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
