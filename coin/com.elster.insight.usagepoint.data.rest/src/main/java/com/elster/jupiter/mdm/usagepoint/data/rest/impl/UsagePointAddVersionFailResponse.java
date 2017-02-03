/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cps.ValuesRangeConflict;
import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;

import java.util.List;
import java.util.stream.Collectors;

public class UsagePointAddVersionFailResponse {
    public boolean success = false;
    public List<IdWithDisplayValueInfo<String>> errors;

    public UsagePointAddVersionFailResponse(List<ValuesRangeConflict> conflictInfos) {
        this.success = conflictInfos.isEmpty();
        this.errors = conflictInfos.stream()
                .map(c -> new IdWithDisplayValueInfo<>(c.getType().name(), null))
                .collect(Collectors.toList());
    }
}
