package com.elster.jupiter.metering.config;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

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
public interface MetrologyContract {

    /**
     * Returns the {@link MetrologyConfiguration} that defined
     * this MetrologyContract.
     *
     * @return The MetrologyConfiguration
     */
    MetrologyConfiguration getMetrologyConfiguration();

    /**
     * Returns the List of {@link ReadingTypeDeliverable} that is being used by this MetrologyContract.
     *
     * @return The List of ReadingTypeDeliverable
     */
    List<ReadingTypeDeliverable> getDeliverables();

}