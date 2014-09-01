package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.util.HasName;
import com.energyict.mdc.common.HasId;

/**
 * Created by bvn on 8/12/14.
 */
class IdWithNameInfo {
    public Object id;
    public String name;

    IdWithNameInfo() {
    }

    <H extends HasId & HasName> IdWithNameInfo(H object) {
        this.id = object.getId();
        this.name = object.getName();
    }

    IdWithNameInfo(Object id, String name) {
        this.id = id;
        this.name = name;
    }
}
