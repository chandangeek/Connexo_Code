package com.elster.jupiter.validators.impl;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.validation.ValidationResult;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.elster.jupiter.validators.impl.RegisterIncreaseValidator.FAIL_EQUAL_DATA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RegisterIncreaseTest {

    private static BigDecimal value = BigDecimal.valueOf(100);
    private static BigDecimal biggerValue = BigDecimal.valueOf(120);

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private Channel channel;
    @Mock
    private ReadingType readingType;
    @Mock
    private ReadingRecord readingRecord;
    @Mock
    private BaseReadingRecord previousReadingRecord;

    private PropertySpecService propertySpecService = new PropertySpecServiceImpl();

    private RegisterIncreaseValidator validator;

    @Before
    public void setUp() {
        ImmutableMap<String, Object> properties = ImmutableMap.of(FAIL_EQUAL_DATA, (Object) true);
        validator = new RegisterIncreaseValidator(thesaurus, propertySpecService, properties);
        validator.init(channel, readingType, Range.closed(Instant.ofEpochMilli(7000L), Instant.ofEpochMilli(14000L)));
        List<BaseReadingRecord> records = Arrays.asList(previousReadingRecord);
        when(channel.getReadingsBefore(readingRecord.getTimeStamp(), 1)).thenReturn(records);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testValidationNoReadings() {
        List<BaseReadingRecord> records = Collections.emptyList();
        when(channel.getReadingsBefore(readingRecord.getTimeStamp(), 1)).thenReturn(records);

        ValidationResult validationResult = validator.validate(readingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.VALID);
    }

    @Test
    public void testValidationOk() {
        when(previousReadingRecord.getValue()).thenReturn(value);
        when(readingRecord.getValue()).thenReturn(biggerValue);

        ValidationResult validationResult = validator.validate(readingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.VALID);
    }

    @Test
    public void testValidationSuspect() {
        when(previousReadingRecord.getValue()).thenReturn(biggerValue);
        when(readingRecord.getValue()).thenReturn(value);

        ValidationResult validationResult = validator.validate(readingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.SUSPECT);
    }

    @Test
    public void testValidationSuspectFailEqualDataTrue() {
        when(previousReadingRecord.getValue()).thenReturn(value);
        when(readingRecord.getValue()).thenReturn(value);

        ValidationResult validationResult = validator.validate(readingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.SUSPECT);
    }

    @Test
    public void testValidationSuspectFailEqualDataFalse() {
        ImmutableMap<String, Object> properties = ImmutableMap.of(FAIL_EQUAL_DATA, (Object) false);
        validator = new RegisterIncreaseValidator(thesaurus, propertySpecService, properties);
        validator.init(channel, readingType, Range.closed(Instant.ofEpochMilli(7000L), Instant.ofEpochMilli(14000L)));

        when(previousReadingRecord.getValue()).thenReturn(value);
        when(readingRecord.getValue()).thenReturn(biggerValue);

        ValidationResult validationResult = validator.validate(readingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.VALID);
    }

    @Test
    public void validateIntervalReadings() {
        IntervalReadingRecord intervalReading = mock(IntervalReadingRecord.class);

        ValidationResult validationResult = validator.validate(intervalReading);

        assertThat(validationResult).isEqualTo(ValidationResult.VALID);
    }

    @Test
    public void testGetPropertyDefaultFormat() {
        assertThat(validator.getPropertyDefaultFormat(FAIL_EQUAL_DATA)).isEqualTo("Fail equal data");
        assertThat(validator.getPropertyDefaultFormat("failEqualData~")).isNull();
        assertThat(validator.getPropertyDefaultFormat("")).isNull();
    }

    @Test
    public void testDefaultFormat() {
        assertThat(validator.getDefaultFormat()).isEqualTo("Register increase");
    }

    @Test
    public void testGetPropertySpec() {
        List<PropertySpec> propertySpecs = validator.getPropertySpecs();

        assertThat(propertySpecs).hasSize(1);
        assertThat(propertySpecs.get(0).getName()).isEqualTo(FAIL_EQUAL_DATA);
        assertThat(propertySpecs.get(0).getValueFactory().getValueType()).isEqualTo(Boolean.class);
    }

    @Test
    public void testGetPropertySpecByName() {
        PropertySpec propertySpec = validator.getPropertySpec(FAIL_EQUAL_DATA);

        assertThat(propertySpec.getName()).isEqualTo(FAIL_EQUAL_DATA);
        assertThat(propertySpec.getValueFactory().getValueType()).isEqualTo(Boolean.class);

        propertySpec = validator.getPropertySpec("failEqualData~");
        assertThat(propertySpec).isNull();
    }
}
