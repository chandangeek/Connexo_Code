/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.HasId;

import java.util.Objects;

/**
 * Created by bvn on 8/12/14.
 */
public class IdWithNameInfo implements Comparable<IdWithNameInfo>{
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        IdWithNameInfo that = (IdWithNameInfo) obj;
        return this.id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public int compareTo(IdWithNameInfo other){
        if (other == null) {
            return 1;
        }
        return ((Number)this.id).intValue() - ((Number)other.id).intValue();
    }
}
