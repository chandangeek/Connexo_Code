/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.util.HasId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ReadingQualityRecord extends com.elster.jupiter.metering.readings.ReadingQuality {

    Instant getTimestamp();

    Channel getChannel();

    CimChannel getCimChannel();

    ReadingType getReadingType();

    void setComment(String comment);

    Optional<BaseReadingRecord> getBaseReadingRecord();

    default List<HasId> getFailedValidationRules() {
        return null;
    }

    default void addFailedValidationRule(HasId validationRule) {
    }

    void update();

    Instant getReadingTimestamp();

    void delete();

    long getVersion();

    boolean isActual();

    default boolean hasEditCategory() {
        return getType().hasEditCategory();
    }

    default boolean hasValidationCategory() {
        return getType().hasValidationCategory();
    }

    default boolean isSuspect() {
        return getType().isSuspect();
    }

    default boolean isMissing() {
        return getType().isMissing();
    }

    default boolean isError() {
        return getType().isError();
    }

    void makePast();

    void makeActual();

    default boolean hasReasonabilityCategory() {
        return getType().hasReasonabilityCategory();
    }

    default boolean hasEstimatedCategory() {
        return getType().hasEstimatedCategory();
    }

    default boolean isConfirmed() {
        return getType().isConfirmed();
    }
}
