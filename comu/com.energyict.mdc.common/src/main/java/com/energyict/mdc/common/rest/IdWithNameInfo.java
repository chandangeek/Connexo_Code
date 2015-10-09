package com.energyict.mdc.common.rest;

import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.HasId;

/**
 * Created by bvn on 8/12/14.
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
