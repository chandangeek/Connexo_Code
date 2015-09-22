package com.energyict.mdc.device.data.impl.configchange;

import com.energyict.mdc.device.config.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Applies changes to the SecurityProperties of the Device
 */
public class SecurityPropertiesConfigChangeItem extends AbstractConfigChangeItem implements DataSourceConfigChangeItem {

    private static SecurityPropertiesConfigChangeItem INSTANCE = new SecurityPropertiesConfigChangeItem();

    private SecurityPropertiesConfigChangeItem() {
    }

    static DataSourceConfigChangeItem getInstance() {
        return INSTANCE;
    }

    @Override
    public void apply(ServerDeviceForConfigChange device, DeviceConfiguration originDeviceConfiguration, DeviceConfiguration destinationDeviceConfiguration) {
        final Optional<DeviceConfigConflictMapping> conflict = getDeviceConfigConflictMapping(device, originDeviceConfiguration, destinationDeviceConfiguration);

        conflict.ifPresent(deviceConfigConflictMapping -> {
            removeSecurityPropertiesFromDevice(device, deviceConfigConflictMapping);
            mapSecurityPropertiesFromDevice(device, deviceConfigConflictMapping);
        });

        final List<DeviceConfigChangeAction<SecurityPropertySet>> matchedSecuritySets = DeviceConfigChangeEngine.INSTANCE.getSecuritySetConfigChangeActions(originDeviceConfiguration, destinationDeviceConfiguration).stream().filter(actionTypeIs(DeviceConfigChangeActionType.MATCH)).collect(Collectors.toList());
        matchedSecuritySets.stream().forEach(matchedSecurityPropertySet -> device.updateSecurityProperties(matchedSecurityPropertySet.getOrigin(), matchedSecurityPropertySet.getDestination()));
    }

    private void removeSecurityPropertiesFromDevice(ServerDeviceForConfigChange device, DeviceConfigConflictMapping deviceConfigConflictMapping) {
        final List<ConflictingSecuritySetSolution> solutionsToRemove = deviceConfigConflictMapping.getConflictingSecuritySetSolutions().stream()
                .filter(solutionsForRemove()).collect(Collectors.toList());
        solutionsToRemove.stream().forEach(conflictingSecuritySetSolution -> device.deleteSecurityPropertiesFor(conflictingSecuritySetSolution.getOriginDataSource()));
    }

    private void mapSecurityPropertiesFromDevice(ServerDeviceForConfigChange device, DeviceConfigConflictMapping deviceConfigConflictMapping) {
        final List<ConflictingSecuritySetSolution> conflictsToMap = deviceConfigConflictMapping.getConflictingSecuritySetSolutions().stream()
                .filter(solutionsForMap()).collect(Collectors.toList());
        conflictsToMap.stream().forEach(conflictingSecuritySetSolution -> device.updateSecurityProperties(conflictingSecuritySetSolution.getOriginDataSource(), conflictingSecuritySetSolution.getDestinationDataSource()));
    }
}
