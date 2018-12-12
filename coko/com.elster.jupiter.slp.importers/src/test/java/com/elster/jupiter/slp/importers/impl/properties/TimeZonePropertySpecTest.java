/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.slp.importers.impl.properties;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.slp.importers.impl.TranslationKeys;
import com.elster.jupiter.util.exception.MessageSeed;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TimeZonePropertySpecTest {

    @Mock
    private Thesaurus thesaurus;
    @Mock
    NlsMessageFormat nlsMessageFormat;

    // in case you are running this test in a different time zone than Europe/Athens
    // do change the time zone according with your location
    private Clock clock = Clock.system(ZoneId.of("Europe/Athens"));  //CXO-7969
    private TimeZonePropertySpec propertySpec;

    @Before
    public void beforeTest() {
        when(nlsMessageFormat.format(anyObject())).thenReturn("format");
        when(thesaurus.getFormat(any(TranslationKey.class)))
                .thenAnswer(invocationOnMock -> nlsMessageFormat);
        when(thesaurus.getFormat(any(MessageSeed.class)))
                .thenAnswer(invocationOnMock -> nlsMessageFormat);
        this.propertySpec = new TimeZonePropertySpec("name", TranslationKeys.Labels.DATA_IMPORTER_TIMEZONE, this.thesaurus, this.clock);
    }

    @Test
    public void testDefaultValue() throws InvalidValueException {
        // in case you are running this test in a different time zone than Europe/Athens
        // do change the time zone according with your location
        assertThat(propertySpec.validateValue("Europe/Athens")).isTrue();            //CXO-7969
        assertThat(propertySpec.getPossibleValues().getDefault()).isEqualTo("Europe/Athens");
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