package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolationException;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LinkToUsagePointTest {

    @Rule
    public TestRule expectedErrorRule = new ExpectedExceptionRule();

    @Mock
    private Device device;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PropertySpecService propertySpecService;
    @Mock
    private Thesaurus thesaurus;

    public LinkToUsagePoint getTestInstance() {
        return new LinkToUsagePoint(thesaurus);
    }

    @Test
    public void testLinkToUsagePoint() {
        UsagePoint usagePoint = mock(UsagePoint.class);
        ExecutableActionProperty property = mock(ExecutableActionProperty.class);
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(DeviceLifeCycleService.MicroActionPropertyName.USAGE_POINT.key());
        when(property.getPropertySpec()).thenReturn(propertySpec);
        when(property.getValue()).thenReturn(usagePoint);
        when(device.getCurrentMeterActivation()).thenReturn(Optional.empty());

        //Business method
        Instant now = Instant.now();
        LinkToUsagePoint microAction = getTestInstance();
        MeterActivationBuilderImpl meterActivationBuilder = new MeterActivationBuilderImpl(device);
        microAction.buildMeterActivation(meterActivationBuilder, device, now, Collections.singletonList(property));
        List<MeterActivation> meterActivations = meterActivationBuilder.build();
        microAction.execute(device, now, Collections.singletonList(property));

        //Asserts
        verify(device).forceActivate(now, usagePoint);
    }

    @Test
    public void testLinkToUsagePointWithoutUsagePoint() {
        //Business method
        Instant now = Instant.now();
        getTestInstance().execute(device, now, Collections.emptyList());

        //Asserts
        verifyNoMoreInteractions(device);
    }

    @Test(expected = DeviceLifeCycleActionViolationException.class)
    public void testLinkToUsagePointWrapExceptions() {
        UsagePoint usagePoint = mock(UsagePoint.class);
        ExecutableActionProperty property = mock(ExecutableActionProperty.class);
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(DeviceLifeCycleService.MicroActionPropertyName.USAGE_POINT.key());
        when(property.getPropertySpec()).thenReturn(propertySpec);
        when(property.getValue()).thenReturn(usagePoint);
        Instant now = Instant.now();
        LocalizedException exception = mock(LocalizedException.class);
        when(device.forceActivate(now, usagePoint)).thenThrow(exception);
        when(device.getCurrentMeterActivation()).thenReturn(Optional.empty());

        //Business method
        LinkToUsagePoint microAction = getTestInstance();
        MeterActivationBuilderImpl meterActivationBuilder = new MeterActivationBuilderImpl(device);
        microAction.buildMeterActivation(meterActivationBuilder, device, now, Collections.singletonList(property));
        List<MeterActivation> meterActivations = meterActivationBuilder.build();
        microAction.execute(device, now, Collections.singletonList(property));

        //Asserts
        //expected that exception is thrown
    }
}
