package com.elster.jupiter.validation;

import java.time.Instant;

/**
 * Created by albertv on 3/9/2015.
 */
public interface DataValidationOccurence {

    public Instant getStarDate();
    public void setStarDate(Instant starDate);
    public Instant getEndDate();
    public void setEndDate(Instant endDate);
    public DataValidationStatus getStatus();
    public void setStatus(DataValidationStatus status);
    public String getMessage();
    public void setMessage(String message);

}
