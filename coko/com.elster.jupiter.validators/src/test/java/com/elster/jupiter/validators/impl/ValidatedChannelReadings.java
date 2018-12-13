/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ValidatedChannelReadings extends ChannelReadings {

    List<DataValidationStatus> validationStatuses = new ArrayList<>();

    ValidatedChannelReadings(int readingsCount) {
        super(readingsCount);
        IntStream.rangeClosed(1,readingsCount).forEach(c -> validationStatuses.add(null));
    }

    void setReadingValue(int index, BigDecimal value, Instant readingTime) {
        setReadingValue(index, value, readingTime, ValidationResult.NOT_VALIDATED);
    }

    void setReadingValue(int index, BigDecimal value, Instant readingTime, ValidationResult validationResult) {
        super.setReadingValue(index, value, readingTime);
        DataValidationStatus dataValidationStatus = mock(DataValidationStatus.class);
        when(dataValidationStatus.getReadingTimestamp()).thenReturn(readingTime);
        when(dataValidationStatus.getValidationResult()).thenReturn(validationResult);
        validationStatuses.remove(index);
        validationStatuses.add(index, dataValidationStatus);
    }

    ValidationEvaluator mockEvaluator() {
        ValidationEvaluator evaluator = mock(ValidationEvaluator.class);
        when(evaluator.getValidationStatus(anyObject(),
                anyObject(),
                anyObject()))
                .thenReturn(validationStatuses.stream().filter(Objects::nonNull).collect(Collectors.toList()));
        return evaluator;
    }
}

