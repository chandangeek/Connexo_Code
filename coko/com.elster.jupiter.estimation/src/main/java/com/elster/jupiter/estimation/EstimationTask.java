/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.orm.HasAuditInfo;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.time.ScheduleExpression;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.Optional;

@ProviderType
public interface EstimationTask extends HasName, HasAuditInfo {

    long getId();

    boolean isActive();

    boolean canBeDeleted();

    Instant getNextExecution();

    QualityCodeSystem getQualityCodeSystem();

    Optional<EndDeviceGroup> getEndDeviceGroup();

    Optional<UsagePointGroup> getUsagePointGroup();

    Optional<MetrologyPurpose> getMetrologyPurpose();

    Optional<Instant> getLastRun();

    Optional<RelativePeriod> getPeriod();

    ScheduleExpression getScheduleExpression();

    Optional<ScheduleExpression> getScheduleExpression(Instant at);

    Optional<TaskOccurrence> getOccurrence(Long id);

    Optional<TaskOccurrence> getLastOccurrence();

    boolean isReValidate();

    EstimationTaskOccurrenceFinder getOccurrencesFinder();

    History<EstimationTask> getHistory();

    void setName(String name);

    void setNextExecution(Instant instant);

    void setEndDeviceGroup(EndDeviceGroup endDeviceGroup);

    void setUsagePointGroup(UsagePointGroup usagePointGroup);

    void setMetrologyPurpose(MetrologyPurpose metrologyPurpose);

    void setPeriod(RelativePeriod relativePeriod);

    void setScheduleExpression(ScheduleExpression scheduleExpression);

    void setReValidate(boolean reValidate);

    void update();

    void delete();

    void triggerNow();

    void updateLastRun(Instant triggerTime);

    int getLogLevel();

    void setLogLevel(int newLevel);
}
