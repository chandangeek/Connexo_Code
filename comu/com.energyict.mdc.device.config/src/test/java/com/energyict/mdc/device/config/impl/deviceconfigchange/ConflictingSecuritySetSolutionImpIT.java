package com.energyict.mdc.device.config.impl.deviceconfigchange;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.device.config.*;
import com.energyict.mdc.device.config.impl.MessageSeeds;
import com.energyict.mdc.device.config.impl.ServerDeviceType;
import org.fest.assertions.core.Condition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Copyrights EnergyICT
 * Date: 15.09.15
 * Time: 10:14
 */
@RunWith(MockitoJUnitRunner.class)
public class ConflictingSecuritySetSolutionImpIT extends AbstractConflictIT{

    @Test
    @Transactional
    public void createRemoveSolutionWithoutViolationsTest() {
        ServerDeviceType deviceType = (ServerDeviceType) deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        DeviceConfiguration sourceConfig = deviceType.newConfiguration("Source").isDirectlyAddressable(true).add();
        sourceConfig.activate();

        DeviceConfiguration destinationConfig = deviceType.newConfiguration("Destination").isDirectlyAddressable(true).add();
        destinationConfig.activate();

        SecurityPropertySet origin = createSecurityPropertySet(sourceConfig, "OriginSecurityPropSet");
        SecurityPropertySet destination = createSecurityPropertySet(destinationConfig, "DestinationSecurityPropSet");
        DeviceConfigConflictMapping deviceConfigConflictMapping = deviceType.getDeviceConfigConflictMappings().get(0);
        deviceConfigConflictMapping.getConflictingSecuritySetSolutions().get(0).markSolutionAsRemove();

        DeviceType reloadedDeviceType = getReloadedDeviceType(deviceType);

        assertThat(reloadedDeviceType.getDeviceConfigConflictMappings()).haveExactly(1, new Condition<DeviceConfigConflictMapping>() {
            @Override
            public boolean matches(DeviceConfigConflictMapping deviceConfigConflictMapping) {
                ConflictingSecuritySetSolution conflictingSecuritySetSolution = deviceConfigConflictMapping.getConflictingSecuritySetSolutions().get(0);
                return deviceConfigConflictMapping.isSolved() && conflictingSecuritySetSolution.getOriginDataSource().getId() == origin.getId();
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

        SecurityPropertySet origin = createSecurityPropertySet(sourceConfig, "OriginSecurityPropSet");
        SecurityPropertySet destination = createSecurityPropertySet(destinationConfig, "DestinationSecurityPropSet");
        DeviceConfigConflictMapping deviceConfigConflictMapping = deviceType.getDeviceConfigConflictMappings().get(0);
        deviceConfigConflictMapping.getConflictingSecuritySetSolutions().get(0).markSolutionAsMap(destination);

        DeviceType reloadedDeviceType = getReloadedDeviceType(deviceType);

        assertThat(reloadedDeviceType.getDeviceConfigConflictMappings()).haveExactly(1, new Condition<DeviceConfigConflictMapping>() {
            @Override
            public boolean matches(DeviceConfigConflictMapping deviceConfigConflictMapping) {
                ConflictingSecuritySetSolution conflictingSecuritySetSolution = deviceConfigConflictMapping.getConflictingSecuritySetSolutions().get(0);
                return deviceConfigConflictMapping.isSolved() &&
                        conflictingSecuritySetSolution.getDestinationDataSource().getId() == destination.getId() &&
                        conflictingSecuritySetSolution.getOriginDataSource().getId() == origin.getId();
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

        SecurityPropertySet origin = createSecurityPropertySet(sourceConfig, "OriginSecurityPropSet");
        SecurityPropertySet destination = createSecurityPropertySet(destinationConfig, "DestinationSecurityPropSet");
        DeviceConfigConflictMapping deviceConfigConflictMapping = deviceType.getDeviceConfigConflictMappings().get(0);
        deviceConfigConflictMapping.getConflictingSecuritySetSolutions().get(0).markSolutionAsMap(null);
    }

    private SecurityPropertySet createSecurityPropertySet(DeviceConfiguration sourceConfig, String name) {
        SecurityPropertySet securityPropertySet = sourceConfig.createSecurityPropertySet(name).authenticationLevel(-1).encryptionLevel(-1).build();
        securityPropertySet.update();
        return securityPropertySet;
    }

}
