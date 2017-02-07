/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    public void setMultiplierTest() {
        BigDecimal multiplierValue = BigDecimal.valueOf(13L);
        ExecutableActionProperty property = mock(ExecutableActionProperty.class);
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(DeviceLifeCycleService.MicroActionPropertyName.MULTIPLIER.key());
        when(property.getPropertySpec()).thenReturn(propertySpec);
        when(property.getValue()).thenReturn(multiplierValue);

        Instant now = Instant.now();
        getTestInstance().execute(device, now, Collections.singletonList(property));

        //asserts
        verify(device).setMultiplier(multiplierValue, now);
    }
}
