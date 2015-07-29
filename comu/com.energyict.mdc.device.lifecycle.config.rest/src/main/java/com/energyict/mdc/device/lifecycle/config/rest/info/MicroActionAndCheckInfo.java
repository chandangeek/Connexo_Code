package com.energyict.mdc.device.lifecycle.config.rest.info;

import com.energyict.mdc.common.rest.IdWithNameInfo;

public class MicroActionAndCheckInfo {

    public String key;
    public String name;
    public String description;
    public IdWithNameInfo category;
    public MicroActionConflictGroupInfo conflictGroup;
    public Boolean isRequired;
    public Boolean checked;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()){
            return false;
        }
        MicroActionAndCheckInfo that = (MicroActionAndCheckInfo) o;
        return !(key != null ? !key.equals(that.key) : that.key != null);
    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }
}
