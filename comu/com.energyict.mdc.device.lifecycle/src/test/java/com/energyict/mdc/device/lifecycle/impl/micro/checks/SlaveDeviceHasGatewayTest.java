/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheckViolation;
import com.energyict.mdc.device.topology.TopologyService;

import java.time.Instant;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link SlaveDeviceHasGateway} component
 */
@RunWith(MockitoJUnitRunner.class)
public class SlaveDeviceHasGatewayTest {

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private TopologyService topologyService;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private Device device;
    @Mock
    private State state;

    @Test
    public void gatewayDevice() {
        SlaveDeviceHasGateway microCheck = this.getTestInstance();
        when(this.device.isLogicalSlave()).thenReturn(false);
        when(this.device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.isDirectlyAddressable()).thenReturn(true);

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void slaveDeviceWithGateway() {
        SlaveDeviceHasGateway microCheck = this.getTestInstance();
        when(this.device.isLogicalSlave()).thenReturn(true);
        Device gateway = mock(Device.class);
        when(this.topologyService.getPhysicalGateway(this.device)).thenReturn(Optional.of(gateway));

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void slaveDeviceWithoutGateway() {
        SlaveDeviceHasGateway microCheck = this.getTestInstance();
        when(this.device.isLogicalSlave()).thenReturn(true);
        when(this.topologyService.getPhysicalGateway(this.device)).thenReturn(Optional.empty());

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(microCheck);
    }

    @Test
    public void slaveDevice2WithoutGateway() {
        SlaveDeviceHasGateway microCheck = this.getTestInstance();
        when(this.device.isLogicalSlave()).thenReturn(false);
        when(this.device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.isDirectlyAddressable()).thenReturn(false);
        when(this.topologyService.getPhysicalGateway(this.device)).thenReturn(Optional.empty());

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(microCheck);
    }

    private SlaveDeviceHasGateway getTestInstance() {
        SlaveDeviceHasGateway slaveDeviceHasGateway = new SlaveDeviceHasGateway();
        slaveDeviceHasGateway.setThesaurus(this.thesaurus);
        slaveDeviceHasGateway.setTopologyService(this.topologyService);
        return slaveDeviceHasGateway;
    }
}
