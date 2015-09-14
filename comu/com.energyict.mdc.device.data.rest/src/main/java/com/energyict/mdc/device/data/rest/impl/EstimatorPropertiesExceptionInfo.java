package com.energyict.mdc.device.data.rest.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EstimatorPropertiesExceptionInfo {
    public boolean success = false;
    public List<PropertyErrorInfo> errors = new ArrayList<>();

    public EstimatorPropertiesExceptionInfo() {}

    public EstimatorPropertiesExceptionInfo(Map<String, String> errorProperties) {
        errorProperties.entrySet().stream().forEach(entry -> errors.add(new PropertyErrorInfo(entry.getKey(), entry.getValue())));
    }



    public static EstimatorPropertiesExceptionInfo from(EstimatorPropertiesException ex) {
       return new EstimatorPropertiesExceptionInfo(ex.getErrors());
    }
}

class PropertyErrorInfo {
    public String id;
    public String msg;

    public PropertyErrorInfo() {}

    public PropertyErrorInfo(String id, String msg) {
        this.id = id;
        this.msg = msg;
    }
}
