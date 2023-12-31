/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.device.config.ConnectionStrategy;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.protocol.ConnectionTypePluggableClass;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.mockito.Mockito.when;

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
                        ConnectionStrategy.MINIMIZE_CONNECTIONS, deviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0)).
                nextExecutionSpec().temporalExpression(TimeDuration.hours(1)).set().
                build();

        // Business method
        deviceConfiguration.setDirectlyAddressable(false);
        deviceConfiguration.save();

        // Asserts: see expected constraint violation rule
    }

}