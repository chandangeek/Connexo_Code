package com.energyict.mdc.device.config;

/**
 * Defines a solution for mapping a conflicting ConnectionMethod.
 * A solution will either be to
 * <ul>
 *     <li>Remove the origin ConnectionMethod</li>
 *     <li>Add the destination ConnectionMethod</li>
 *     <li>Map the origin ConnectionMethod to the destination ConnectionMethod</li>
 * </ul>
 */
public interface ConflictingConnectionMethodSolution {

    DeviceConfigConflictMapping.ConflictingMappingAction getConflictingMappingAction();
    PartialConnectionTask getOriginPartialConnectionTask();
    PartialConnectionTask getDestinationPartialConnectionTask();
}
