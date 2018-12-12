/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.kpi;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.util.HasId;

import com.google.common.collect.Range;

import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Optional;

public interface RegisteredDevicesKpi extends HasId {

    TemporalAmount getFrequency();

    long getVersion();

    /**
     * Gets the group of devices against which this RegisteredDevicesKpi is calculated.
     *
     * @return The EndDeviceGroup
     */
    EndDeviceGroup getDeviceGroup();

    long getTarget();

    void delete();

    List<RegisteredDevicesKpiScore> getScores(Range<Instant> interval);

    /**
     * @return the most recent Instant either a connection task or communication task KPI was calculated by a recurrent task
     */
    Optional<Instant> getLatestCalculation();

    void updateTarget(long target);

    void setTarget(long target);

    List<RecurrentTask> getNextRecurrentTasks();

    List<RecurrentTask> getPrevRecurrentTasks();

    void setNextRecurrentTasks(List<RecurrentTask> nextRecurrentTasks);

    void save();
}
