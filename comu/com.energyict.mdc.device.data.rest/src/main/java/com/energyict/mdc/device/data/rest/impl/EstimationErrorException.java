package com.energyict.mdc.device.data.rest.impl;


import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EstimationErrorException extends RuntimeException {

    private List<Instant> readings = new ArrayList<>();

    public EstimationErrorException(List<Instant> readings) {
        super();
        this.readings = readings;
    }

    public List<Instant> getReadings() {
        return Collections.unmodifiableList(this.readings);
    }

}
