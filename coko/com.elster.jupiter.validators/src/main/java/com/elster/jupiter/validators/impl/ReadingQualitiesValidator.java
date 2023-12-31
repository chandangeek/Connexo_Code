/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityValueFactory;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ReadingQualityPropertyValue;
import com.elster.jupiter.validation.ValidationResult;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ReadingQualitiesValidator extends AbstractValidator {

    static final String READING_QUALITIES = "readingQualities";

    private List<ReadingQualityPropertyValue> selectedReadingQualities;

    ReadingQualitiesValidator(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    ReadingQualitiesValidator(Thesaurus thesaurus, PropertySpecService propertySpecService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, properties);
        checkRequiredProperties();
    }

    /**
     * All possible reading qualities, with their translations
     */
    private List<ReadingQualityPropertyValue> getPossibleReadingQualityPropertyValues() {
        List<ReadingQualityPropertyValue> possibleReadingQualityPropertyValues = new ArrayList<>();

        //Add every possible combination
        for (QualityCodeSystem system : QualityCodeSystem.values()) {
            if (system != QualityCodeSystem.NOTAPPLICABLE) {
                for (QualityCodeIndex index : QualityCodeIndex.values()) {
                    String cimCode = system.ordinal() + "." + index.category().ordinal() + "." + index.index();
                    possibleReadingQualityPropertyValues.add(new ReadingQualityPropertyValue(
                            cimCode,
                            getThesaurus().getString(system.getTranslationKey().getKey(), system.getTranslationKey().getDefaultFormat()),
                            getThesaurus().getString(index.category().getTranslationKey().getKey(), index.category().getTranslationKey().getDefaultFormat()),
                            getThesaurus().getString(index.getTranslationKey().getKey(), index.getTranslationKey().getDefaultFormat())
                    ));
                }
            }
        }

        //Add the wildcard combinations
        for (QualityCodeCategory qualityCodeCategory : QualityCodeCategory.values()) {

            String cimCode = ReadingQualityPropertyValue.WILDCARD + "." + qualityCodeCategory.ordinal() + "." + ReadingQualityPropertyValue.WILDCARD;
            possibleReadingQualityPropertyValues.add(new ReadingQualityPropertyValue(
                    cimCode,
                    getThesaurus().getString(ReadingQualitiesTranslationKeys.ALL_SYSTEMS.getKey(), ReadingQualitiesTranslationKeys.ALL_SYSTEMS.getDefaultFormat()),
                    getThesaurus().getString(qualityCodeCategory.getTranslationKey().getKey(), qualityCodeCategory.getTranslationKey().getDefaultFormat()),
                    getThesaurus().getString(ReadingQualitiesTranslationKeys.ALL_INDEXES.getKey(), ReadingQualitiesTranslationKeys.ALL_INDEXES.getDefaultFormat())
            ));
        }
        return possibleReadingQualityPropertyValues;
    }

    @Override
    public void init(Channel channel, ReadingType readingType, Range<Instant> interval) {
        selectedReadingQualities = getSelectedReadingQualities(properties);
    }

    /**
     * The received properties contain the CIM code(s) of the selected reading qualities.
     */
    static List<ReadingQualityPropertyValue> getSelectedReadingQualities(Map<String, Object> properties) {
        return Optional.ofNullable(properties.get(READING_QUALITIES))
                .map(value -> value instanceof Collection<?> ? ((Collection<?>) value).stream() : Stream.of(value))
                .orElse(Stream.empty())
                .map(Object::toString)
                .map(ReadingQualityPropertyValue::new)
                .collect(Collectors.toList());
    }

    @Override
    public ValidationResult validate(IntervalReadingRecord intervalReadingRecord) {
        return validate(intervalReadingRecord.getReadingQualities());
    }

    @Override
    public ValidationResult validate(ReadingRecord readingRecord) {
        return validate(readingRecord.getReadingQualities());
    }

    private ValidationResult validate(Collection<? extends ReadingQualityRecord> readingQualities) {
        return readingQualities.stream()
                .map(ReadingQualityRecord::getTypeCode)
                .map(ReadingQualityPropertyValue::new)
                .filter(selectedReadingQualities::contains)
                .findFirst()
                .map(rq -> ValidationResult.SUSPECT)
                .orElse(ValidationResult.VALID);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.singletonList(
                getPropertySpecService()
                        .specForValuesOf(new ReadingQualityValueFactory())
                        .named(READING_QUALITIES, ReadingQualitiesTranslationKeys.READING_QUALITIES)
                        .fromThesaurus(this.getThesaurus())
                        .addValues(getPossibleReadingQualityPropertyValues())
                        .markMultiValued()
                        .markRequired()
                        .finish());
    }

    @Override
    public String getDefaultFormat() {
        return "Reading qualities [STD]";
    }

    @Override
    public Set<QualityCodeSystem> getSupportedQualityCodeSystems() {
        return ImmutableSet.of(QualityCodeSystem.MDC, QualityCodeSystem.MDM);
    }

    @Override
    public List<TranslationKey> getExtraTranslationKeys() {
        return Arrays.asList(ReadingQualitiesTranslationKeys.values());
    }
}
