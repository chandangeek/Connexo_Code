package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRule;
import com.google.common.collect.ImmutableList;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataValidationStatusImpl implements DataValidationStatus {
    private final Instant timeStamp;
    private final Map<ReadingQuality, List<? extends ValidationRule>> qualityRecordList = new HashMap<>();
    private final Map<ReadingQuality, List<? extends ValidationRule>> bulkQualityRecordList = new HashMap<>();
    private boolean completelyValidated;

    public DataValidationStatusImpl(Instant timeStamp, boolean completelyValidated) {
        this.timeStamp = timeStamp;
        this.completelyValidated = completelyValidated;
    }

    @Override
    public Instant getReadingTimestamp() {
        return timeStamp;
    }

    @Override
    public Collection<? extends ReadingQuality> getReadingQualities() {
        return Collections.unmodifiableCollection(qualityRecordList.keySet());
    }

    @Override
    public Collection<? extends ReadingQuality> getBulkReadingQualities() {
        return Collections.unmodifiableCollection(bulkQualityRecordList.keySet());
    }

    @Override
    public Collection<ValidationRule> getOffendedValidationRule(ReadingQuality readingQuality) {
        if (qualityRecordList.containsKey(readingQuality)) {
            return Collections.unmodifiableCollection(qualityRecordList.get(readingQuality));
        } else if (bulkQualityRecordList.containsKey(readingQuality)) {
            return Collections.unmodifiableCollection(bulkQualityRecordList.get(readingQuality));
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<ValidationRule> getOffendedRules() {
        ImmutableList.Builder<ValidationRule> allOffended = ImmutableList.builder();
        for (List<? extends ValidationRule> validationRules : qualityRecordList.values()) {
            allOffended.addAll(validationRules);
        }
        return allOffended.build();
    }

    @Override
    public Collection<ValidationRule> getBulkOffendedRules() {
        ImmutableList.Builder<ValidationRule> allOffended = ImmutableList.builder();
        bulkQualityRecordList.values().stream().forEach(allOffended::addAll);
        return allOffended.build();
    }

    @Override
    public boolean completelyValidated() {
        return completelyValidated;
    }

    public void addReadingQuality(ReadingQuality quality, List<IValidationRule> iValidationRules) {
        qualityRecordList.put(quality, iValidationRules);
    }

    public void addBulkReadingQuality(ReadingQuality quality, List<IValidationRule> iValidationRules) {
        bulkQualityRecordList.put(quality, iValidationRules);
    }
    
    @Override
    public ValidationResult getValidationResult() {
    	return ValidationResult.getValidationResult(getReadingQualities());
    }

    @Override
    public ValidationResult getBulkValidationResult() {
        return ValidationResult.getValidationResult(getBulkReadingQualities());
    }
}
