package com.elster.jupiter.validators.impl;

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
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ReadingQualityPropertyValue;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.validation.ValidationResult;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReadingQualitiesValidatorWithWildcardsTest {

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
        ReadingQualityPropertyValue rqpv =  new ReadingQualityPropertyValue("2.7.*");
        selectedReadingQualities.add(rqpv.getCimCode());
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
    public void testReadingQualityMatchesSuspect() {
        ArrayList<ReadingQualityRecord> readingQualities = new ArrayList<>();
        readingQualities.add(mockReadingQuality("2.7.9"));
        readingQualities.add(mockReadingQuality("1.1.0"));
        readingQualities.add(mockReadingQuality("2.2.0"));
        doReturn(readingQualities).when(intervalReadingRecord).getReadingQualities();

        ValidationResult validationResult = validator.validate(intervalReadingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.SUSPECT);
    }

    @Test
    public void testReadingQualityDoesNotMatchSuspect() {
        ArrayList<ReadingQualityRecord> readingQualities = new ArrayList<>();
        readingQualities.add(mockReadingQuality("2.4.9"));
        readingQualities.add(mockReadingQuality("1.1.0"));
        readingQualities.add(mockReadingQuality("2.2.0"));
        doReturn(readingQualities).when(intervalReadingRecord).getReadingQualities();

        ValidationResult validationResult = validator.validate(intervalReadingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.VALID);
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
    public void testValidationValid_Registers() {
        ArrayList<ReadingQualityRecord> readingQualities = new ArrayList<>();
        readingQualities.add(mockReadingQuality("2.4.9"));
        readingQualities.add(mockReadingQuality("1.1.0"));
        readingQualities.add(mockReadingQuality("2.2.0"));
        doReturn(readingQualities).when(readingRecord).getReadingQualities();

        ValidationResult validationResult = validator.validate(readingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.VALID);
    }

    @Test
    public void testValidationSuspect_Registers() {
        ArrayList<ReadingQualityRecord> readingQualities = new ArrayList<>();
        readingQualities.add(mockReadingQuality("2.7.9"));
        readingQualities.add(mockReadingQuality("1.1.0"));
        readingQualities.add(mockReadingQuality("2.2.0"));
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


}
