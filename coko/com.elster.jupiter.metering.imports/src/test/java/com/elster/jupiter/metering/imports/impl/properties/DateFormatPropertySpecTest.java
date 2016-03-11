package com.elster.jupiter.metering.imports.impl.properties;

import com.elster.jupiter.metering.imports.impl.SimpleNlsMessageFormat;
import com.elster.jupiter.metering.imports.impl.Translations;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.util.exception.MessageSeed;

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
public class DateFormatPropertySpecTest {

    @Mock
    private Thesaurus thesaurus;

    private DateFormatPropertySpec propertySpec;

    @Before
    public void beforeTest() {
        when(thesaurus.getFormat(any(TranslationKey.class)))
                .thenAnswer(invocationOnMock -> new SimpleNlsMessageFormat((TranslationKey) invocationOnMock.getArguments()[0]));
        when(thesaurus.getFormat(any(MessageSeed.class)))
                .thenAnswer(invocationOnMock -> new SimpleNlsMessageFormat((MessageSeed) invocationOnMock.getArguments()[0]));
        propertySpec = new DateFormatPropertySpec("name", Translations.Labels.DATA_IMPORTER_DATE_FORMAT, this.thesaurus);
    }

    @Test
    public void testDefaultValue() throws InvalidValueException {
        assertThat(propertySpec.validateValue("dd/MM/yyyy HH:mm")).isTrue();
        assertThat(propertySpec.getPossibleValues().getDefault()).isEqualTo("dd/MM/yyyy HH:mm");
    }

    @Test
    public void testValidateEmptyValue() throws InvalidValueException {
        assertThat(propertySpec.validateValue("")).isFalse();
    }

    @Test
    public void testValidateValue() throws InvalidValueException {
        assertThatThrownBy(() -> propertySpec.validateValue("ddd/MMM//yy")).isInstanceOf(InvalidValueException.class);
    }

}