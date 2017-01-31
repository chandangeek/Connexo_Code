/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.fasterxml.jackson.databind.JsonNode;

public class ValidationInfoParser {

    public ValidationLoadProfilePeriodInfo parseFromNode(JsonNode jsonNode) {
        ValidationLoadProfilePeriodInfo info = new ValidationLoadProfilePeriodInfo();
        convertJsonNodeToInfo(info, jsonNode);
        return info;
    }

    private void convertJsonNodeToInfo(ValidationLoadProfilePeriodInfo info, JsonNode singleFilter) {
        JsonNode property = singleFilter.get("id");
        if (property != null && property.numberValue() != null) {
            info.id = property.numberValue().longValue();
        }
        property = singleFilter.get("intervalStart");
        if (property != null && property.numberValue() != null) {
            info.startInterval = property.numberValue().longValue();
        }
        property = singleFilter.get("intervalEnd");
        if (property != null && property.numberValue() != null) {
            info.endInterval = property.numberValue().longValue();
        }
    }
}
