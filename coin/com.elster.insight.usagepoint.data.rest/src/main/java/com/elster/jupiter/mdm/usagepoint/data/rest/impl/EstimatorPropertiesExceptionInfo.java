/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EstimatorPropertiesExceptionInfo {
    public boolean success = false;
    public List<PropertyErrorInfo> errors = new ArrayList<>();

    public EstimatorPropertiesExceptionInfo() {
    }

    public EstimatorPropertiesExceptionInfo(Map<String, String> errorProperties) {
        errorProperties.entrySet()
                .stream()
                .forEach(entry -> errors.add(new PropertyErrorInfo(entry.getKey(), entry.getValue())));
    }

    public static EstimatorPropertiesExceptionInfo from(EstimatorPropertiesException ex) {
        return new EstimatorPropertiesExceptionInfo(ex.getErrors());
    }
}

