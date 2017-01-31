/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.rest;

import com.elster.jupiter.rest.util.IdWithNameInfo;

public class MicroActionAndCheckInfo {

    public String key;
    public String name;
    public String description;
    public IdWithNameInfo category;
    public Boolean isRequired;
    public Boolean checked;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
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
