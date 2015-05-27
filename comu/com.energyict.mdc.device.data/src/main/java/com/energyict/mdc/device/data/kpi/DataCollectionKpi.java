package com.energyict.mdc.device.data.kpi;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.HasId;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
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
@ProviderType
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

    void updateDisplayRange(TimeDuration displayPeriod);

    TimeDuration getDisplayRange();

    /**
     * @return  Returns the static target for the connection kpi, if present.
     *    Optional will be empty if this Kpi does not support connection Kpis or
     *    if no entries have been registered for the Kpi so far
     */
    public Optional<BigDecimal> getStaticConnectionKpiTarget();

    /**
     * @return  Returns the static target for the communication kpi, if present.
     *    Optional will be empty if this Kpi does not support communication Kpis or
     *    if no entries have been registered for the Kpi so far
     */
    public Optional<BigDecimal> getStaticCommunicationKpiTarget();

    /**
     * Gets the available {@link DataCollectionKpiScore}s that relate to
     * setting up connections for the specified interval.
     *
     * @param interval The Interval
     * @return The List of DataCollectionKpiScore or an empty List if the connection setup KPI is not calculated
     */
    public List<DataCollectionKpiScore> getConnectionSetupKpiScores(Range<Instant> interval);

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
     * for the specified interval.
     *
     * @param interval The Interval
     * @return The List of DataCollectionKpiScore or an empty List if the communication task execution KPI is not calculated
     */
    public List<DataCollectionKpiScore> getComTaskExecutionKpiScores(Range<Instant> interval);

    void setFrequency(TemporalAmount intervalLength);

    TemporalAmount getFrequency();

    /**
     * Gets the group of devices against which this DataCollectionKpi is calculated.
     *
     * @return The EndDeviceGroup
     */
    public EndDeviceGroup getDeviceGroup();

    public void delete();

    /**
     *
     * @return the most recent Instant either a connection task or communication task KPI was calculated by a recurrent task
     */
    public Optional<Instant> getLatestCalculation();

    /**
     * Add a communication task KPI to this data collection KPI with the given frequency and static target
     * The KPI calculation can be stopped by calling dropComTaskExecutionKpiCalculation
     * @param staticTarget target
     */
    public void calculateComTaskExecutionKpi(BigDecimal staticTarget);

    /**
     * Stops calculation of the communication KPI / ComTaskExecution KPI
     */
    public void dropComTaskExecutionKpi();

    /**
     * Add a communication task KPI to this data collection KPI with the given frequency and static target
     * The calculation can be stopped by calling dropConnectionKpiCalculation
     * @param staticTarget target
     */
    public void calculateConnectionKpi(BigDecimal staticTarget);

    /**
     * Stops calculation of the connection KPI
     */
    public void dropConnectionSetupKpi();

}