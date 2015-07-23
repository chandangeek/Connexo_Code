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
    private final List<DeviceConfigConflictMapping> newDeviceConfigConflictMappings = new ArrayList<>();

    public DeviceConfigConflictMappingEngine(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    void reCalculateConflicts() {
        DeviceConfigChangeEngine deviceConfigChangeEngine = new DeviceConfigChangeEngine(deviceType);
        deviceConfigChangeEngine.calculateConfigChangeActions();
        List<DeviceConfigChangeAction> deviceConfigChangeActions = deviceConfigChangeEngine.getDeviceConfigChangeActions();

        deviceConfigChangeActions.stream()
//                .filter(deviceConfigChangeAction ->
//                deviceConfigChangeAction.getActionType().equals(DeviceConfigChangeActionType.CONFLICT))
                .forEach(conflictingAction -> {

                    // find existing conflictMapping
                    if(conflictingAction.getActionType().equals(DeviceConfigChangeActionType.CONFLICT)){
                        DeviceConfigConflictMapping conflictMapping = getDeviceConfigConflictMapping(conflictingAction);

                        if (isConnectionMethodAction(conflictingAction)) {
                            // do connectionTask stuff
                            // find existing solution
                            Optional<ConflictingConnectionMethodSolution> existingConnectionMethodConflictSolution = conflictMapping.getConflictingConnectionMethodSolutions().stream()
                                    .filter(sameOriginConnectionMethodConflict(conflictingAction)).findFirst();
                            if (!existingConnectionMethodConflictSolution.isPresent()) {
                                // TODO check to reduce the casts
                                conflictMapping.newConflictingConnectionMethods(((PartialConnectionTask) conflictingAction.getOrigin()), ((PartialConnectionTask) conflictingAction.getDestination()));
                            }
                        } else if (isSecurityPropertySetAction(conflictingAction)) {
                            // do securityPropertySet stuff
                            // find existing solution
                            Optional<ConflictingSecuritySetSolution> existingSecurityPropertySetConflictSolution = conflictMapping.getConflictingSecuritySetSolutions().stream()
                                    .filter(sameOriginSecurityPropertySetConflict(conflictingAction)).findFirst();
                            if (!existingSecurityPropertySetConflictSolution.isPresent()) {
                                // TODO check to reduce the casts
                                conflictMapping.newConflictingSecurityPropertySets(((SecurityPropertySet) conflictingAction.getOrigin()), ((SecurityPropertySet) conflictingAction.getDestination()));
                            }
                        }
                    } else { // cleaunup if exists
                        Optional<DeviceConfigConflictMapping> existingDeviceConfigConflictMapping = getExistingDeviceConfigConflictMapping(conflictingAction);
                        existingDeviceConfigConflictMapping.ifPresent(deviceConfigConflictMapping -> {
                            if(isConnectionMethodAction(conflictingAction)){
                                Optional<ConflictingConnectionMethodSolution> existingConnectionMethodConflictSolution = deviceConfigConflictMapping.getConflictingConnectionMethodSolutions().stream()
                                        .filter(sameOriginConnectionMethodConflict(conflictingAction)).findFirst();
                                existingConnectionMethodConflictSolution.ifPresent(deviceConfigConflictMapping::removeConnectionMethodSolution);
                            } else if(isSecurityPropertySetAction(conflictingAction)){
                                Optional<ConflictingSecuritySetSolution> existingSecurityPropertySetConflictSolution = deviceConfigConflictMapping.getConflictingSecuritySetSolutions().stream()
                                        .filter(sameOriginSecurityPropertySetConflict(conflictingAction)).findFirst();
                                existingSecurityPropertySetConflictSolution.ifPresent(deviceConfigConflictMapping::removeSecuritySetSolution);
                            }
                        });
                    }

                });

        // remove the non-conflicting
        List<DeviceConfigConflictMapping> solvedConflicts = deviceType.getDeviceConfigConflictMappings().stream().filter(areConflictsResolved()).collect(Collectors.toList());
        deviceType.getDeviceConfigConflictMappings().removeAll(solvedConflicts);
    }

    private boolean isConnectionMethodAction(DeviceConfigChangeAction conflictingAction) {
        return (conflictingAction.getOrigin() != null && conflictingAction.getOrigin() instanceof PartialConnectionTask)
                || (conflictingAction.getDestination() != null && conflictingAction.getDestination() instanceof PartialConnectionTask);
    }

    private boolean isSecurityPropertySetAction(DeviceConfigChangeAction conflictingAction) {
        return (conflictingAction.getOrigin() != null && conflictingAction.getOrigin() instanceof SecurityPropertySet)
                || (conflictingAction.getDestination() != null && conflictingAction.getDestination() instanceof SecurityPropertySet);
    }

    private Predicate<DeviceConfigConflictMapping> areConflictsResolved() {
        return deviceConfigConflictMapping -> deviceConfigConflictMapping.getConflictingConnectionMethodSolutions().size() == 0 &&
                deviceConfigConflictMapping.getConflictingSecuritySetSolutions().size() == 0;
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

    private Predicate<DeviceConfigChangeAction> getMatchingDeviceConfigConflictMapping(DeviceConfigConflictMapping deviceConfigConflictMapping) {
        return deviceConfigChangeAction -> deviceConfigChangeAction.getOriginDeviceConfiguration().getId() == deviceConfigConflictMapping.getOriginDeviceConfiguration().getId()
                && deviceConfigChangeAction.getDestinationDeviceConfiguration().getId() == deviceConfigConflictMapping.getDestinationDeviceConfiguration().getId();
    }

    private Predicate<ConflictingConnectionMethodSolution> sameOriginConnectionMethodConflict(DeviceConfigChangeAction conflictingAction) {
        return conflictingConnectionMethodSolution -> conflictingConnectionMethodSolution.getOriginPartialConnectionTask().getId() == conflictingAction.getOrigin().getId();
    }

    private Predicate<ConflictingSecuritySetSolution> sameOriginSecurityPropertySetConflict(DeviceConfigChangeAction conflictingAction) {
        return conflictingSecuritySetSolution -> conflictingSecuritySetSolution.getOriginSecurityPropertySet().getId() == conflictingAction.getOrigin().getId();
    }

    private Supplier<DeviceConfigConflictMapping> makeNewConflictMapping(DeviceConfiguration origin, DeviceConfiguration destination) {
        return () -> ((ServerDeviceType) deviceType).newConflictMappingFor(origin, destination);
    }
}
