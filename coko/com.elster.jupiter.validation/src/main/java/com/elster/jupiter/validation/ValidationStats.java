package com.elster.jupiter.validation;

import java.util.Date;

public final class ValidationStats {

    private final int numberOfSuspects;
    private final Date lastChecked;

    public ValidationStats(Date lastChecked, int numberOfSuspects) {
        this.lastChecked = lastChecked;
        this.numberOfSuspects = numberOfSuspects;
    }

    public Date getLastChecked() {
        return lastChecked;
    }

    public int getNumberOfSuspects() {
        return numberOfSuspects;
    }

}
