/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import java.util.List;
import java.util.stream.Collectors;

public class CustomPropertySetIntervalConflictErrorInfo {
    public boolean success = false;
    public List<ValueRangeConflictErrorInfo> errors;

    public CustomPropertySetIntervalConflictErrorInfo() {
    }

    public CustomPropertySetIntervalConflictErrorInfo(List<CustomPropertySetIntervalConflictInfo> conflictInfos) {
        this.success = conflictInfos.isEmpty();
        this.errors = conflictInfos.stream().map(c -> new ValueRangeConflictErrorInfo(c.conflictType, c.message)).collect(Collectors.toList());
    }

    public class ValueRangeConflictErrorInfo {
        public String id;
        public String msg;

        public ValueRangeConflictErrorInfo(String id, String msg) {
            this.id = id;
            this.msg = msg;
        }
    }
}
