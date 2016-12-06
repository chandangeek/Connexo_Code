package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.mockito.Mockito.when;

/**
 * Tests the {@link DeviceConfigurationImpl} component.
 * <p>
 * Copyrights EnergyICT
 * Date: 20/02/14
 * Time: 10:21
 */
public class DeviceConfigurationImplWithRealProtocolPluggableServiceTest extends PersistenceWithRealProtocolPluggableServiceTest {

    private static final String DEVICE_TYPE_NAME = "DeviceConfigWRPPSTestType";

    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    private DeviceType deviceType;

    @Before
    public void initializeDeviceType() {
        when(this.deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Arrays.asList(DeviceProtocolCapabilities.values()));
        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(DEVICE_TYPE_NAME, deviceProtocolPluggableClass);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_CONFIG_DIRECTLY_ADDRESSABLE_WHEN_CONNECTIONTASKS + "}", strict = false)
    public void cannotSwitchOffDirectlyAddressableWithPartialConnectionTasks() {
        when(this.deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Arrays.asList(DeviceProtocolCapabilities.values()));
        ConnectionTypePluggableClass connectionTypePluggableClass =
                inMemoryPersistence.getProtocolPluggableService().
                        newConnectionTypePluggableClass(
                                "cannotSwitchOffDirectlyAddressableWithPartialConnectionTasks",
                                OutboundNoParamsConnectionTypeImpl.class.getName());
        connectionTypePluggableClass.save();
        DeviceConfiguration deviceConfiguration =
                deviceType
                    .newConfiguration("cannotSwitchOffDirectlyAddressableWithPartialConnectionTasks")
                    .isDirectlyAddressable(true)
                    .add();
        deviceConfiguration.
                newPartialScheduledConnectionTask(
                        "cannotSwitchOffDirectlyAddressableWithPartialConnectionTasks",
                        connectionTypePluggableClass,
                        TimeDuration.minutes(5),
                        ConnectionStrategy.MINIMIZE_CONNECTIONS).
                nextExecutionSpec().temporalExpression(TimeDuration.hours(1)).set().
                build();

        // Business method
        deviceConfiguration.setDirectlyAddressable(false);
        deviceConfiguration.save();

        // Asserts: see expected constraint violation rule
    }

}