/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;

import java.time.Instant;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

/**
 * Tests the {@link RemoveLocation} component.
 */
@RunWith(MockitoJUnitRunner.class)
public class RemoveLocationTest {
    @Mock
    private Device device;
    @Mock
    private Thesaurus thesaurus;

    @Test
    public void executeRemoveLocation() {
        Instant now = Instant.ofEpochSecond(97L);
        RemoveLocation microAction = this.getTestInstance();

        // Business method
        microAction.execute(this.device, now, Collections.emptyList());

        // Asserts
        verify(device).setLocation(null);
        verify(device).setSpatialCoordinates(null);
        verify(device).save();
    }

    private RemoveLocation getTestInstance() {
        return new RemoveLocation(thesaurus);
    }
}
