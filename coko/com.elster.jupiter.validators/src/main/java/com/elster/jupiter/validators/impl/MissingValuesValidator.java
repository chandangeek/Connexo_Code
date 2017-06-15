/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.validation.ValidationResult;

import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This Validator will interpret Intervals as being closed. i.e. start and end time are included in the interval.
 * So when validating missing readings for a five minute interval over a period of five minutes will expect 2 readings.
 */
class MissingValuesValidator extends AbstractValidator {

    private Set<Instant> instants;
    private ReadingType readingType;

    MissingValuesValidator(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public void init(Channel channel, ReadingType readingType, Range<Instant> interval) {
        this.readingType = readingType;
        Range<Instant> channelContainerRange = channel.getChannelsContainer().getRange();
        if (channelContainerRange.isConnected(interval)) {
            Range<Instant> intersectionRange = channelContainerRange.intersection(interval);
            this.instants = channel.toList(intersectionRange).stream()
                    .skip(skipFirstInstantIfDeltaChannel(channel, readingType) && BoundType.CLOSED.equals(intersectionRange.lowerBoundType()) ? 1 : 0)
                    .collect(Collectors.toSet());
        } else {
            instants = new HashSet<>();
        }
    }

    private boolean skipFirstInstantIfDeltaChannel(Channel channel, ReadingType readingType) {
        Optional<? extends ReadingType> bulkQuantityReadingType = channel.getBulkQuantityReadingType();
        return bulkQuantityReadingType.isPresent() && readingType.isBulkQuantityReadingType(bulkQuantityReadingType.get());
    }

    @Override
    public ValidationResult validate(IntervalReadingRecord intervalReadingRecord) {
        if (intervalReadingRecord.getQuantity(readingType) != null) {
            instants.remove(intervalReadingRecord.getTimeStamp());
        }
        return ValidationResult.VALID;
    }

    @Override
    public ValidationResult validate(ReadingRecord readingRecord) {
        // this type of validation can only verify missings on intervalreadings
        return ValidationResult.VALID;
    }

    @Override
    public String getDefaultFormat() {
        return "Check missing values  [STD]";
    }

    @Override
    public Optional<QualityCodeIndex> getReadingQualityCodeIndex() {
        return Optional.of(QualityCodeIndex.KNOWNMISSINGREAD);
    }

    @Override
    public Map<Instant, ValidationResult> finish() {
        return instants.stream()
                .collect(Collectors.toMap(Function.identity(),
                        instant -> ValidationResult.SUSPECT));
    }

    @Override
    public Set<QualityCodeSystem> getSupportedQualityCodeSystems() {
        return ImmutableSet.of(QualityCodeSystem.MDC, QualityCodeSystem.MDM);
    }

    @Override
    public List<TranslationKey> getExtraTranslationKeys() {
        return Collections.emptyList();
    }
}
