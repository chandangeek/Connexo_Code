/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

import com.elster.jupiter.util.HasId;

import java.util.List;

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
