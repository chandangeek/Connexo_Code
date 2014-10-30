package com.elster.jupiter.validators.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.validation.ValidationResult;
import com.google.common.collect.Range;

/**
 * This Validator will interpret Intervals as being closed. i.e. start and end time are included in the interval. So when validating missing readings for a five minute interval over a period of five minutes will expect 2 readings.
 * <p/>
 */
class MissingValuesValidator extends AbstractValidator {

    private static final String READING_QUALITY_TYPE_CODE = "3.5.259";
    
    private Set<Instant> instants;

    MissingValuesValidator(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    @Override
    public NlsKey getPropertyNlsKey(String property) {
        // there are no properties
        return null;
    }

    @Override
    public String getPropertyDefaultFormat(String property) {
        // there are no properties
        return null;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public void init(Channel channel, ReadingType readingType, Range<Instant> interval) {
        instants = new HashSet<>(channel.toList(interval));
    }

    @Override
    public ValidationResult validate(IntervalReadingRecord intervalReadingRecord) {
        instants.remove(intervalReadingRecord.getTimeStamp());
        return ValidationResult.VALID;
    }

    @Override
    public ValidationResult validate(ReadingRecord readingRecord) {
        // this type of validation can only verify missings on intervalreadings
        return ValidationResult.VALID;
    }

    @Override
    public String getDefaultFormat() {
        return "Check missing values";
    }

    @Override
    public Optional<ReadingQualityType> getReadingQualityTypeCode() {
        return Optional.of(new ReadingQualityType(READING_QUALITY_TYPE_CODE));
    }

    @Override
    public Map<Instant, ValidationResult> finish() {
    	return instants.stream().collect(Collectors.toMap(Function.identity(), instant -> ValidationResult.SUSPECT));
    }
    
    @Override
    public List<String> getRequiredProperties() {
        return Collections.emptyList();
    }
}
