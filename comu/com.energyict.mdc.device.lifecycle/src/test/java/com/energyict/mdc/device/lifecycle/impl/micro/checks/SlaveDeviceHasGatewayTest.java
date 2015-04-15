package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.topology.TopologyService;

import com.elster.jupiter.nls.Thesaurus;

import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link SlaveDeviceHasGateway} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-15 (10:05)
 */
@RunWith(MockitoJUnitRunner.class)
public class SlaveDeviceHasGatewayTest {

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private TopologyService topologyService;
    @Mock
    private Device device;

    @Test
    public void gatewayDevice() {
        SlaveDeviceHasGateway microCheck = this.getTestInstance();
        when(this.device.isLogicalSlave()).thenReturn(false);

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device);

        // Asserts
        assertThat(violation.isPresent()).isFalse();
    }

    @Test
    public void slaveDeviceWithGateway() {
        SlaveDeviceHasGateway microCheck = this.getTestInstance();
        when(this.device.isLogicalSlave()).thenReturn(true);
        Device gateway = mock(Device.class);
        when(this.topologyService.getPhysicalGateway(this.device)).thenReturn(Optional.of(gateway));

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device);

        // Asserts
        assertThat(violation.isPresent()).isFalse();
    }

    @Test
    public void slaveDeviceWithoutGateway() {
        SlaveDeviceHasGateway microCheck = this.getTestInstance();
        when(this.device.isLogicalSlave()).thenReturn(true);
        when(this.topologyService.getPhysicalGateway(this.device)).thenReturn(Optional.empty());

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device);

        // Asserts
        assertThat(violation.isPresent()).isTrue();
        assertThat(violation.get().getCheck()).isEqualTo(MicroCheck.SLAVE_DEVICE_HAS_GATEWAY);
    }

    private SlaveDeviceHasGateway getTestInstance() {
        return new SlaveDeviceHasGateway(this.thesaurus, this.topologyService);
    }

}