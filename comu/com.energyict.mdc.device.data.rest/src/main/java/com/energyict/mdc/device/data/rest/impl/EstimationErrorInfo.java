package com.energyict.mdc.device.data.rest.impl;

import java.time.Instant;
import java.util.List;

public class EstimationErrorInfo {
    public boolean success = false;
    public List<Instant> readings;

    public EstimationErrorInfo() {

    }

    public EstimationErrorInfo(List<Instant> readings) {
        this.readings = readings;
    }

    public static EstimationErrorInfo from(EstimationErrorException exception) {
        return new EstimationErrorInfo(exception.getReadings());
    }
}
