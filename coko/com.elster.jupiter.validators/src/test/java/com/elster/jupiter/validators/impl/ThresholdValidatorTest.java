package com.elster.jupiter.validators.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validators.MessageSeeds;
import com.elster.jupiter.validators.MissingRequiredProperty;
import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ThresholdValidatorTest {

    public static final Quantity MINIMUM = Quantity.create(BigDecimal.valueOf(1000L), 1, "Wh");
    public static final Quantity MAXIMUM = Quantity.create(BigDecimal.valueOf(5000L), 1, "Wh");
    public static final Quantity BELOW_MINIMUM = Quantity.create(BigDecimal.valueOf(0L), 1, "Wh");
    public static final Quantity ABOVE_MAXIMUM = Quantity.create(BigDecimal.valueOf(6000L), 1, "Wh");
    public static final Quantity IN_THE_MIDDLE = Quantity.create(BigDecimal.valueOf(3000L), 1, "Wh");
    public static final String THRESHOLD_VIOLATION = "Threshold violation";
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private IntervalReadingRecord intervalReadingRecord;
    @Mock
    private ReadingRecord readingRecord;
    @Mock
    private Channel channel;
    @Mock
    private ReadingType readingType;

    private ThresholdValidator thresholdValidator;

    @Before
    public void setUp() {
        ImmutableMap<String, Quantity> properties = ImmutableMap.of("minimum", MINIMUM, "maximum", MAXIMUM);
        thresholdValidator = new ThresholdValidator(thesaurus, properties);
        thresholdValidator.init(channel, readingType, new Interval(new Date(7000L), new Date(14000L)));
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testValidationOk() {
        when(intervalReadingRecord.getQuantity(readingType)).thenReturn(IN_THE_MIDDLE);

        ValidationResult validationResult = thresholdValidator.validate(intervalReadingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.PASS);
    }

    @Test
    public void testValidationOnMissingDataYieldsSkipped() {
        when(intervalReadingRecord.getQuantity(readingType)).thenReturn(null);

        ValidationResult validationResult = thresholdValidator.validate(intervalReadingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.SKIPPED);
    }

    @Test
    public void testValidationOnMinimumPasses() {
        when(intervalReadingRecord.getQuantity(readingType)).thenReturn(MINIMUM);

        ValidationResult validationResult = thresholdValidator.validate(intervalReadingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.PASS);
    }

    @Test
    public void testValidationOnMaximumPasses() {
        when(intervalReadingRecord.getQuantity(readingType)).thenReturn(MAXIMUM);

        ValidationResult validationResult = thresholdValidator.validate(intervalReadingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.PASS);
    }

    @Test
    public void testValidationBelowMinimumIsSuspect() {
        when(intervalReadingRecord.getQuantity(readingType)).thenReturn(BELOW_MINIMUM);

        ValidationResult validationResult = thresholdValidator.validate(intervalReadingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.SUSPECT);
    }

    @Test
    public void testValidationAboveMaximumIsSuspect() {
        when(intervalReadingRecord.getQuantity(readingType)).thenReturn(ABOVE_MAXIMUM);

        ValidationResult validationResult = thresholdValidator.validate(intervalReadingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.SUSPECT);
    }

    @Test(expected = MissingRequiredProperty.class)
    public void testConstructionWithoutRequiredProperty() {
        ImmutableMap<String, Quantity> properties = ImmutableMap.of("minimum", MINIMUM);
        thresholdValidator = new ThresholdValidator(thesaurus, properties);
    }

    @Test
    public void testValidationOkForReadingRecord() {
        when(readingRecord.getQuantity(readingType)).thenReturn(IN_THE_MIDDLE);

        ValidationResult validationResult = thresholdValidator.validate(readingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.PASS);
    }

    @Test
    public void testValidationOnMissingDataYieldsSkippedForReadingRecord() {
        when(readingRecord.getQuantity(readingType)).thenReturn(null);

        ValidationResult validationResult = thresholdValidator.validate(readingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.SKIPPED);
    }

    @Test
    public void testValidationOnMinimumPassesForReadingRecord() {
        when(readingRecord.getQuantity(readingType)).thenReturn(MINIMUM);

        ValidationResult validationResult = thresholdValidator.validate(readingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.PASS);
    }

    @Test
    public void testValidationOnMaximumPassesForReadingRecord() {
        when(readingRecord.getQuantity(readingType)).thenReturn(MAXIMUM);

        ValidationResult validationResult = thresholdValidator.validate(readingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.PASS);
    }

    @Test
    public void testValidationBelowMinimumIsSuspectForReadingRecord() {
        when(readingRecord.getQuantity(readingType)).thenReturn(BELOW_MINIMUM);

        ValidationResult validationResult = thresholdValidator.validate(readingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.SUSPECT);
    }

    @Test
    public void testValidationAboveMaximumIsSuspectForReadingRecord() {
        when(readingRecord.getQuantity(readingType)).thenReturn(ABOVE_MAXIMUM);

        ValidationResult validationResult = thresholdValidator.validate(readingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.SUSPECT);
    }

    @Test
    public void testGetDefaultFormat() {
        assertThat(thresholdValidator.getDefaultFormat()).isEqualTo(THRESHOLD_VIOLATION);
    }

    @Test
    public void testNlsKey() {
        assertThat(thresholdValidator.getNlsKey()).isEqualTo(SimpleNlsKey.key(MessageSeeds.COMPONENT_NAME, Layer.DOMAIN, ThresholdValidator.class.getName()));
    }

    @Test
    public void testPropertyNlsKey() {
        assertThat(thresholdValidator.getPropertyNlsKey("minimum")).isEqualTo(SimpleNlsKey.key(MessageSeeds.COMPONENT_NAME, Layer.DOMAIN, ThresholdValidator.class.getName() + '.' + "minimum"));
    }

    @Test
    public void testPropertyNlsKeyForNonExistingProperty() {
        assertThat(thresholdValidator.getPropertyNlsKey("notAProperty")).isNull();
    }

    @Test
    public void testDisplayName() {
        when(thesaurus.getString(ThresholdValidator.class.getName(), THRESHOLD_VIOLATION)).thenReturn(THRESHOLD_VIOLATION + " en français");

        assertThat(thresholdValidator.getDisplayName()).isEqualTo(THRESHOLD_VIOLATION + " en français");
    }

    @Test
    public void testPropertyDisplayName() {
        when(thesaurus.getString(ThresholdValidator.class.getName() + '.' + "minimum", "Minimum")).thenReturn("Au moins");

        assertThat(thresholdValidator.getDisplayName("minimum")).isEqualTo("Au moins");
    }

    @Test
    public void testGetPropertyDefaultFormat() {
        assertThat(thresholdValidator.getPropertyDefaultFormat("minimum")).isEqualTo("Minimum");
        assertThat(thresholdValidator.getPropertyDefaultFormat("maximum")).isEqualTo("Maximum");
        assertThat(thresholdValidator.getPropertyDefaultFormat("notAProperty")).isNull();
    }

    @Test
    public void testFinish() {
        assertThat(thresholdValidator.finish()).isEmpty();
    }

    @Test
    public void testGetReadingQualityTypeCode() {
        assertThat(thresholdValidator.getReadingQualityTypeCode()).isAbsent();
    }

}