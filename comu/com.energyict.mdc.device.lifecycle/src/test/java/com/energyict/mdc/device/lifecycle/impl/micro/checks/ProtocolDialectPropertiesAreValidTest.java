package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;

import com.elster.jupiter.nls.Thesaurus;

import java.util.Arrays;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ProtocolDialectPropertiesAreValid} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-16 (14:03)
 */
@RunWith(MockitoJUnitRunner.class)
public class ProtocolDialectPropertiesAreValidTest {

    private static final String DIALECT1_NAME = "One";
    private static final String DIALECT2_NAME = "Two";

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties1;
    @Mock
    private ComTaskEnablement comTaskEnablement1;
    @Mock
    private ProtocolDialectProperties protocolDialectProperties1;
    @Mock
    private ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties2;
    @Mock
    private ComTaskEnablement comTaskEnablement2;
    @Mock
    private ProtocolDialectProperties protocolDialectProperties2;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private Device device;

    @Before
    public void initializeMocks() {
        when(this.protocolDialectConfigurationProperties1.getDeviceProtocolDialectName()).thenReturn(DIALECT1_NAME);
        when(this.protocolDialectConfigurationProperties2.getDeviceProtocolDialectName()).thenReturn(DIALECT2_NAME);
        when(this.comTaskEnablement1.getProtocolDialectConfigurationProperties()).thenReturn(this.protocolDialectConfigurationProperties1);
        when(this.comTaskEnablement2.getProtocolDialectConfigurationProperties()).thenReturn(this.protocolDialectConfigurationProperties2);
        when(this.deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement1, comTaskEnablement2));
        when(this.protocolDialectProperties1.getProtocolDialectConfigurationProperties()).thenReturn(this.protocolDialectConfigurationProperties1);
        when(this.protocolDialectProperties1.getDeviceProtocolDialectName()).thenReturn(DIALECT1_NAME);
        when(this.protocolDialectProperties2.getProtocolDialectConfigurationProperties()).thenReturn(this.protocolDialectConfigurationProperties2);
        when(this.protocolDialectProperties2.getDeviceProtocolDialectName()).thenReturn(DIALECT2_NAME);
        when(this.device.getDeviceConfiguration()).thenReturn(this.deviceConfiguration);
    }

    @Test
    public void allDialectsAreMissing() {
        when(this.device.getProtocolDialectProperties(DIALECT1_NAME)).thenReturn(Optional.<ProtocolDialectProperties>empty());
        when(this.device.getProtocolDialectProperties(DIALECT2_NAME)).thenReturn(Optional.<ProtocolDialectProperties>empty());
        ProtocolDialectPropertiesAreValid microCheck = this.getTestInstance();

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device);

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(MicroCheck.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID);
    }

    @Test
    public void dialect1IsMissing() {
        when(this.device.getProtocolDialectProperties(DIALECT1_NAME)).thenReturn(Optional.<ProtocolDialectProperties>empty());
        when(this.device.getProtocolDialectProperties(DIALECT2_NAME)).thenReturn(Optional.of(this.protocolDialectProperties2));
        ProtocolDialectPropertiesAreValid microCheck = this.getTestInstance();

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device);

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(MicroCheck.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID);
    }

    @Test
    public void dialect2IsMissing() {
        when(this.device.getProtocolDialectProperties(DIALECT1_NAME)).thenReturn(Optional.of(this.protocolDialectProperties1));
        when(this.device.getProtocolDialectProperties(DIALECT2_NAME)).thenReturn(Optional.<ProtocolDialectProperties>empty());
        ProtocolDialectPropertiesAreValid microCheck = this.getTestInstance();

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device);

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(MicroCheck.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID);
    }

    @Test
    public void allDialectsAreValid() {
        when(this.device.getProtocolDialectProperties(DIALECT1_NAME)).thenReturn(Optional.of(this.protocolDialectProperties1));
        when(this.device.getProtocolDialectProperties(DIALECT2_NAME)).thenReturn(Optional.of(this.protocolDialectProperties2));
        ProtocolDialectPropertiesAreValid microCheck = this.getTestInstance();

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device);

        // Asserts
        assertThat(violation).isEmpty();
    }

    public ProtocolDialectPropertiesAreValid getTestInstance() {
        return new ProtocolDialectPropertiesAreValid(this.thesaurus);
    }

}