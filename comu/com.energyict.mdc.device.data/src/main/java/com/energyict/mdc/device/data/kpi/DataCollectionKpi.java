package com.energyict.mdc.device.data.kpi;

import com.energyict.mdc.common.HasId;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.util.time.Interval;

import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Optional;

/**
 * Models key performance indicators for the data collection
 * of a set of devices for both connection setup and task execution.
 * Note that the KPI must calculate one and/or the other
 * but cannot be empty.
 * The definition of the set of devices is provided by an
 * {@link com.elster.jupiter.metering.groups.EndDeviceGroup}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-06 (10:12)
 */
public interface DataCollectionKpi extends HasId {

    /**
     * Tests if this DataCollectionKpi calculates the connection setup KPI.
     *
     * @return A flag that indicates if this DataCollectionKpi calculates the connection setup KPI.
     */
    public boolean calculatesConnectionSetupKpi();

    /**
     * Returns the amount of time between each calculation of the
     * connection setup KPI for the devices contained in the {@link EndDeviceGroup}.
     *
     * @return A flag that indicates if this DataCollectionKpi calculates the connection setup KPI.
     */
    public Optional<TemporalAmount> connectionSetupKpiCalculationIntervalLength();

    /**
     * Gets the available {@link DataCollectionKpiScore}s that relate to
     * setting up connections for the specified {@link Interval}.
     *
     * @param interval The Interval
     * @return The List of DataCollectionKpiScore or an empty List if the connection setup KPI is not calculated
     */
    public List<DataCollectionKpiScore> getConnectionSetupKpiScores(Interval interval);

    /**
     * Tests if this DataCollectionKpi calculates the communication task execution KPI.
     *
     * @return A flag that indicates if this DataCollectionKpi calculates the communication task execution KPI.
     */
    public boolean calculatesComTaskExecutionKpi();

    /**
     * Returns the amount of time between each calculation of the
     * communication task execution KPI for the devices contained in the {@link EndDeviceGroup}.
     *
     * @return A flag that indicates if this DataCollectionKpi calculates the communication task execution KPI.
     */
    public Optional<TemporalAmount> comTaskExecutionKpiCalculationIntervalLength();

    /**
     * Gets the available {@link DataCollectionKpiScore}s that relate to
     * the execution of {@link com.energyict.mdc.tasks.ComTask}s
     * for the specified {@link Interval}.
     *
     * @param interval The Interval
     * @return The List of DataCollectionKpiScore or an empty List if the communication task execution KPI is not calculated
     */
    public List<DataCollectionKpiScore> getComTaskExecutionKpiScores(Interval interval);

    /**
     * Gets the group of devices against which this DataCollectionKpi is calculated.
     *
     * @return The EndDeviceGroup
     */
    public QueryEndDeviceGroup getDeviceGroup();

    public void delete();

}