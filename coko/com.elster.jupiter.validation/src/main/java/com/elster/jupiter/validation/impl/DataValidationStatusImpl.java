package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationRule;
import com.google.common.collect.ImmutableList;

import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 29/08/2014
 * Time: 17:12
 */
public class DataValidationStatusImpl implements DataValidationStatus {
    private final Date timeStamp;
    private final Map<ReadingQualityRecord, List<? extends ValidationRule>> qualityRecordList = new HashMap<>();
    private boolean completelyValidated;

    public DataValidationStatusImpl(Date timeStamp, boolean completelyValidated) {
        this.timeStamp = timeStamp;
        this.completelyValidated = completelyValidated;
    }

    @Override
    public Date getReadingTimestamp() {
        return timeStamp;
    }

    @Override
    public Collection<ReadingQualityRecord> getReadingQualities() {
        return Collections.unmodifiableCollection(qualityRecordList.keySet());
    }

    @Override
    public Collection<ValidationRule> getOffendedValidationRule(ReadingQualityRecord readingQuality) {
        if (qualityRecordList.containsKey(readingQuality)) {
            return Collections.unmodifiableCollection(qualityRecordList.get(readingQuality));
        } else {
            return Collections.emptyList();
        }
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
    public boolean completelyValidated() {
        return completelyValidated;
    }

    public void addReadingQuality(ReadingQualityRecord quality, List<IValidationRule> iValidationRules) {
        qualityRecordList.put(quality, iValidationRules);
    }
}
