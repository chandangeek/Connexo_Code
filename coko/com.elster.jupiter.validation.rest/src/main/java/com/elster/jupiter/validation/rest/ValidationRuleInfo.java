/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.rest;

import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.validation.ValidationAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ValidationRuleInfo {

    public long id;
    public boolean active;
    public boolean deleted;
    public String implementation; //validator classname
    public String displayName; // readable name
    public String name;
    public int position;
    public ValidationAction action = ValidationAction.FAIL;
    public List<PropertyInfo> properties = new ArrayList<PropertyInfo>();
    public List<ReadingTypeInfo> readingTypes = new ArrayList<ReadingTypeInfo>();
    public ValidationRuleSetVersionInfo ruleSetVersion;
    public long version;
    public VersionInfo<Long> parent;
    public IdWithNameInfo application;

    public ValidationRuleInfo() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return id == ((ValidationRuleInfo) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
