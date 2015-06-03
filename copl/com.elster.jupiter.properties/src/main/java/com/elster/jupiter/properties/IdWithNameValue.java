package com.elster.jupiter.properties;

import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public abstract class IdWithNameValue implements HasName {
    
    public static IdWithNameValue EMPTY = new IdWithNameValue() {
        
        @Override
        public Object getId() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }
    };
    
    public abstract Object getId();
    
    public abstract String getName();

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof IdWithNameValue)) {
            return false;
        }
        IdWithNameValue o = (IdWithNameValue) obj;
        return getId() != null ? getId().equals(o.getId()) : o.getId() == null;
    }
    
    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
