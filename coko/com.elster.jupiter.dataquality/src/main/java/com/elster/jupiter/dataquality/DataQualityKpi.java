/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality;

import com.elster.jupiter.tasks.RecurrentTask;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Optional;

@ProviderType
public interface DataQualityKpi {

    long getId();

    long getVersion();

    TemporalAmount getFrequency();

    Optional<Instant> getLatestCalculation();

    void delete();

    void makeObsolete();

    Optional<Instant> getObsoleteTime();

    List<RecurrentTask> getNextRecurrentTasks();

    List<RecurrentTask> getPrevRecurrentTasks();

    void setNextRecurrentTasks(List<RecurrentTask> nextRecurrentTasks);

    void save();

}
