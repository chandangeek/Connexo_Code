package com.energyict.mdc.device.data.importers.impl.properties;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.InvalidValueException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DateFormatPropertySpecTest {

    @Mock
    private Thesaurus thesaurus;

    private DateFormatPropertySpec propertySpec;

    @Before
    public void beforeTest() {
        when(thesaurus.getString(anyString(), anyString())).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[1]);
        propertySpec = new DateFormatPropertySpec("name", thesaurus);
    }

    @Test
    public void testDefaultValue() throws Exception {
        assertThat(propertySpec.validateValue("dd/MM/yyyy HH:mm")).isTrue();
        assertThat(propertySpec.getPossibleValues().getDefault()).isEqualTo("dd/MM/yyyy HH:mm");
    }

    @Test
    public void testValidateEmptyValue() throws Exception {
        assertThat(propertySpec.validateValue("")).isFalse();
    }

    @Test
    public void testValidateValue() throws Exception {
        assertThatThrownBy(() -> propertySpec.validateValue("ddd/MMM//yy")).isInstanceOf(InvalidValueException.class);
    }
}
