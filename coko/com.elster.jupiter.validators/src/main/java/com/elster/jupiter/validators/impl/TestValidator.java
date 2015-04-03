package com.elster.jupiter.validators.impl;

import com.elster.jupiter.estimation.AdvanceReadingsSettingsFactory;
import com.elster.jupiter.estimation.NoneAdvanceReadingsSettings;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.time.AllRelativePeriod;
import com.elster.jupiter.validation.ValidationResult;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.elster.jupiter.validation.ValidationResult.*;

class TestValidator extends AbstractValidator {

    static final String RELATIVE_PERIOD = "relativePeriod";
    static final String ADVANCE_READINGS_SETTINGS = "advanceReadingsSettings";

    private final MeteringService meteringService;

    TestValidator(Thesaurus thesaurus, PropertySpecService propertySpecService, MeteringService meteringService) {
        super(thesaurus, propertySpecService);
        this.meteringService = meteringService;
    }

    TestValidator(Thesaurus thesaurus, PropertySpecService propertySpecService, MeteringService meteringService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, properties);
        this.meteringService = meteringService;
    }


    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(getPropertySpecService().relativePeriodPropertySpec(RELATIVE_PERIOD, true, new AllRelativePeriod()));

        PropertySpecBuilder propertySpecBuilder = getPropertySpecService().newPropertySpecBuilder(new AdvanceReadingsSettingsFactory(meteringService));
        propertySpecBuilder.markRequired();
        PropertySpec spec =
                propertySpecBuilder.name(ADVANCE_READINGS_SETTINGS).setDefaultValue(new NoneAdvanceReadingsSettings()).finish();
        builder.add(spec);

        return builder.build();
    }

    @Override
    public void init(Channel channel, ReadingType readingType, Range<Instant> interval) {
    }

    @Override
    public ValidationResult validate(IntervalReadingRecord intervalReadingRecord) {
        return VALID;
    }

    @Override
    public String getDefaultFormat() {
        return "Test validator";
    }



    @Override
    public List<String> getRequiredProperties() {
        return Arrays.asList(RELATIVE_PERIOD, ADVANCE_READINGS_SETTINGS);
    }

    @Override
    public String getPropertyDefaultFormat(String property) {
        switch (property) {
            case RELATIVE_PERIOD:
                return "Relative period";
            default:
                return null;
        }
    }

    @Override
    public ValidationResult validate(ReadingRecord readingRecord) {
        return VALID;
    }

}
