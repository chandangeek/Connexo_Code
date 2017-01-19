package com.elster.jupiter.validators.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingQualityValueFactory;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ReadingQualityPropertyValue;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.validation.ValidationResult;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

class ReadingQualitiesValidator extends AbstractValidator {

    static final String READING_QUALITIES = "readingQualities";
    private static final Set<QualityCodeSystem> QUALITY_CODE_SYSTEMS = ImmutableSet.of(QualityCodeSystem.MDC, QualityCodeSystem.MDM);

    private List<ReadingQualityPropertyValue> selectedReadingQualities;

    ReadingQualitiesValidator(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    ReadingQualitiesValidator(Thesaurus thesaurus, PropertySpecService propertySpecService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, properties);
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
                    getThesaurus().getString(TranslationKeys.ALL_SYSTEMS.getKey(), TranslationKeys.ALL_SYSTEMS.getDefaultFormat()),
                    getThesaurus().getString(qualityCodeCategory.getTranslationKey().getKey(), qualityCodeCategory.getTranslationKey().getDefaultFormat()),
                    getThesaurus().getString(TranslationKeys.ALL_INDEXES.getKey(), TranslationKeys.ALL_INDEXES.getDefaultFormat())
            ));
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
    @SuppressWarnings("unchecked")
    private void initParameters(Map<String, Object> properties) {
        Object value = properties.get(READING_QUALITIES);
        if (value != null) {
            if (value instanceof Collection) {
                ((Collection<Object>) value).stream().forEach((e) -> addToSelectedReadingQualities(String.valueOf(e)));
            } else {
                addToSelectedReadingQualities(String.valueOf(value));
            }
        }
    }

    private void addToSelectedReadingQualities(String cimCode){
        getSelectedReadingQualities().add(new ReadingQualityPropertyValue(cimCode));
    }

    private List<ReadingQualityPropertyValue> getSelectedReadingQualities() {
        if (selectedReadingQualities == null) {
            selectedReadingQualities = new ArrayList<>();
        }
        return selectedReadingQualities;
    }

    @Override
    public ValidationResult validate(IntervalReadingRecord intervalReadingRecord) {
        return validate(intervalReadingRecord.getReadingQualities().stream().map(readingQualityRecord -> new ReadingQualityPropertyValue(readingQualityRecord.getType().getCode())));
     }

    @Override
    public ValidationResult validate(ReadingRecord readingRecord) {
        return validate (readingRecord.getReadingQualities().stream().map(readingQualityRecord -> new ReadingQualityPropertyValue(readingQualityRecord.getType().getCode())));
    }

    private ValidationResult validate(Stream<ReadingQualityPropertyValue> readingQualityPropertyValueStream){
        Optional<ReadingQualityPropertyValue> selected = readingQualityPropertyValueStream.filter((rq) -> selectedReadingQualities.contains(rq)).findFirst();
        return selected.isPresent() ? ValidationResult.SUSPECT : ValidationResult.VALID;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.singletonList(
                getPropertySpecService()
                        .specForValuesOf(new ReadingQualityValueFactory())
                        .named(READING_QUALITIES, TranslationKeys.READING_QUALITIES_VALIDATOR)
                        .fromThesaurus(this.getThesaurus())
                        .addValues(getPossibleReadingQualityPropertyValues())
                        .markMultiValued()
                        .markRequired()
                        .finish());
    }

    @Override
    public String getDefaultFormat() {
        return TranslationKeys.READING_QUALITIES_VALIDATOR.getDefaultFormat();
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
    public Set<QualityCodeSystem> getSupportedQualityCodeSystems() {
        return QUALITY_CODE_SYSTEMS;
    }
}