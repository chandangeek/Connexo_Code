package com.elster.jupiter.properties;

import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public abstract class HasIdAndName implements HasName {
    
    public abstract Object getId();
    
    public abstract String getName();

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof HasIdAndName)) {
            return false;
        }
        HasIdAndName o = (HasIdAndName) obj;
        return getId() != null ? getId().equals(o.getId()) : o.getId() == null;
    }
    
    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
