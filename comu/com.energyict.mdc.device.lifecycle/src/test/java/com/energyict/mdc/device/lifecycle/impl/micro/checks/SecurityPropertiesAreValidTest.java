package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;

import com.elster.jupiter.nls.Thesaurus;

import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link SecurityPropertiesAreValid} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-16 (13:12)
 */
@RunWith(MockitoJUnitRunner.class)
public class SecurityPropertiesAreValidTest {

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private Device device;

    @Test
    public void invalidProperties() {
        when(this.device.securityPropertiesAreValid()).thenReturn(false);
        SecurityPropertiesAreValid microCheck = this.getTestInstance();

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device);

        // Asserts
        assertThat(violation.isPresent()).isTrue();
        assertThat(violation.get().getCheck()).isEqualTo(MicroCheck.SECURITY_PROPERTIES_ARE_ALL_VALID);
    }

    @Test
    public void validProperties() {
        when(this.device.securityPropertiesAreValid()).thenReturn(true);
        SecurityPropertiesAreValid microCheck = this.getTestInstance();

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device);

        // Asserts
        assertThat(violation.isPresent()).isFalse();
    }

    private SecurityPropertiesAreValid getTestInstance() {
        return new SecurityPropertiesAreValid(this.thesaurus);
    }

}