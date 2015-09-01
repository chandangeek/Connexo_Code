package com.elster.jupiter.validators.impl;

import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validators.MissingRequiredProperty;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.Instant;

import static com.elster.jupiter.validators.impl.ThresholdValidator.MAX;
import static com.elster.jupiter.validators.impl.ThresholdValidator.MIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ThresholdValidatorTest {

    public static final BigDecimal MINIMUM = BigDecimal.valueOf(1000L);
    public static final BigDecimal MAXIMUM = BigDecimal.valueOf(5000L);
    public static final Quantity BELOW_MINIMUM = Quantity.create(BigDecimal.valueOf(0L), 3, "Wh");
    public static final Quantity ABOVE_MAXIMUM = Quantity.create(BigDecimal.valueOf(6000L), 3, "Wh");
    public static final Quantity IN_THE_MIDDLE = Quantity.create(BigDecimal.valueOf(3000L), 3, "Wh");
    public static final String THRESHOLD_VIOLATION = "Threshold violation";
    @Mock
    private Thesaurus thesaurus;

    private PropertySpecService propertySpecService = new PropertySpecServiceImpl();
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
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        ImmutableMap<String, Object> properties = ImmutableMap.of(MIN, (Object) MINIMUM, MAX, MAXIMUM);
        thresholdValidator = new ThresholdValidator(thesaurus, propertySpecService, properties);
        thresholdValidator.init(channel, readingType, Range.closed(Instant.ofEpochMilli(7000L), Instant.ofEpochMilli(14000L)));
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testValidationOk() {
        when(intervalReadingRecord.getQuantity(readingType)).thenReturn(IN_THE_MIDDLE);

        ValidationResult validationResult = thresholdValidator.validate(intervalReadingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.VALID);
    }

    @Test
    public void testValidationOnMissingDataYieldsSkipped() {
        when(intervalReadingRecord.getQuantity(readingType)).thenReturn(null);

        ValidationResult validationResult = thresholdValidator.validate(intervalReadingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.NOT_VALIDATED);
    }

    @Test
    public void testValidationOnMinimumPasses() {
        when(intervalReadingRecord.getQuantity(readingType)).thenReturn(Quantity.create(MINIMUM, 3, "Wh"));

        ValidationResult validationResult = thresholdValidator.validate(intervalReadingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.VALID);
    }

    @Test
    public void testValidationOnMaximumPasses() {
        when(intervalReadingRecord.getQuantity(readingType)).thenReturn(Quantity.create(MAXIMUM, 3, "Wh"));

        ValidationResult validationResult = thresholdValidator.validate(intervalReadingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.VALID);
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
        ImmutableMap<String, Object> properties = ImmutableMap.of(MIN, (Object) MINIMUM);
        thresholdValidator = new ThresholdValidator(thesaurus, propertySpecService, properties);
    }

    @Test
    public void testValidationOkForReadingRecord() {
        when(readingRecord.getQuantity(readingType)).thenReturn(IN_THE_MIDDLE);

        ValidationResult validationResult = thresholdValidator.validate(readingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.VALID);
    }

    @Test
    public void testValidationOnMissingDataYieldsSkippedForReadingRecord() {
        when(readingRecord.getQuantity(readingType)).thenReturn(null);

        ValidationResult validationResult = thresholdValidator.validate(readingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.NOT_VALIDATED);
    }

    @Test
    public void testValidationOnMinimumPassesForReadingRecord() {
        when(readingRecord.getQuantity(readingType)).thenReturn(Quantity.create(MINIMUM, 3, "Wh"));

        ValidationResult validationResult = thresholdValidator.validate(readingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.VALID);
    }

    @Test
    public void testValidationOnMaximumPassesForReadingRecord() {
        when(readingRecord.getQuantity(readingType)).thenReturn(Quantity.create(MAXIMUM, 3, "Wh"));

        ValidationResult validationResult = thresholdValidator.validate(readingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.VALID);
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
        assertThat(thresholdValidator.getPropertyNlsKey(MIN)).isEqualTo(SimpleNlsKey.key("UNI", Layer.REST, ThresholdValidator.MIN));
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
        when(thesaurus.getString(ThresholdValidator.MIN, "Minimum")).thenReturn("Au moins");

        assertThat(thresholdValidator.getDisplayName(ThresholdValidator.MIN)).isEqualTo("Au moins");
    }

    @Test
    public void testGetPropertyDefaultFormat() {
        assertThat(thresholdValidator.getPropertyDefaultFormat(MIN)).isEqualTo("Minimum");
        assertThat(thresholdValidator.getPropertyDefaultFormat(MAX)).isEqualTo("Maximum");
        assertThat(thresholdValidator.getPropertyDefaultFormat("notAProperty")).isNull();
    }

    @Test
    public void testFinish() {
        assertThat(thresholdValidator.finish()).isEmpty();
    }

    @Test
    public void testGetReadingQualityTypeCode() {
        assertThat(thresholdValidator.getReadingQualityTypeCode().isPresent()).isFalse();
    }

}