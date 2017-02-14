/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import com.elster.jupiter.validation.DataValidationStatus;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class MeterReadingValidationData {

    private Map<Instant, DataValidationStatus> validationStatuses = new HashMap<>();

    public MeterReadingValidationData(Map<Instant, DataValidationStatus> validationStatuses) {
        this.validationStatuses = validationStatuses;
    }

    public DataValidationStatus getValidationStatus(Instant readingTimestamp) {
        return validationStatuses.get(readingTimestamp);
    }
}
