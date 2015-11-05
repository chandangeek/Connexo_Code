package com.elster.jupiter.validators.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.metering.readings.ProfileStatus.Flag;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.ListValue;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validators.impl.IntervalStateValidator.IntervalFlag;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.validators.impl.IntervalStateValidator.INTERVAL_FLAGS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IntervalStateValidatorTest {

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private Channel channel;
    @Mock
    private ReadingType readingType;
    @Mock
    private IntervalReadingRecord intervalReadingRecord;

    private PropertySpecService propertySpecService = new PropertySpecServiceImpl();

    private IntervalStateValidator validator;



    @Before
    public void setUp() {
        ListValue<IntervalFlag> flags = new ListValue<>();
        Map<String, Object> properties = new HashMap<>();
        properties.put(INTERVAL_FLAGS, (Object) flags);
        validator = new IntervalStateValidator(thesaurus, propertySpecService, properties);

        flags.addValue(validator.new IntervalFlag(Flag.BADTIME, "badTime", "Bad time"));
        flags.addValue(validator.new IntervalFlag(Flag.POWERDOWN, "powerDown", "Power down"));

        validator.init(channel, readingType, Range.closed(Instant.ofEpochMilli(7000L), Instant.ofEpochMilli(14000L)));
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testValidationOk() {
        ProfileStatus profileStatus = ProfileStatus.of();
        when(intervalReadingRecord.getProfileStatus()).thenReturn(profileStatus);

        ValidationResult validationResult = validator.validate(intervalReadingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.VALID);
    }

    @Test
    public void testValidationOkDifferentFlags() {
        ProfileStatus profileStatus = ProfileStatus.of(Flag.POWERUP);
        when(intervalReadingRecord.getProfileStatus()).thenReturn(profileStatus);

        ValidationResult validationResult = validator.validate(intervalReadingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.VALID);
    }

    @Test
    public void testValidationSuspect() {
        ProfileStatus profileStatus = ProfileStatus.of(Flag.BADTIME);
        when(intervalReadingRecord.getProfileStatus()).thenReturn(profileStatus);

        ValidationResult validationResult = validator.validate(intervalReadingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.SUSPECT);
    }

    @Test
    public void testGetPropertyDefaultFormat() {
        assertThat(validator.getPropertyDefaultFormat(INTERVAL_FLAGS)).isEqualTo("Interval flags");
        assertThat(validator.getPropertyDefaultFormat("flags~")).isNull();
        assertThat(validator.getPropertyDefaultFormat("")).isNull();
    }

    @Test
    public void testDefaultFormat() {
        assertThat(validator.getDefaultFormat()).isEqualTo("Interval state");
    }

    @Test
    public void testGetPropertySpec() {
        List<PropertySpec> propertySpecs = validator.getPropertySpecs();

        assertThat(propertySpecs).hasSize(1);
        assertThat(propertySpecs.get(0).getName()).isEqualTo(INTERVAL_FLAGS);
        assertThat(propertySpecs.get(0).getValueFactory().getValueType()).isEqualTo(ListValue.class);
    }

    @Test
    public void testGetPropertySpecByName() {
        PropertySpec propertySpec = validator.getPropertySpec(INTERVAL_FLAGS).get();

        assertThat(propertySpec.getName()).isEqualTo(INTERVAL_FLAGS);
        assertThat(propertySpec.getValueFactory().getValueType()).isEqualTo(ListValue.class);

        assertThat(validator.getPropertySpec("flags~")).isEmpty();
    }

    @Test
    public void validateRegisterReadings() {
        ReadingRecord readingRecord = mock(ReadingRecord.class);

        ValidationResult validationResult = validator.validate(readingRecord);

        assertThat(validationResult).isEqualTo(ValidationResult.VALID);
    }

}