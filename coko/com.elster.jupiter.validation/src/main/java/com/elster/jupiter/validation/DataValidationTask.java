/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.orm.HasAuditInfo;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.util.time.ScheduleExpression;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ProviderType
public interface DataValidationTask extends HasAuditInfo {

    void activate();

    DataValidationTaskStatus execute(DataValidationOccurrence taskOccurence);

    void deactivate();

    Instant getNextExecution();

    void update();

    void delete();

    String getName();

    void setName(String name);

    QualityCodeSystem getQualityCodeSystem();

    Optional<EndDeviceGroup> getEndDeviceGroup();

    Optional<UsagePointGroup> getUsagePointGroup();

    Optional<MetrologyPurpose> getMetrologyPurpose();

    void setEndDeviceGroup(EndDeviceGroup endDeviceGroup);

    void setUsagePointGroup(UsagePointGroup usagePointGroup);

    void setMetrologyPurpose(MetrologyPurpose metrologyPurpose);

    long getId();

    void setScheduleImmediately(boolean scheduleImmediately);

    void setScheduleExpression(ScheduleExpression scheduleExpression);

    Optional<Instant> getLastRun();

    ScheduleExpression getScheduleExpression();

    Optional<ScheduleExpression> getScheduleExpression(Instant at);

    void setNextExecution(Instant instant);

    void triggerNow();

    boolean canBeDeleted();

    Optional<DataValidationOccurrence> getLastOccurrence();

    List<? extends DataValidationOccurrence> getOccurrences();

    Optional<? extends DataValidationOccurrence> getOccurrence(Long id);

    DataValidationOccurrenceFinder getOccurrencesFinder();

    History<? extends DataValidationTask> getHistory();

    void updateLastRun(Instant triggerTime);

    int getLogLevel();

    void setLogLevel(int newLevel);
}

