/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl.deviceconfigchange;

import com.energyict.mdc.device.config.ConflictingConnectionMethodSolution;
import com.energyict.mdc.device.config.ConflictingSecuritySetSolution;
import com.energyict.mdc.device.config.ConflictingSolution;
import com.energyict.mdc.device.config.DeviceConfigChangeAction;
import com.energyict.mdc.device.config.DeviceConfigChangeActionType;
import com.energyict.mdc.device.config.DeviceConfigChangeEngine;
import com.energyict.mdc.device.config.DeviceConfigConflictMapping;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.impl.ServerDeviceType;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Calculates the DeviceConfigConflictMappings. For each DeviceConfigChangeAction that has the actionType 'CONFLICT' an new
 * <i>persistent</i> DeviceConfigConflictMapping will be created. The user can update these DeviceConfigConflictMappings
 * with a proper solution for the conflicts.
 */
public final class DeviceConfigConflictMappingEngine {

    public static final DeviceConfigConflictMappingEngine INSTANCE = new DeviceConfigConflictMappingEngine();

    private DeviceConfigConflictMappingEngine() {
    }

    public void reCalculateConflicts(DeviceType deviceType) {
        if (!deviceType.isDataloggerSlave()) {
            List<DeviceConfigChangeAction> deviceConfigChangeActions = DeviceConfigChangeEngine.INSTANCE.calculateDeviceConfigChangeActionsForConflicts(deviceType);

            deviceConfigChangeActions.stream()
                    .forEach(conflictingAction -> {
                        // find existing conflictMapping
                        if (conflictingAction.getActionType().equals(DeviceConfigChangeActionType.CONFLICT)) {
                            DeviceConfigConflictMappingImpl conflictMapping = getDeviceConfigConflictMapping(conflictingAction, deviceType);
                            findOrCreateConflict(conflictingAction, conflictMapping);
                        } else { // cleaunup if exists
                            Optional<DeviceConfigConflictMappingImpl> existingDeviceConfigConflictMapping = getExistingDeviceConfigConflictMapping(conflictingAction, deviceType);
                            existingDeviceConfigConflictMapping.ifPresent(removeExistingConflictIfExists(conflictingAction));
                        }
                    });
            // remove the non-conflicting
            List<DeviceConfigConflictMapping> solvedConflicts = deviceType.getDeviceConfigConflictMappings()
                    .stream()
                    .filter(areConflictsResolved(deviceConfigChangeActions))
                    .collect(Collectors.toList());
            ((ServerDeviceType) deviceType).removeDeviceConfigConflictMappings(solvedConflicts);
        }
    }

    private Consumer<DeviceConfigConflictMappingImpl> removeExistingConflictIfExists(DeviceConfigChangeAction conflictingAction) {
        return deviceConfigConflictMapping -> {
            if (isConnectionMethodAction(conflictingAction)) {
                Optional<ConflictingConnectionMethodSolution> existingConnectionMethodConflictSolution = deviceConfigConflictMapping.getConflictingConnectionMethodSolutions().stream()
                        .filter(sameOriginConflict(conflictingAction)).findFirst();
                existingConnectionMethodConflictSolution.ifPresent(deviceConfigConflictMapping::removeConnectionMethodSolution);
            } else if (isSecurityPropertySetAction(conflictingAction)) {
                Optional<ConflictingSecuritySetSolution> existingSecurityPropertySetConflictSolution = deviceConfigConflictMapping.getConflictingSecuritySetSolutions().stream()
                        .filter(sameOriginConflict(conflictingAction)).findFirst();
                existingSecurityPropertySetConflictSolution.ifPresent(deviceConfigConflictMapping::removeSecuritySetSolution);
            }
        };
    }

    private void findOrCreateConflict(DeviceConfigChangeAction conflictingAction, DeviceConfigConflictMappingImpl conflictMapping) {
        if (isConnectionMethodAction(conflictingAction)) {
            // do connectionTask stuff
            findOrCreateConflictingConnectionMethodSolution(conflictingAction, conflictMapping);
        } else if (isSecurityPropertySetAction(conflictingAction)) {
            // do securityPropertySet stuff
            findOrCreateConflictingSecuritySetSolution(conflictingAction, conflictMapping);
        }
    }

    private void findOrCreateConflictingSecuritySetSolution(DeviceConfigChangeAction conflictingAction, DeviceConfigConflictMappingImpl conflictMapping) {
        // find existing solution
        Optional<ConflictingSecuritySetSolution> existingSecurityPropertySetConflictSolution = conflictMapping.getConflictingSecuritySetSolutions().stream()
                .filter(sameOriginConflict(conflictingAction)).findFirst();
        // if not exists, create it
        if (!existingSecurityPropertySetConflictSolution.isPresent()) {
            conflictMapping.newConflictingSecurityPropertySets(((SecurityPropertySet) conflictingAction.getOrigin()));
        }
    }

    private void findOrCreateConflictingConnectionMethodSolution(DeviceConfigChangeAction conflictingAction, DeviceConfigConflictMappingImpl conflictMapping) {
        // find existing solution
        Optional<ConflictingConnectionMethodSolution> existingConnectionMethodConflictSolution = conflictMapping.getConflictingConnectionMethodSolutions().stream()
                .filter(sameOriginConflict(conflictingAction)).findFirst();
        // if not exists, create it
        if (!existingConnectionMethodConflictSolution.isPresent()) {
            conflictMapping.newConflictingConnectionMethods(((PartialConnectionTask) conflictingAction.getOrigin()));
        }
    }

    private boolean isConnectionMethodAction(DeviceConfigChangeAction conflictingAction) {
        return (conflictingAction.getOrigin() != null && conflictingAction.getOrigin() instanceof PartialConnectionTask)
                || (conflictingAction.getDestination() != null && conflictingAction.getDestination() instanceof PartialConnectionTask);
    }

    private boolean isSecurityPropertySetAction(DeviceConfigChangeAction conflictingAction) {
        return (conflictingAction.getOrigin() != null && conflictingAction.getOrigin() instanceof SecurityPropertySet)
                || (conflictingAction.getDestination() != null && conflictingAction.getDestination() instanceof SecurityPropertySet);
    }

    private Predicate<DeviceConfigConflictMapping> areConflictsResolved(List<DeviceConfigChangeAction> deviceConfigChangeActions) {
        return deviceConfigConflictMapping -> {

            // look if there is till a deviceConfigChangeAction for the given deviceConfigConflictMapping
            Optional<DeviceConfigChangeAction> deviceConfigChangeAction = deviceConfigChangeActions.stream().filter(configChangeAction ->
                    configChangeAction.getOriginDeviceConfiguration().getId() == deviceConfigConflictMapping.getOriginDeviceConfiguration().getId()
                            && configChangeAction.getDestinationDeviceConfiguration().getId() == deviceConfigConflictMapping.getDestinationDeviceConfiguration().getId())
                    .findFirst();
            // if there is none, then the conflicts are solved ...
            return !deviceConfigChangeAction.isPresent() || deviceConfigConflictMapping.getConflictingConnectionMethodSolutions().size() == 0 && deviceConfigConflictMapping.getConflictingSecuritySetSolutions().size() == 0;
        };

    }

    private DeviceConfigConflictMappingImpl getDeviceConfigConflictMapping(DeviceConfigChangeAction configChangeAction, DeviceType deviceType) {
        return getExistingDeviceConfigConflictMapping(configChangeAction, deviceType)
                .orElseGet(makeNewConflictMapping(configChangeAction.getOriginDeviceConfiguration(), configChangeAction.getDestinationDeviceConfiguration(), ((ServerDeviceType) deviceType)));
    }

    private Optional<DeviceConfigConflictMappingImpl> getExistingDeviceConfigConflictMapping(DeviceConfigChangeAction configChangeAction, DeviceType deviceType) {
        return deviceType.getDeviceConfigConflictMappings().stream()
                .filter(getMatchingDeviceConfigConflictMapping(configChangeAction)).map(deviceConfigConflictMapping -> ((DeviceConfigConflictMappingImpl) deviceConfigConflictMapping)).findFirst();
    }

    private Predicate<DeviceConfigConflictMapping> getMatchingDeviceConfigConflictMapping(DeviceConfigChangeAction conflictingAction) {
        return deviceConfigConflictMapping -> deviceConfigConflictMapping.getOriginDeviceConfiguration().getId() == conflictingAction.getOriginDeviceConfiguration().getId()
                && deviceConfigConflictMapping.getDestinationDeviceConfiguration().getId() == conflictingAction.getDestinationDeviceConfiguration().getId();
    }

    private Predicate<ConflictingSolution> sameOriginConflict(DeviceConfigChangeAction<?> configChangeAction) {
        return conflictingSolution -> configChangeAction.getOrigin() != null && conflictingSolution.getOriginDataSource().getId() == configChangeAction.getOrigin().getId();
    }

    private Supplier<DeviceConfigConflictMappingImpl> makeNewConflictMapping(DeviceConfiguration origin, DeviceConfiguration destination, ServerDeviceType serverDeviceType) {
        return () -> serverDeviceType.newConflictMappingFor(origin, destination);
    }
}
