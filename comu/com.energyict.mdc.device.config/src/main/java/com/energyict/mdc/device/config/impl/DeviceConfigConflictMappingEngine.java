package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Calculates the DeviceConfigConflictMappings
 */
final class DeviceConfigConflictMappingEngine {

    private final DeviceType deviceType;
    private final List<DeviceConfigConflictMapping> newDeviceConfigConflictMappings = new ArrayList<>();

    public DeviceConfigConflictMappingEngine(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    List<DeviceConfigConflictMapping> calculate() {
        //TODO continue
        return deviceType.getDeviceConfigConflictMappings();
    }

    // TODO revise the fact that this is public ...
    public DeviceConfigConflictMapping calculateFor(DeviceConfiguration origin, DeviceConfiguration destination) {

        Optional<DeviceConfigConflictMapping> existingConflict =
                deviceType.getDeviceConfigConflictMappings().stream()
                        .filter(isAConflictMappingFor(origin, destination))
                        .findFirst();


        for (PartialConnectionTask originConnectionTask : origin.getPartialConnectionTasks()) {
            if (destination.getPartialConnectionTasks().stream().noneMatch(isSameConnectionTask(originConnectionTask))) {
                DeviceConfigConflictMapping deviceConfigConflictMapping = existingConflict.orElseGet(makeNewConflictMapping(origin, destination));
                Optional<ConflictingConnectionMethodSolution> existingConnectionMethodSolution = deviceConfigConflictMapping.getConflictingConnectionMethodSolutions()
                        .stream()
                        .filter(conflictingConnectionMethodSolution ->
                                conflictingConnectionMethodSolution.getOriginPartialConnectionTask().getId() == originConnectionTask.getId())
                        .findFirst();
                if (existingConnectionMethodSolution.isPresent()) {
//                    existingConnectionMethodSolution.get().
                } else {
                    //TODO create new one
                }
            }
        }

        for (PartialConnectionTask destinationConnectionTask : destination.getPartialConnectionTasks()) {
            if (origin.getPartialConnectionTasks().stream().noneMatch(isSameConnectionTask(destinationConnectionTask))) {
                // add destCT
            }
        }

        for (SecurityPropertySet originSecurityPropertySet : origin.getSecurityPropertySets()) {
            if (destination.getSecurityPropertySets().stream().noneMatch(isSameSecurityPropertySet(originSecurityPropertySet))) {
                // remove securityPropSet
            }
        }

        for (SecurityPropertySet destinationSecurityPropertySet : destination.getSecurityPropertySets()) {
            if (origin.getSecurityPropertySets().stream().noneMatch(isSameSecurityPropertySet(destinationSecurityPropertySet))) {

            }

        }


        // do a complete check of conflicts
        // then check if it exists ...
        return null;
    }

    private Supplier<DeviceConfigConflictMapping> makeNewConflictMapping(DeviceConfiguration origin, DeviceConfiguration destination) {
        return () -> ((ServerDeviceType) deviceType).newConflictMappingFor(origin, destination);
    }

    private Predicate<DeviceConfigConflictMapping> isAConflictMappingFor(DeviceConfiguration origin, DeviceConfiguration destination) {
        return deviceConfigConflictMapping ->
                deviceConfigConflictMapping.getOriginDeviceConfiguration().getId() == origin.getId() &&
                        deviceConfigConflictMapping.getDestinationDeviceConfiguration().getId() == destination.getId();
    }

    private Predicate<SecurityPropertySet> isSameSecurityPropertySet(SecurityPropertySet otherSecuritySet) {
        return securityPropertySet ->
                otherSecuritySet.getName().equals(securityPropertySet.getName())
                        && otherSecuritySet.getAuthenticationDeviceAccessLevel().equals(securityPropertySet.getAuthenticationDeviceAccessLevel())
                        && otherSecuritySet.getEncryptionDeviceAccessLevel().equals(securityPropertySet.getEncryptionDeviceAccessLevel());
    }

    private Predicate<PartialConnectionTask> isSameConnectionTask(PartialConnectionTask otherConnectionTask) {
        return connectionTask -> otherConnectionTask.getConnectionType().equals(connectionTask.getConnectionType())
                && otherConnectionTask.getName().equals(connectionTask.getName());
    }


//    class Key {
//        private final DeviceConfiguration originDeviceConfig;
//        private final DeviceConfiguration destinationDeviceConfig;
//
//        Key(DeviceConfiguration originDeviceConfig, DeviceConfiguration destinationDeviceConfig) {
//            this.originDeviceConfig = originDeviceConfig;
//            this.destinationDeviceConfig = destinationDeviceConfig;
//        }
//
//        public DeviceConfiguration getDestinationDeviceConfig() {
//            return destinationDeviceConfig;
//        }
//
//        public DeviceConfiguration getOriginDeviceConfig() {
//            return originDeviceConfig;
//        }
//
//        @Override
//        public boolean equals(Object o) {
//            if (this == o) return true;
//            if (o == null || getClass() != o.getClass()) return false;
//
//            Key key = (Key) o;
//
//            return originDeviceConfig.getId() == key.originDeviceConfig.getId() && destinationDeviceConfig.getId() != key.destinationDeviceConfig.getId();
//
//        }
//
//        @Override
//        public int hashCode() {
//            long result = originDeviceConfig.getId();
//            result = 31 * result + destinationDeviceConfig.getId();
//            return (int) result;
//        }
//    }
}
