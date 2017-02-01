/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl.deviceconfigchange;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.impl.MessageSeeds;
import com.energyict.mdc.device.config.impl.PartialScheduledConnectionTaskImpl;
import com.energyict.mdc.device.config.impl.ServerDeviceType;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

public class DeviceConfigConflictMappingImplIT extends AbstractConflictIT {

    @Test
    @Transactional
    public void createWithoutViolationsTest() {
        ServerDeviceType deviceType = (ServerDeviceType) deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);

        DeviceConfiguration sourceConfig = deviceType.newConfiguration("Source").isDirectlyAddressable(true).add();
        sourceConfig.activate();

        DeviceConfiguration destinationConfig = deviceType.newConfiguration("Destination").isDirectlyAddressable(true).add();
        destinationConfig.activate();

        PartialConnectionTask origin = createOutboundConnectionTask(sourceConfig, "OriginConnectionTask");
        PartialConnectionTask destination = createOutboundConnectionTask(destinationConfig, "DestinationConnectionTask");

        DeviceType reloadedDeviceType = getReloadedDeviceType(deviceType);
        assertThat(reloadedDeviceType.getDeviceConfigConflictMappings()).hasSize(2);
        verifyConflictValidation(never());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MULTIPLE_SOLUTIONS_FOR_SAME_CONFLICT + "}")
    public void createWithMultipleSolutionsForSameDataSourceTest() {
        ServerDeviceType deviceType = (ServerDeviceType) deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);

        DeviceConfiguration sourceConfig = deviceType.newConfiguration("Source").isDirectlyAddressable(true).add();
        sourceConfig.activate();

        DeviceConfiguration destinationConfig = deviceType.newConfiguration("Destination").isDirectlyAddressable(true).add();
        destinationConfig.activate();

        PartialConnectionTask origin = createOutboundConnectionTask(sourceConfig, "OriginConnectionTask");
        PartialConnectionTask destination = createOutboundConnectionTask(destinationConfig, "DestinationConnectionTask");
        // This should have created two conflicts

        // Create you own conflict
        DeviceConfigConflictMappingImpl deviceConfigConflictMapping = (DeviceConfigConflictMappingImpl) deviceType.getDeviceConfigConflictMappings().get(0);
        deviceConfigConflictMapping.newConflictingConnectionMethods(origin);
        AbstractConflictSolution solution = mock(AbstractConflictSolution.class);
        deviceConfigConflictMapping.recalculateSolvedState(solution);
    }

    private PartialScheduledConnectionTaskImpl createOutboundConnectionTask(DeviceConfiguration sourceConfig, String name) {
        PartialScheduledConnectionTaskImpl build = sourceConfig.newPartialScheduledConnectionTask(name, connectionTypePluggableClass, FIFTEEN_MINUTES, ConnectionStrategy.AS_SOON_AS_POSSIBLE).build();
        build.save();
        return build;
    }
}