/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.properties;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.properties.InvalidValueException;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;

import java.time.Clock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(MockitoJUnitRunner.class)
public class TimeZonePropertySpecTest {
    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    private Clock clock = Clock.systemUTC();
    private TimeZonePropertySpec propertySpec;

    @Before
    public void beforeTest() {
        this.propertySpec = new TimeZonePropertySpec("name", TranslationKeys.DEVICE_DATA_IMPORTER_TIMEZONE, this.thesaurus, this.clock);
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
