/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.TopologyService;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DetachSlaveFromMaster} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-06 (15:35)
 */
@RunWith(MockitoJUnitRunner.class)
public class DetachSlaveFromMasterTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PropertySpecService propertySpecService;
    @Mock
    private TopologyService topologyService;
    @Mock
    private Device gateway;
    @Mock
    private Device slaveDevice;
    @Mock
    private Thesaurus thesaurus;

    @Before
    public void initializeMocks() {
        when(topologyService.getPhysicalGateway(slaveDevice)).thenReturn(Optional.of(gateway));
        when(topologyService.getPhysicalGateway(gateway)).thenReturn(Optional.empty());
    }

    @Test
    public void testGetPropertySpecsDelegatesToPropertySpecService() {
        DetachSlaveFromMaster microAction = this.getTestInstance();

        // Business method
        List<PropertySpec> propertySpecs = microAction.getPropertySpecs(this.propertySpecService);

        // Asserts
        int numberOfSpecs = propertySpecs.size();
        verify(this.propertySpecService, times(numberOfSpecs))
                .specForValuesOf(any(ValueFactory.class));
    }

    @Test
    public void executeWithEffectiveTimestampClearsPhysicalGateway() {
        Instant now = Instant.ofEpochSecond(97L);
        DetachSlaveFromMaster microAction = this.getTestInstance();

        // Business method
        microAction.execute(this.slaveDevice, now, Collections.emptyList());

        // Asserts
        verify(this.topologyService).clearPhysicalGateway(this.slaveDevice);
    }

    @Test
    public void executeWithoutEffectiveTimeClearsPhysicalGateway() {
        DetachSlaveFromMaster microAction = this.getTestInstance();
        Instant now = Instant.now();

        // Business method
        microAction.execute(this.slaveDevice, now, Collections.emptyList());

        // Asserts
        verify(this.topologyService).clearPhysicalGateway(this.slaveDevice);
    }

    @Test
    public void executeWithEffectiveTimestampDoesNotClearForGatewayDevice() {
        Instant now = Instant.ofEpochSecond(97L);
        DetachSlaveFromMaster microAction = this.getTestInstance();

        // Business method
        microAction.execute(this.gateway, now, Collections.emptyList());

        // Asserts
        verify(this.topologyService, never()).clearPhysicalGateway(any(Device.class));
    }

    @Test
    public void executeWithoutEffectiveTimestampDoesNotClearForGatewayDevice() {
        DetachSlaveFromMaster microAction = this.getTestInstance();

        // Business method
        microAction.execute(this.gateway, Instant.now(), Collections.emptyList());

        // Asserts
        verify(this.topologyService, never()).clearPhysicalGateway(any(Device.class));
    }

    private DetachSlaveFromMaster getTestInstance() {
        return new DetachSlaveFromMaster(thesaurus, this.topologyService);
    }

}