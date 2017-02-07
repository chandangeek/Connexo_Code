/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;
import com.energyict.mdc.device.data.Device;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests the {@link CloseMeterActivation} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-06 (14:47)
 */
@RunWith(MockitoJUnitRunner.class)
public class CloseMeterActivationTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PropertySpecService propertySpecService;
    @Mock
    private Device device;
    @Mock
    private Thesaurus thesaurus;

    @Test
    public void testGetPropertySpecsDelegatesToPropertySpecService() {
        CloseMeterActivation microAction = this.getTestInstance();

        // Business method
        List<PropertySpec> propertySpecs = microAction.getPropertySpecs(this.propertySpecService);

        // Asserts
        int numberOfSpecs = propertySpecs.size();
        verify(this.propertySpecService, times(numberOfSpecs))
                .specForValuesOf(any(ValueFactory.class));
    }

    @Test
    public void executeWithEffectiveTimeClosesMeterActivation() {
        Instant now = Instant.ofEpochSecond(97L);
        CloseMeterActivation microAction = this.getTestInstance();

        // Business method
        microAction.execute(this.device, now, Collections.emptyList());

        // Asserts
        verify(this.device).deactivate(now);
    }

    private CloseMeterActivation getTestInstance() {
        return new CloseMeterActivation(thesaurus);
    }

}