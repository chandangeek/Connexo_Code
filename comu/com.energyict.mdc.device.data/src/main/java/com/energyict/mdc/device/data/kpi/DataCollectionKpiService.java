package com.energyict.mdc.device.data.kpi;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;

import com.energyict.mdc.device.data.tasks.TaskStatus;

import java.math.BigDecimal;
import java.time.temporal.TemporalAmount;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
public interface DataCollectionKpiService {

    public DataCollectionKpiBuilder newDataCollectionKpi(QueryEndDeviceGroup group);

    /**
     * Finds all {@link DataCollectionKpi} that are defined in the system.
     *
     * @return The List of DataCollectionKpi
     */
    public List<DataCollectionKpi> findAllDataCollectionKpis();

    /**
     * Finds the {@link DataCollectionKpi} with the specified id.
     *
     * @param id The unique identifier
     * @return The DataCollectionKpi
     */
    public Optional<DataCollectionKpi> findDataCollectionKpi(long id);

    /**
     * Finds the {@link DataCollectionKpi} for the specified {@link QueryEndDeviceGroup}.
     *
     * @param group The QueryEndDeviceGroup
     * @return The DataCollectionKpi
     */
    public Optional<DataCollectionKpi> findDataCollectionKpi(QueryEndDeviceGroup group);

    /**
     * Models behavior to build a new {@link DataCollectionKpi}.
     */
    public interface DataCollectionKpiBuilder {

        /**
         * Specifies that the DataCollectionKpi that is under construction
         * will calculate the connection setup KPI and will do that e.g. every day.
         * The returned KpiTargetBuilder allows to define targets.
         *
         * @param intervalLength The amount of time between each calculation of the connection setup KPI
         * @return The KpiTargetBuilder
         */
        public KpiTargetBuilder calculateConnectionSetupKpi(TemporalAmount intervalLength);

        /**
         * Specifies that the DataCollectionKpi that is under construction
         * will calculate the communication task execution KPI and will do that e.g. every day.
         * The returned KpiTargetBuilder allows to define targets.
         *
         * @param intervalLength The amount of time between each calculation of the communication task execution KPI
         * @return The KpiTargetBuilder
         */
        public KpiTargetBuilder calculateComTaskExecutionKpi(TemporalAmount intervalLength);

        /**
         * Completes the builder and returns the newly
         * created DataCollectionKpi from the specifications
         * that were provided earlier in the build process.
         *
         * @return The newly created DataCollectionKpi
         */
        public DataCollectionKpi save();

    }

    public interface KpiTargetBuilder {

        /**
         * Uses the specified value as a fixed minimum target value,
         * i.e. actual values are expected to be equal to or higher.
         */
        public void expectingAsMinimum(BigDecimal target);

        /**
         * Uses the specified value as a fixed maximum target value,
         * i.e. actual values are expected to be equal to or lower.
         */
        public void expectingAsMaximum(BigDecimal target);

    }

}