/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.properties;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.properties.InvalidValueException;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(MockitoJUnitRunner.class)
public class DateFormatPropertySpecTest {
    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    private DateFormatPropertySpec propertySpec;

    @Before
    public void beforeTest() {
        propertySpec = new DateFormatPropertySpec("name", TranslationKeys.DEVICE_DATA_IMPORTER_DATE_FORMAT, this.thesaurus);
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
