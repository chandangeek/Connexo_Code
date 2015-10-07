package com.energyict.mdc.device.config;

import com.energyict.mdc.common.HasId;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 08.09.15
 * Time: 16:51
 */
public interface ConflictingSolution<S extends HasId> {

    DeviceConfigConflictMapping.ConflictingMappingAction getConflictingMappingAction();

    S getOriginDataSource();

    List<S> getMappableToDataSources();

    S getDestinationDataSource();

    /**
     * Set the new action on this solution. The result will automatically be saved.
     *
     */
    void markSolutionAsRemove();

    /**
     * Set the new action on this solution. The result will automatically be saved.
     * An additional DataSource can be set in case of a <i>map</i> action
     *
     * @param dataSource the destination DataSource to map
     */
    void markSolutionAsMap(S dataSource);
}
