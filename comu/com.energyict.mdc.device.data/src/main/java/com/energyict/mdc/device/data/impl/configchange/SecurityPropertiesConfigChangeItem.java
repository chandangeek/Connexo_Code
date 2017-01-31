/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.configchange;

import com.energyict.mdc.device.config.ConflictingSecuritySetSolution;
import com.energyict.mdc.device.config.DeviceConfigChangeAction;
import com.energyict.mdc.device.config.DeviceConfigChangeEngine;
import com.energyict.mdc.device.config.DeviceConfigConflictMapping;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.SecurityPropertySet;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Applies changes to the SecurityProperties of the Device
 */
public class SecurityPropertiesConfigChangeItem extends AbstractConfigChangeItem {

    private static final SecurityPropertiesConfigChangeItem INSTANCE = new SecurityPropertiesConfigChangeItem();

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

        List<DeviceConfigChangeAction<SecurityPropertySet>> securitySetActions = DeviceConfigChangeEngine.INSTANCE.getSecuritySetConfigChangeActions(originDeviceConfiguration, destinationDeviceConfiguration);
        List<DeviceConfigChangeAction<SecurityPropertySet>> matchedSecuritySets = getMatchItems(securitySetActions);
        List<SecurityPropertySet> removeItems = getRemoveItems(securitySetActions);
        matchedSecuritySets.stream().forEach(matchedSecurityPropertySet -> device.updateSecurityProperties(matchedSecurityPropertySet.getOrigin(), matchedSecurityPropertySet.getDestination()));
        removeItems.forEach(device::deleteSecurityPropertiesFor);
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
