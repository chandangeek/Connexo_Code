package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.BoundedBigDecimalPropertySpecImpl;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.impl.DeviceLifeCyclePropertySupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 09.12.15
 * Time: 14:18
 */
@RunWith(MockitoJUnitRunner.class)
public class SetMultiplierTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PropertySpecService propertySpecService;
    @Mock
    private Device device;
    @Mock
    private Thesaurus thesaurus;

    public SetMultiplier getTestInstance() {
        return new SetMultiplier(thesaurus);
    }

    @Test
    public void testGetPropertySpecsDelegatesToPropertySpecService() {
        SetMultiplier setMultiplier = getTestInstance();

        when(propertySpecService.boundedDecimalPropertySpec(any(String.class), any(Boolean.class), any(BigDecimal.class), any(BigDecimal.class)))
                .thenAnswer(invocationOnMock -> new BoundedBigDecimalPropertySpecImpl(
                        ((String) invocationOnMock.getArguments()[0]),
                        ((BigDecimal) invocationOnMock.getArguments()[2]),
                        ((BigDecimal) invocationOnMock.getArguments()[3])));

        // Business method
        List<PropertySpec> propertySpecs = setMultiplier.getPropertySpecs(this.propertySpecService);

        assertThat(propertySpecs).hasSize(1);
        assertThat(propertySpecs.get(0).getName()).isEqualToIgnoringCase(DeviceLifeCycleService.MicroActionPropertyName.MULTIPLIER.key());
    }


    @Test
    public void setMultiplierTest() {
        BigDecimal multiplierValue = BigDecimal.valueOf(13L);
        ExecutableActionProperty property = mock(ExecutableActionProperty.class);
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(DeviceLifeCycleService.MicroActionPropertyName.MULTIPLIER.key());
        when(property.getPropertySpec()).thenReturn(propertySpec);
        when(property.getValue()).thenReturn(multiplierValue);

        getTestInstance().execute(device, Instant.now(), Collections.singletonList(property));

        //asserts
        verify(device).setMultiplier(multiplierValue);
    }
}
