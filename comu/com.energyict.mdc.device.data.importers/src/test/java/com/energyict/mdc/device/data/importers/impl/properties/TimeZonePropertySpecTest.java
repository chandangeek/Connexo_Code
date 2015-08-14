package com.energyict.mdc.device.data.importers.impl.properties;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.InvalidValueException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TimeZonePropertySpecTest {

    @Mock
    private Thesaurus thesaurus;

    private TimeZonePropertySpec propertySpec;

    @Before
    public void beforeTest() {
        when(thesaurus.getString(anyString(), anyString())).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[1]);
        propertySpec = new TimeZonePropertySpec("name", thesaurus, Clock.system(ZoneOffset.UTC));
    }

    @Test
    public void testDefaultValue() throws Exception {
        assertThat(propertySpec.validateValue("GMT+03:00")).isTrue();
        assertThat(propertySpec.getPossibleValues().getDefault()).isEqualTo("GMT+00:00");
    }

    @Test
    public void testValidateEmptyValue() throws Exception {
        assertThat(propertySpec.validateValue("")).isFalse();
    }

    @Test
    public void testValidateValue() throws Exception {
        assertThatThrownBy(() -> propertySpec.validateValue("GMT+")).isInstanceOf(InvalidValueException.class);
    }
}
