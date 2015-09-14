package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EstimatorPropertiesException extends RuntimeException {
    private Map<String, String> errors = new HashMap<>();

    public EstimatorPropertiesException(Map<String, String> errors) {
        super();
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return Collections.unmodifiableMap(this.errors);
    }
}
