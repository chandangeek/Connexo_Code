package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.util.HasName;
import com.energyict.mdc.common.HasId;

public class IdWithNameInfo {
    public long id;
    public String name;

    public IdWithNameInfo(){}

    public<T extends HasId & HasName> IdWithNameInfo(T source){
        this.id = source.getId();
        this.name = source.getName();
    }
}
