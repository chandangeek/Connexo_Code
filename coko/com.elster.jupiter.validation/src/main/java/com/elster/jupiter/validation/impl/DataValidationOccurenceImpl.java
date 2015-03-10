package com.elster.jupiter.validation.impl;

import com.elster.jupiter.validation.DataValidationOccurence;
import com.elster.jupiter.validation.DataValidationStatus;

import java.time.Instant;

/**
 * Created by albertv on 3/9/2015.
 */
public class DataValidationOccurenceImpl implements DataValidationOccurence {

    private Instant starDate;
    private Instant endDate;
    private DataValidationStatus status;
    private String message;

    public Instant getStarDate() {
        return starDate;
    }

    public void setStarDate(Instant starDate) {
        this.starDate = starDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public DataValidationStatus getStatus() {
        return status;
    }

    public void setStatus(DataValidationStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
