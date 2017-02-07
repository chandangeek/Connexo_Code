/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.ProtocolReadingQualities;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.validation.ValidationResult;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.validators.impl.ReadingQualitiesValidator.READING_QUALITIES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReadingQualitiesValidatorTest {

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private Channel channel;
    @Mock
    private ReadingType readingType;
    @Mock
    private IntervalReadingRecord intervalReadingRecord;
    @Mock
    private ReadingRecord readingRecord;

    private PropertySpecService propertySpecService = new PropertySpecServiceImpl();

    private ReadingQualitiesValidator validator;

    @Before
    public void setUp() {
        NlsMessageFormat nlsMessageFormat = mock(NlsMessageFormat.class);
        when(nlsMessageFormat.format()).thenReturn("This unit test does not care about translations");
        when(thesaurus.getFormat(any(TranslationKey.class))).thenReturn(nlsMessageFormat);
        when(thesaurus.getFormat(any(MessageSeed.class))).thenReturn(nlsMessageFormat);

        //Make a validation rule that checks for reading qualities 'Bad time' and 'Power down'
        Map<String, Object> properties = new HashMap<>();
        ArrayList<String> selectedReadingQualities = new ArrayList<>();
        selectedReadingQualities.add(ProtocolReadingQualities.BADTIME.getCimCode());
        selectedReadingQualities.add(ProtocolReadingQualities.POWERDOWN.getCimCode());
        properties.put(ReadingQualitiesValidator.READING_QUALITIES, selectedReadingQualities);
        validator = new ReadingQualitiesValidator(thesaurus, propertySpecService, properties);
        validator.init(channel, readingType, Range.closed(Instant.ofEpochMilli(7000L), Instant.ofEpochMilli(14000L)));
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testValidationOk() {
        when(intervalReadingRecord.getReadingQualities()).thenReturn(new ArrayList<>());

        ValidationResult validationResult = validator.validate(intervalReadingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.VALID);
    }

    @Test
    public void testValidationOkDifferentReadingQualities() {
        ArrayList<ReadingQualityRecord> readingQualities = new ArrayList<>();
        readingQualities.add(mockReadingQuality(ProtocolReadingQualities.POWERUP.getCimCode()));
        doReturn(readingQualities).when(intervalReadingRecord).getReadingQualities();

        ValidationResult validationResult = validator.validate(intervalReadingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.VALID);
    }

    @Test
    public void testValidationSuspect() {
        ArrayList<ReadingQualityRecord> readingQualities = new ArrayList<>();
        readingQualities.add(mockReadingQuality(ProtocolReadingQualities.BADTIME.getCimCode()));
        doReturn(readingQualities).when(intervalReadingRecord).getReadingQualities();

        ValidationResult validationResult = validator.validate(intervalReadingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.SUSPECT);
    }

    @Test
    public void testValidationOk_Registers() {
        when(readingRecord.getReadingQualities()).thenReturn(new ArrayList<>());

        ValidationResult validationResult = validator.validate(readingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.VALID);
    }

    @Test
    public void testValidationOkDifferentReadingQualities_Registers() {
        ArrayList<ReadingQualityRecord> readingQualities = new ArrayList<>();
        readingQualities.add(mockReadingQuality(ProtocolReadingQualities.POWERUP.getCimCode()));
        doReturn(readingQualities).when(readingRecord).getReadingQualities();

        ValidationResult validationResult = validator.validate(readingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.VALID);
    }

    @Test
    public void testValidationSuspect_Registers() {
        ArrayList<ReadingQualityRecord> readingQualities = new ArrayList<>();
        readingQualities.add(mockReadingQuality(ProtocolReadingQualities.BADTIME.getCimCode()));
        doReturn(readingQualities).when(readingRecord).getReadingQualities();

        ValidationResult validationResult = validator.validate(readingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.SUSPECT);
    }

    private ReadingQualityRecord mockReadingQuality(String code) {
        ReadingQualityRecord readingQuality = mock(ReadingQualityRecord.class);
        ReadingQualityType readingQualityType = new ReadingQualityType(code);
        when(readingQuality.getType()).thenReturn(readingQualityType);
        when(readingQuality.isActual()).thenReturn(true);
        when(readingQuality.getTypeCode()).thenReturn(code);
        return readingQuality;
    }

    @Test
    public void testDefaultFormat() {
        assertThat(validator.getDefaultFormat()).isEqualTo("Reading qualities");
    }

    @Test
    public void testGetPropertySpec() {
        List<PropertySpec> propertySpecs = validator.getPropertySpecs();

        assertThat(propertySpecs).hasSize(1);
        PropertySpec propertySpec = propertySpecs.get(0);
        assertThat(propertySpec.getName()).isEqualTo(READING_QUALITIES);
        assertThat(propertySpec.supportsMultiValues()).isTrue();
        assertThat(propertySpec.isRequired()).isTrue();
        assertThat(propertySpec.getValueFactory().getValueType()).isEqualTo(List.class);
    }

    @Test
    public void testGetPropertySpecByName() {
        PropertySpec propertySpec = validator.getPropertySpec(READING_QUALITIES).get();

        assertThat(propertySpec.getName()).isEqualTo(READING_QUALITIES);
        assertThat(propertySpec.getValueFactory().getValueType()).isEqualTo(List.class);

        assertThat(validator.getPropertySpec("invalidPropertyName")).isEmpty();
    }

    @Test
    public void validateRegisterReadings() {
        ReadingRecord readingRecord = mock(ReadingRecord.class);

        ValidationResult validationResult = validator.validate(readingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.VALID);
    }

    @Test
    public void testGetSupportedQualityCodeSystems() {
        assertThat(validator.getSupportedQualityCodeSystems()).containsOnly(QualityCodeSystem.MDC, QualityCodeSystem.MDM);
    }
}
