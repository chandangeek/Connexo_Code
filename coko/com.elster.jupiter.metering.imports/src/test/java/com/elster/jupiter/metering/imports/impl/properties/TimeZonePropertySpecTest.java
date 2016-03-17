package com.elster.jupiter.metering.imports.impl.properties;

import com.elster.jupiter.devtools.persistence.test.SimpleNlsMessageFormat;
import com.elster.jupiter.metering.imports.impl.TranslationKeys;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.util.exception.MessageSeed;

import java.time.Clock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TimeZonePropertySpecTest {

    @Mock
    private Thesaurus thesaurus;

    private Clock clock = Clock.systemUTC();
    private TimeZonePropertySpec propertySpec;

    @Before
    public void beforeTest() {
        when(thesaurus.getFormat(any(TranslationKey.class)))
                .thenAnswer(invocationOnMock -> new SimpleNlsMessageFormat((TranslationKey) invocationOnMock.getArguments()[0]));
        when(thesaurus.getFormat(any(MessageSeed.class)))
                .thenAnswer(invocationOnMock -> new SimpleNlsMessageFormat((MessageSeed) invocationOnMock.getArguments()[0]));
        this.propertySpec = new TimeZonePropertySpec("name", TranslationKeys.Labels.DATA_IMPORTER_TIMEZONE, this.thesaurus, this.clock);
    }

    @Test
    public void testDefaultValue() throws InvalidValueException {
        assertThat(propertySpec.validateValue("GMT+03:00")).isTrue();
        assertThat(propertySpec.getPossibleValues().getDefault()).isEqualTo("GMT+00:00");
    }

    @Test
    public void testValidateEmptyValue() throws InvalidValueException {
        assertThat(propertySpec.validateValue("")).isFalse();
    }

    @Test
    public void testValidateValue() throws InvalidValueException {
        assertThatThrownBy(() -> propertySpec.validateValue("GMT+")).isInstanceOf(InvalidValueException.class);
    }

}