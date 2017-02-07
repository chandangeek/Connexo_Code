/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.kpi;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.time.TimeDuration;

import aQute.bnd.annotation.ProviderType;

import java.math.BigDecimal;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Optional;

/**
 * Provides services to define and calculate key performance
 * indicators of the data collection engine.
 * Such KPIs are defined against a group of {@link com.energyict.mdc.device.data.Device}s.
 * The contents of the group, i.e. the Devices that are contained in the group
 * are in fact a set of conditions against Device attributes
 * and are provided by an {@link com.elster.jupiter.metering.groups.EndDeviceGroup}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-06 (11:30)
 */
@ProviderType
public interface DataCollectionKpiService {

    DataCollectionKpiBuilder newDataCollectionKpi(EndDeviceGroup group);

    /**
     * Finds all {@link DataCollectionKpi} that are defined in the system.
     *
     * @return The List of DataCollectionKpi
     */
    List<DataCollectionKpi> findAllDataCollectionKpis();

    /**
     * Finds all {@link DataCollectionKpi} that are defined in the system through a Finder
     * implementation, allowing paged searching and sorting
     *
     * @return Finder for DataCollectionKpi
     */
    Finder<DataCollectionKpi> dataCollectionKpiFinder();

    /**
     * Finds the {@link DataCollectionKpi} with the specified id.
     *
     * @param id The unique identifier
     * @return The DataCollectionKpi
     */
    public Optional<DataCollectionKpi> findDataCollectionKpi(long id);

    Optional<DataCollectionKpi> findAndLockDataCollectionKpiByIdAndVersion(long id, long version);

    /**
     * Finds the {@link DataCollectionKpi} for the specified {@link EndDeviceGroup}.
     *
     * @param group The QueryEndDeviceGroup
     * @return The DataCollectionKpi
     */
    Optional<DataCollectionKpi> findDataCollectionKpi(EndDeviceGroup group);

    /**
     * Models behavior to build a new {@link DataCollectionKpi}.
     */
    interface DataCollectionKpiBuilder {

        /**
         * The Kpi's calculation frequency. This field is transient, serves mainly as a validation field.
         *
         * @param temporalAmount The amount of time between each calculation of the communication task execution KPI
         * @return The KpiTargetBuilder
         */
        DataCollectionKpiBuilder frequency(TemporalAmount temporalAmount);
        /**
         * The displayPeriod is the period for which the KPI will be displayed in the UI. So display period will be
         * fixed for the moment.
         *
         * @param displayPeriod The period to be used for display, that is, the period over which KPI data will be displayed in the graph
         * @return the current builder, augmented with displayPeriod.
         */
        DataCollectionKpiBuilder displayPeriod(TimeDuration displayPeriod);
        /**
         * Specifies that the DataCollectionKpi that is under construction
         * will calculate the connection setup KPI and will do that e.g. every day.
         * The returned KpiTargetBuilder allows to define targets.
         *
         * @return The KpiTargetBuilder
         */
        KpiTargetBuilder calculateConnectionSetupKpi();

        /**
         * Specifies that the DataCollectionKpi that is under construction
         * will calculate the communication task execution KPI and will do that e.g. every day.
         * The returned KpiTargetBuilder allows to define targets.
         *
         */
        KpiTargetBuilder calculateComTaskExecutionKpi();

        /**
         * Completes the builder and returns the newly
         * created DataCollectionKpi from the specifications
         * that were provided earlier in the build process.
         *
         * @return The newly created DataCollectionKpi
         */
        DataCollectionKpi save();

    }

    interface KpiTargetBuilder {

        /**
         * Uses the specified value as a fixed minimum target value,
         * i.e. actual values are expected to be equal to or higher.
         */
        void expectingAsMinimum(BigDecimal target);

        /**
         * Uses the specified value as a fixed maximum target value,
         * i.e. actual values are expected to be equal to or lower.
         */
        void expectingAsMaximum(BigDecimal target);

    }

}