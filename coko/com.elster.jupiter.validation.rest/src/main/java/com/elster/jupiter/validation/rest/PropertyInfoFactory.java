package com.elster.jupiter.validation.rest;

import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.rest.RelativePeriodInfo;

public class PropertyInfoFactory {

    public <T> Object asInfoObject(T property) {
        if (property instanceof RelativePeriod) {
            return new RelativePeriodInfo((RelativePeriod) property);
        }
        return property;
    }

    public <T> Object asInfoObjectForPredifinedValues(T property) {
        return property;
    }

}