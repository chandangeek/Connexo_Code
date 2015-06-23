package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.topology.TopologyService;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
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

    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private TopologyService topologyService;
    @Mock
    private Device gateway;
    @Mock
    private Device slaveDevice;

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
                .basicPropertySpec(
                        anyString(),
                        anyBoolean(),
                        any(ValueFactory.class));
    }

    @Test
    public void executeWithEffectiveTimestampClearsPhysicalGateway() {
        Instant now = Instant.ofEpochSecond(97L);
        DetachSlaveFromMaster microAction = this.getTestInstance();
        ExecutableActionProperty property = mock(ExecutableActionProperty.class);
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(DeviceLifeCycleService.MicroActionPropertyName.EFFECTIVE_TIMESTAMP.key());
        when(property.getPropertySpec()).thenReturn(propertySpec);
        when(property.getValue()).thenReturn(now);

        // Business method
        microAction.execute(this.slaveDevice, Arrays.asList(property));

        // Asserts
        verify(this.topologyService).clearPhysicalGateway(this.slaveDevice);
    }

    @Test
    public void executeWithoutEffectiveTimeClearsPhysicalGateway() {
        DetachSlaveFromMaster microAction = this.getTestInstance();

        // Business method
        microAction.execute(this.slaveDevice, Collections.emptyList());

        // Asserts
        verify(this.topologyService).clearPhysicalGateway(this.slaveDevice);
    }

    @Test
    public void executeWithEffectiveTimestampDoesNotClearForGatewayDevice() {
        Instant now = Instant.ofEpochSecond(97L);
        DetachSlaveFromMaster microAction = this.getTestInstance();
        ExecutableActionProperty property = mock(ExecutableActionProperty.class);
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(DeviceLifeCycleService.MicroActionPropertyName.EFFECTIVE_TIMESTAMP.key());
        when(property.getPropertySpec()).thenReturn(propertySpec);
        when(property.getValue()).thenReturn(now);

        // Business method
        microAction.execute(this.gateway, Arrays.asList(property));

        // Asserts
        verify(this.topologyService, never()).clearPhysicalGateway(any(Device.class));
    }

    @Test
    public void executeWithoutEffectiveTimestampDoesNotClearForGatewayDevice() {
        DetachSlaveFromMaster microAction = this.getTestInstance();

        // Business method
        microAction.execute(this.gateway, Collections.emptyList());

        // Asserts
        verify(this.topologyService, never()).clearPhysicalGateway(any(Device.class));
    }

    private DetachSlaveFromMaster getTestInstance() {
        return new DetachSlaveFromMaster(this.topologyService);
    }

}