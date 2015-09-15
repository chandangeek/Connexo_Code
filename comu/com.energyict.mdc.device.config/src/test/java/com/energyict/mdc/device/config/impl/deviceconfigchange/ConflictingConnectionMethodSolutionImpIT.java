package com.energyict.mdc.device.config.impl.deviceconfigchange;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.device.config.*;
import com.energyict.mdc.device.config.impl.MessageSeeds;
import com.energyict.mdc.device.config.impl.PartialScheduledConnectionTaskImpl;
import com.energyict.mdc.device.config.impl.ServerDeviceType;
import org.fest.assertions.core.Condition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Copyrights EnergyICT
 * Date: 14.09.15
 * Time: 11:57
 */
@RunWith(MockitoJUnitRunner.class)
public class ConflictingConnectionMethodSolutionImpIT extends AbstractConflictIT{

    @Test
    @Transactional
    public void createRemoveSolutionWithoutViolationsTest() {
        ServerDeviceType deviceType = (ServerDeviceType) deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        DeviceConfiguration sourceConfig = deviceType.newConfiguration("Source").isDirectlyAddressable(true).add();
        sourceConfig.activate();

        DeviceConfiguration destinationConfig = deviceType.newConfiguration("Destination").isDirectlyAddressable(true).add();
        destinationConfig.activate();

        PartialConnectionTask origin = createOutboundConnectionTask(sourceConfig, "OriginConnectionTask");
        PartialConnectionTask destination = createOutboundConnectionTask(destinationConfig, "DestinationConnectionTask");
        DeviceConfigConflictMapping deviceConfigConflictMapping = deviceType.getDeviceConfigConflictMappings().get(0);
        deviceConfigConflictMapping.getConflictingConnectionMethodSolutions().get(0).setSolution(DeviceConfigConflictMapping.ConflictingMappingAction.REMOVE);

        DeviceType reloadedDeviceType = getReloadedDeviceType(deviceType);

        assertThat(reloadedDeviceType.getDeviceConfigConflictMappings()).haveExactly(1, new Condition<DeviceConfigConflictMapping>() {
            @Override
            public boolean matches(DeviceConfigConflictMapping deviceConfigConflictMapping) {
                ConflictingConnectionMethodSolution conflictingConnectionMethodSolution = deviceConfigConflictMapping.getConflictingConnectionMethodSolutions().get(0);
                return deviceConfigConflictMapping.isSolved() && conflictingConnectionMethodSolution.getOriginDataSource().getId() == origin.getId();
            }
        });
    }

    @Test
    @Transactional
    public void createMapSolutionWithoutViolationsTest() {
        ServerDeviceType deviceType = (ServerDeviceType) deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        DeviceConfiguration sourceConfig = deviceType.newConfiguration("Source").isDirectlyAddressable(true).add();
        sourceConfig.activate();

        DeviceConfiguration destinationConfig = deviceType.newConfiguration("Destination").isDirectlyAddressable(true).add();
        destinationConfig.activate();

        PartialConnectionTask origin = createOutboundConnectionTask(sourceConfig, "OriginConnectionTask");
        PartialConnectionTask destination = createOutboundConnectionTask(destinationConfig, "DestinationConnectionTask");
        DeviceConfigConflictMapping deviceConfigConflictMapping = deviceType.getDeviceConfigConflictMappings().get(0);
        deviceConfigConflictMapping.getConflictingConnectionMethodSolutions().get(0).setSolution(DeviceConfigConflictMapping.ConflictingMappingAction.MAP, destination);

        DeviceType reloadedDeviceType = getReloadedDeviceType(deviceType);

        assertThat(reloadedDeviceType.getDeviceConfigConflictMappings()).haveExactly(1, new Condition<DeviceConfigConflictMapping>() {
            @Override
            public boolean matches(DeviceConfigConflictMapping deviceConfigConflictMapping) {
                ConflictingConnectionMethodSolution conflictingConnectionMethodSolution = deviceConfigConflictMapping.getConflictingConnectionMethodSolutions().get(0);
                return deviceConfigConflictMapping.isSolved() &&
                        conflictingConnectionMethodSolution.getDestinationDataSource().getId() == destination.getId() &&
                        conflictingConnectionMethodSolution.getOriginDataSource().getId() == origin.getId();
            }
        });
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DESTINATION_DATA_SOURCE_IS_EMPTY + "}")
    public void createMapSolutionWithViolationsTest() {
        ServerDeviceType deviceType = (ServerDeviceType) deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        DeviceConfiguration sourceConfig = deviceType.newConfiguration("Source").isDirectlyAddressable(true).add();
        sourceConfig.activate();

        DeviceConfiguration destinationConfig = deviceType.newConfiguration("Destination").isDirectlyAddressable(true).add();
        destinationConfig.activate();

        PartialConnectionTask origin = createOutboundConnectionTask(sourceConfig, "OriginConnectionTask");
        PartialConnectionTask destination = createOutboundConnectionTask(destinationConfig, "DestinationConnectionTask");
        DeviceConfigConflictMapping deviceConfigConflictMapping = deviceType.getDeviceConfigConflictMappings().get(0);
        deviceConfigConflictMapping.getConflictingConnectionMethodSolutions().get(0).setSolution(DeviceConfigConflictMapping.ConflictingMappingAction.MAP, null);
    }

    private PartialScheduledConnectionTaskImpl createOutboundConnectionTask(DeviceConfiguration sourceConfig, String name) {
        PartialScheduledConnectionTaskImpl build = sourceConfig.newPartialScheduledConnectionTask(name, connectionTypePluggableClass, FIFTEEN_MINUTES, ConnectionStrategy.AS_SOON_AS_POSSIBLE).build();
        build.save();
        return build;
    }

}