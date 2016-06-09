package com.elster.jupiter.validators.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ReadingQualityPropertyValue;
import com.elster.jupiter.properties.ReadingQualityValueFactory;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.validation.ValidationResult;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.*;

class IntervalStateValidator extends AbstractValidator {

    static final String READING_QUALITIES = "readingQualities";
    private static final Set<String> SUPPORTED_APPLICATIONS = ImmutableSet.of("MDC", "INS");

    private Set<String> selectedReadingQualities;

    private List<ReadingQualityPropertyValue> possibleReadingQualityPropertyValues = null;

    IntervalStateValidator(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    IntervalStateValidator(Thesaurus thesaurus, PropertySpecService propertySpecService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, properties);
    }

    /**
     * All possible reading qualities
     */
    private List<ReadingQualityPropertyValue> getPossibleReadingQualityPropertyValues() {
        if (possibleReadingQualityPropertyValues == null) {
            possibleReadingQualityPropertyValues = new ArrayList<>();

            //Add every possible combination
            for (QualityCodeSystem system : QualityCodeSystem.values()) {

                for (QualityCodeIndex index : QualityCodeIndex.values()) {
                    String cimCode = system.ordinal() + "." + index.category().ordinal() + "." + index.index();
                    possibleReadingQualityPropertyValues.add(new ReadingQualityPropertyValue(
                            cimCode,
                            getThesaurus().getStringBeyondComponent(system.getTranslationKey().getKey(), system.getTranslationKey().getDefaultFormat()),
                            getThesaurus().getStringBeyondComponent(index.category().getTranslationKey().getKey(), index.category().getTranslationKey().getDefaultFormat()),
                            getThesaurus().getStringBeyondComponent(index.getTranslationKey().getKey(), index.getTranslationKey().getDefaultFormat())
                    ));
                }
            }

            //Add the wildcard combinations
            for (QualityCodeCategory qualityCodeCategory : QualityCodeCategory.values()) {

                String cimCode = "*." + qualityCodeCategory.ordinal() + ".*";
                possibleReadingQualityPropertyValues.add(new ReadingQualityPropertyValue(
                        cimCode,
                        getThesaurus().getStringBeyondComponent(com.elster.jupiter.cbo.TranslationKeys.ALL_SYSTEMS.getKey(), com.elster.jupiter.cbo.TranslationKeys.ALL_SYSTEMS.getDefaultFormat()),
                        getThesaurus().getStringBeyondComponent(qualityCodeCategory.getTranslationKey().getKey(), qualityCodeCategory.getTranslationKey().getDefaultFormat()),
                        getThesaurus().getStringBeyondComponent(com.elster.jupiter.cbo.TranslationKeys.ALL_INDEXES.getKey(), com.elster.jupiter.cbo.TranslationKeys.ALL_INDEXES.getDefaultFormat())
                ));
            }
        }
        return possibleReadingQualityPropertyValues;
    }

    @Override
    public void init(Channel channel, ReadingType readingType, Range<Instant> interval) {
        initParameters(properties);
    }

    /**
     * The received properties contain the CIM code(s) of the selected reading qualities.
     */
    private void initParameters(Map<String, Object> properties) {
        Object value = properties.get(READING_QUALITIES);
        if (value != null) {
            if (value instanceof Collection) {
                for (Object element : ((Collection) value)) {
                    getSelectedReadingQualities().add(String.valueOf(element));
                }
            } else {
                getSelectedReadingQualities().add(String.valueOf(value));
            }
        }
    }

    public Set<String> getSelectedReadingQualities() {
        if (selectedReadingQualities == null) {
            selectedReadingQualities = Collections.emptySet();
        }
        return selectedReadingQualities;
    }

    @Override
    public ValidationResult validate(IntervalReadingRecord intervalReadingRecord) {
        List<? extends ReadingQualityRecord> readingQualities = intervalReadingRecord.getReadingQualities();
        //TODO implement, also support wildcard
        return Collections.disjoint(getSelectedReadingQualities(), readingQualities) ? ValidationResult.VALID : ValidationResult.SUSPECT;
    }

    @Override
    public ValidationResult validate(ReadingRecord readingRecord) {
        List<? extends ReadingQualityRecord> readingQualities = readingRecord.getReadingQualities();
        //TODO implement, also support wildcard
        return Collections.disjoint(getSelectedReadingQualities(), readingQualities) ? ValidationResult.VALID : ValidationResult.SUSPECT;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.singletonList(
                getPropertySpecService()
                        .specForValuesOf(new ReadingQualityValueFactory())
                        .named(READING_QUALITIES, TranslationKeys.INTERVAL_STATE_VALIDATOR)
                        .fromThesaurus(this.getThesaurus())
                        .addValues(getPossibleReadingQualityPropertyValues())
                        .markMultiValued()
                        .finish());
    }

    @Override
    public String getDefaultFormat() {
        return TranslationKeys.INTERVAL_STATE_VALIDATOR.getDefaultFormat();
    }

    @Override
    public List<Pair<? extends NlsKey, String>> getExtraTranslations() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getRequiredProperties() {
        return Collections.singletonList(READING_QUALITIES);
    }

    @Override
    public Set<String> getSupportedApplications() {
        return SUPPORTED_APPLICATIONS;
    }
}