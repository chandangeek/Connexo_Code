package com.energyict.mdc.device.config;

import java.util.List;

/**
 * Defines conflicts between two different DeviceConfigurations which will not be mappable by default
 * if one would change the configuration of a device to on another.
 * Conflicting mappings will exist when:
 * <ul>
 *     <li>Multiple connectionMethods of the same type but with a different name exist</li>
 *     <li>Multiple securitySets with the same authentication- and encryptionLevel, but a different name exist</li>
 * </ul>
 */
public interface DeviceConfigConflictMapping {

    enum ConflictingMappingAction {
        ADD,
        REMOVE,
        MAP
    }

    DeviceConfiguration getOriginDeviceConfiguration();
    DeviceConfiguration getDestinationDeviceConfiguration();
    List<ConflictingConnectionMethodSolution> getConflictingConnectionMethodSolutions();
    List<ConflictingSecuritySetSolution> getConflictingSecuritySetSolutions();
    boolean isSolved();
}
