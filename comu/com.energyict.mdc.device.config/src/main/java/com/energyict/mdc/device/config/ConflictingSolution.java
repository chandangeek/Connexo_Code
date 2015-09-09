package com.energyict.mdc.device.config;

import com.energyict.mdc.common.HasId;

/**
 * Copyrights EnergyICT
 * Date: 08.09.15
 * Time: 16:51
 */
public interface ConflictingSolution<S extends HasId> {

    DeviceConfigConflictMapping.ConflictingMappingAction getConflictingMappingAction();
    S getOriginDataSource();
    S getDestinationDataSource();

    /**
     * Set the new action on this solution. The result will automatically be saved.
     * @param action the new action
     */
    void setConflictingMappingAction(DeviceConfigConflictMapping.ConflictingMappingAction action);
}
