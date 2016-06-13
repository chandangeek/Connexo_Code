package com.elster.jupiter.validators.impl;

import com.elster.jupiter.metering.*;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.validation.ValidationResult;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.validators.impl.ReadingQualitiesValidator.READING_QUALITIES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
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

    private PropertySpecService propertySpecService = new PropertySpecServiceImpl();

    private ReadingQualitiesValidator validator;

    //TODO adjust this to use reading qualities

    @Before
    public void setUp() {
        NlsMessageFormat nlsMessageFormat = mock(NlsMessageFormat.class);
        when(nlsMessageFormat.format()).thenReturn("This unit test does not care about translations");
        when(thesaurus.getFormat(any(TranslationKey.class))).thenReturn(nlsMessageFormat);

        //List<ReadingQualityInformation> flags = new ArrayList<>();
        Map<String, Object> properties = new HashMap<>();
        //properties.put(READING_QUALITIES, flags);
        validator = new ReadingQualitiesValidator(thesaurus, propertySpecService, properties);

       // flags.add(validator.new ReadingQualityInformation(ProtocolReadingQualities.BADTIME, "badTime", "Bad time"));
       // flags.add(validator.new ReadingQualityInformation(ProtocolReadingQualities.POWERDOWN, "powerDown", "Power down"));

        validator.init(channel, readingType, Range.closed(Instant.ofEpochMilli(7000L), Instant.ofEpochMilli(14000L)));
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testValidationOk() {
        //ProfileStatus profileStatus = ProfileStatus.of();
        //TODO replace by reading qualities
        //when(intervalReadingRecord.getProfileStatus()).thenReturn(profileStatus);

        ValidationResult validationResult = validator.validate(intervalReadingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.VALID);
    }

    @Test
    public void testValidationOkDifferentFlags() {
        //ProfileStatus profileStatus = ProfileStatus.of(Flag.POWERUP);
        //TODO replace by reading qualities
        //when(intervalReadingRecord.getProfileStatus()).thenReturn(profileStatus);

        ValidationResult validationResult = validator.validate(intervalReadingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.VALID);
    }

    @Test
    @Ignore
    public void testValidationSuspect() {
        //ProfileStatus profileStatus = ProfileStatus.of(Flag.BADTIME);
        //TODO replace by reading qualities
        //when(intervalReadingRecord.getProfileStatus()).thenReturn(profileStatus);

        ValidationResult validationResult = validator.validate(intervalReadingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.SUSPECT);
    }

    @Test
    public void testDefaultFormat() {
        assertThat(validator.getDefaultFormat()).isEqualTo("Interval state");
    }

    @Test
    public void testGetPropertySpec() {
        List<PropertySpec> propertySpecs = validator.getPropertySpecs();

        assertThat(propertySpecs).hasSize(1);
        PropertySpec propertySpec = propertySpecs.get(0);
        assertThat(propertySpec.getName()).isEqualTo(READING_QUALITIES);
        assertThat(propertySpec.supportsMultiValues()).isTrue();
        assertThat(propertySpec.getValueFactory().getValueType()).isEqualTo(List.class);
    }

    @Test
    public void testGetPropertySpecByName() {
        PropertySpec propertySpec = validator.getPropertySpec(READING_QUALITIES).get();

        assertThat(propertySpec.getName()).isEqualTo(READING_QUALITIES);
        assertThat(propertySpec.getValueFactory().getValueType()).isEqualTo(List.class);

        assertThat(validator.getPropertySpec("flags~")).isEmpty();
    }

    @Test
    public void validateRegisterReadings() {
        ReadingRecord readingRecord = mock(ReadingRecord.class);

        ValidationResult validationResult = validator.validate(readingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.VALID);
    }

    @Test
    public void testGetSupportedApplications() {
        assertThat(validator.getSupportedApplications()).containsOnly("INS", "MDC");
    }
}
