package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.*;
import com.energyict.mdc.device.config.impl.deviceconfigchange.DeviceConfigChangeAction;
import com.energyict.mdc.device.config.impl.deviceconfigchange.DeviceConfigChangeActionType;
import com.energyict.mdc.device.config.impl.deviceconfigchange.DeviceConfigChangeEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Calculates the DeviceConfigConflictMappings
 */
final class DeviceConfigConflictMappingEngine {

    private final DeviceType deviceType;

    public DeviceConfigConflictMappingEngine(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    void reCalculateConflicts() {
        DeviceConfigChangeEngine deviceConfigChangeEngine = new DeviceConfigChangeEngine(deviceType);
        deviceConfigChangeEngine.calculateConfigChangeActions();
        List<DeviceConfigChangeAction> deviceConfigChangeActions = deviceConfigChangeEngine.getDeviceConfigChangeActions();

        deviceConfigChangeActions.stream()
                .forEach(conflictingAction -> {

                    // find existing conflictMapping
                    if (conflictingAction.getActionType().equals(DeviceConfigChangeActionType.CONFLICT)) {
                        DeviceConfigConflictMapping conflictMapping = getDeviceConfigConflictMapping(conflictingAction);

                        if (isConnectionMethodAction(conflictingAction)) {
                            // do connectionTask stuff
                            //TODO do same for securitypropertyset stuff
                            findOrCreateConflictingConnectionMethodSolution(conflictingAction, conflictMapping);
                        } else if (isSecurityPropertySetAction(conflictingAction)) {
                            // do securityPropertySet stuff
                            // find existing solution
                            Optional<ConflictingSecuritySetSolution> existingSecurityPropertySetConflictSolution = conflictMapping.getConflictingSecuritySetSolutions().stream()
                                    .filter(sameOriginConflict(conflictingAction)).findFirst();
                            if (!existingSecurityPropertySetConflictSolution.isPresent()) {
                                conflictMapping.newConflictingSecurityPropertySets(((SecurityPropertySet) conflictingAction.getOrigin()), ((SecurityPropertySet) conflictingAction.getDestination()));
                            }
                        }
                    } else { // cleaunup if exists
                        Optional<DeviceConfigConflictMapping> existingDeviceConfigConflictMapping = getExistingDeviceConfigConflictMapping(conflictingAction);
                        existingDeviceConfigConflictMapping.ifPresent(deviceConfigConflictMapping -> {
                            if (isConnectionMethodAction(conflictingAction)) {
                                Optional<ConflictingConnectionMethodSolution> existingConnectionMethodConflictSolution = deviceConfigConflictMapping.getConflictingConnectionMethodSolutions().stream()
                                        .filter(sameOriginConflict(conflictingAction)).findFirst();
                                existingConnectionMethodConflictSolution.ifPresent(deviceConfigConflictMapping::removeConnectionMethodSolution);
                            } else if (isSecurityPropertySetAction(conflictingAction)) {
                                Optional<ConflictingSecuritySetSolution> existingSecurityPropertySetConflictSolution = deviceConfigConflictMapping.getConflictingSecuritySetSolutions().stream()
                                        .filter(sameOriginConflict(conflictingAction)).findFirst();
                                existingSecurityPropertySetConflictSolution.ifPresent(deviceConfigConflictMapping::removeSecuritySetSolution);
                            }
                        });
                    }

                });

        // remove the non-conflicting
        List<DeviceConfigConflictMapping> solvedConflicts = deviceType.getDeviceConfigConflictMappings().stream().filter(areConflictsResolved(deviceConfigChangeActions)).collect(Collectors.toList());
        deviceType.getDeviceConfigConflictMappings().removeAll(solvedConflicts);
    }

    private void findOrCreateConflictingConnectionMethodSolution(DeviceConfigChangeAction conflictingAction, DeviceConfigConflictMapping conflictMapping) {
        // find existing solution
        Optional<ConflictingConnectionMethodSolution> existingConnectionMethodConflictSolution = conflictMapping.getConflictingConnectionMethodSolutions().stream()
                .filter(sameOriginConflict(conflictingAction)).findFirst();
        // if not exists, create it
        if (!existingConnectionMethodConflictSolution.isPresent()) {
            conflictMapping.newConflictingConnectionMethods(((PartialConnectionTask) conflictingAction.getOrigin()), ((PartialConnectionTask) conflictingAction.getDestination()));
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

    private DeviceConfigConflictMapping getDeviceConfigConflictMapping(DeviceConfigChangeAction configChangeAction) {
        return getExistingDeviceConfigConflictMapping(configChangeAction)
                .orElseGet(makeNewConflictMapping(configChangeAction.getOriginDeviceConfiguration(), configChangeAction.getDestinationDeviceConfiguration()));
    }

    private Optional<DeviceConfigConflictMapping> getExistingDeviceConfigConflictMapping(DeviceConfigChangeAction configChangeAction){
        return deviceType.getDeviceConfigConflictMappings().stream()
                .filter(getMatchingDeviceConfigConflictMapping(configChangeAction)).findFirst();
    }

    private Predicate<DeviceConfigConflictMapping> getMatchingDeviceConfigConflictMapping(DeviceConfigChangeAction conflictingAction) {
        return deviceConfigConflictMapping -> deviceConfigConflictMapping.getOriginDeviceConfiguration().getId() == conflictingAction.getOriginDeviceConfiguration().getId()
                && deviceConfigConflictMapping.getDestinationDeviceConfiguration().getId() == conflictingAction.getDestinationDeviceConfiguration().getId();
    }

    private Predicate<ConflictingSolution> sameOriginConflict(DeviceConfigChangeAction<?> configChangeAction){
        return conflictingSolution -> configChangeAction.getOrigin() != null && conflictingSolution.getOriginDataSource().getId() == configChangeAction.getOrigin().getId();
    }

    private Supplier<DeviceConfigConflictMapping> makeNewConflictMapping(DeviceConfiguration origin, DeviceConfiguration destination) {
        return () -> ((ServerDeviceType) deviceType).newConflictMappingFor(origin, destination);
    }
}
