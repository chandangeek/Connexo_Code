/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.kpi;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.tasks.RecurrentTask;
import com.energyict.mdc.device.data.Device;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Optional;

@ProviderType
public interface RegisteredDevicesKpiService {

    RegisteredDevicesKpiBuilder newRegisteredDevicesKpi(EndDeviceGroup endDeviceGroup);

    /**
     * Finds all {@link RegisteredDevicesKpi} that are defined in the system.
     *
     * @return The List of RegisteredDevicesKpi
     */
    List<RegisteredDevicesKpi> findAllRegisteredDevicesKpis();

    /**
     * Finds all {@link RegisteredDevicesKpi} that are defined in the system through a Finder
     * implementation, allowing paged searching and sorting
     *
     * @return Finder for RegisteredDevicesKpi
     */
    Finder<RegisteredDevicesKpi> registeredDevicesKpiFinder();

    /**
     * Finds the {@link RegisteredDevicesKpi} with the specified id.
     *
     * @param id The unique identifier
     * @return an Optional RegisteredDevicesKpi
     */
    Optional<RegisteredDevicesKpi> findRegisteredDevicesKpi(long id);

    /**
     * Finds the {@link RegisteredDevicesKpi} with the specified id.
     *
     * @param id The unique recurrent task identifier
     * @return an Optional RegisteredDevicesKpi
     */
    Optional<RegisteredDevicesKpi> findRegisteredDevicesKpiByRecurrentTask(long id);


    /**
     * Finds the {@link RegisteredDevicesKpi} with the specified id and version.
     *
     * @param id The unique identifier
     * @param version The version
     * @return an optional RegisteredDevicesKpi
     * @throws java.util.ConcurrentModificationException if the object is currently locked in the database
     */
    Optional<RegisteredDevicesKpi> findAndLockRegisteredDevicesKpiByIdAndVersion(long id, long version);

    /**
     * Finds the {@link RegisteredDevicesKpi} for the specified {@link EndDeviceGroup}.
     *
     * @param group The QueryEndDeviceGroup
     * @return The RegisteredDevicesKpi
     */
    Optional<RegisteredDevicesKpi> findRegisteredDevicesKpi(EndDeviceGroup group);

    /**
     * Gets the scores for a gateway for a certain interval and a certain frequency
     * @param gateway the gateway to find the scores for
     * @param interval the interval to find the scores for
     * @param frequency the frequency you want to see the scores in
     * @return a list of {@link RegisteredDevicesKpiScore}
     */
    List<RegisteredDevicesKpiScore> getScores(Device gateway, Range<Instant> interval, RegisteredDevicesKpiFrequency frequency);

    List<RegisteredDevicesKpi> findByDeviceGroup(EndDeviceGroup endDevice, int skip, int limit);

    interface RegisteredDevicesKpiBuilder {

        /**
         * The Kpi's calculation frequency. This field is transient, serves mainly as a validation field.
         *
         * @param temporalAmount The amount of time between each calculation of the communication task execution KPI
         * @return The RegisteredDevicesKpiBuilder
         */
        RegisteredDevicesKpiBuilder frequency(TemporalAmount temporalAmount);

        /**
         * Specifies that the RegisteredDevicesKpiBuilder that is under construction
         * will use the given number as target
         *
         * @return The RegisteredDevicesKpiBuilder
         */
        RegisteredDevicesKpiBuilder target(long target);

        /**
         * Specifies that the RegisteredDevicesKpiBuilder that is under construction
         * will trigger the given recurrent tasks
         *
         * @return The RegisteredDevicesKpiBuilder
         */
        RegisteredDevicesKpiBuilder setNextRecurrentTasks(List<RecurrentTask> nextRecurrentTasks);

        /**
         * Completes the builder and returns the newly
         * created RegisteredDevicesKpi from the specifications
         * that were provided earlier in the build process.
         *
         * @return The newly created RegisteredDevicesKpi
         */
        RegisteredDevicesKpi save();

    }
}
