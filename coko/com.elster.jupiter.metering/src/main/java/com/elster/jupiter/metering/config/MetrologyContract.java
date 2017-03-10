/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.util.HasId;

import aQute.bnd.annotation.ProviderType;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Models an API for measurement data that is calculated from
 * measurement data that is being collected by meters that
 * have been activated on usage points.
 * It is called a contract because it will list what the meter(s)
 * need to provide as input and a list of measurements
 * that will be calculated from these inputs.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-04 (11:46)
 */
@ProviderType
public interface MetrologyContract extends HasId {

    /**
     * Returns the {@link MetrologyConfiguration} that defined
     * this MetrologyContract.
     *
     * @return The MetrologyConfiguration
     */
    MetrologyConfiguration getMetrologyConfiguration();

    MetrologyContract addDeliverable(ReadingTypeDeliverable deliverable);

    void removeDeliverable(ReadingTypeDeliverable deliverable);

    /**
     * Returns the List of {@link ReadingTypeDeliverable} that is being used by this MetrologyContract.
     *
     * @return The List of ReadingTypeDeliverable
     */
    List<ReadingTypeDeliverable> getDeliverables();

    Set<ReadingTypeRequirement> getRequirements();

    MetrologyPurpose getMetrologyPurpose();

    boolean isMandatory();

    Status getStatus(UsagePoint usagePoint);

    long getVersion();

    void update();

    /**
     * @return A {@link Collection} of {@link ReadingType} {@link Set Sets}.
     * All reading types delivered in this {@link MetrologyContract} are grouped in the sets by their dependency levels,
     * i.e. how deeply their own and their underlying reading types' calculation depends on other deliverable reading types;
     * returned collection of sets guarantees order of reading type groups
     * defined by dependencies between reading type deliverables in this contract,
     * i.e. reading types in the first (zeroth) set have no dependencies,
     * and all the latter ones have at least one dependency on the previous group.
     */
    Collection<Set<ReadingType>> sortReadingTypesByDependencyLevel();

    interface Status {

        String getKey();

        String getName();

        boolean isComplete();
    }
}
