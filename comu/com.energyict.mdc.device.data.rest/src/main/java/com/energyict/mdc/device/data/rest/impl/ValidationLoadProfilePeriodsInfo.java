/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.util.json.JsonDeserializeException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

/**
 * Created by adrianlupan on 5/11/15.
 */

public class ValidationLoadProfilePeriodsInfo {

    private List<ValidationLoadProfilePeriodInfo> loadProfilePeriodInfos;

    public ValidationLoadProfilePeriodsInfo() {}

    public static ValidationLoadProfilePeriodsInfo fromString(final String bean) {
        TypeReference<List<ValidationLoadProfilePeriodInfo>> lpList = new TypeReference<List<ValidationLoadProfilePeriodInfo>>(){};
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ValidationLoadProfilePeriodsInfo validationLoadProfilePeriodsInfo = new ValidationLoadProfilePeriodsInfo();
            validationLoadProfilePeriodsInfo.loadProfilePeriodInfos = objectMapper.readValue(bean, lpList);
            return validationLoadProfilePeriodsInfo;
        } catch (IOException e) {
            throw new JsonDeserializeException(e, bean, ValidationLoadProfilePeriodsInfo.class);
        }
    }

    public List<ValidationLoadProfilePeriodInfo> getLoadProfilePeriodInfos() {
        return loadProfilePeriodInfos;
    }

}
