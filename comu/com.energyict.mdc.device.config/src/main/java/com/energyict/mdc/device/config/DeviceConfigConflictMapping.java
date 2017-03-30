/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

import com.elster.jupiter.util.HasId;

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
public interface DeviceConfigConflictMapping extends HasId {
    enum ConflictingMappingAction {
        REMOVE,
        MAP,
        NOT_DETERMINED_YET
    }

    DeviceConfiguration getOriginDeviceConfiguration();
    DeviceConfiguration getDestinationDeviceConfiguration();
    List<ConflictingConnectionMethodSolution> getConflictingConnectionMethodSolutions();
    List<ConflictingSecuritySetSolution> getConflictingSecuritySetSolutions();
    DeviceType getDeviceType();

    boolean isSolved();
    long getVersion();
}
