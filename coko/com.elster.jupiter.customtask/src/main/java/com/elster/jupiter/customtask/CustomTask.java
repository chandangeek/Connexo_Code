/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask;

import com.elster.jupiter.orm.HasAuditInfo;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.time.ScheduleExpression;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ProviderType
public interface CustomTask extends HasName, HasAuditInfo {
    long getId();

    String getApplication();

    Optional<Instant> getLastRun();

    Map<String, Object> getValues();

    Map<String, Object> getValues(Instant at);

    List<PropertiesInfo> getPropertySpecs();

    List<CustomTaskAction> getActionsForUser(User user, String application);

    Instant getNextExecution();

    String getTaskType();

    CustomTaskOccurrenceFinder getOccurrencesFinder();

    void update();

    void delete();

    boolean canBeDeleted();

    boolean isActive();

    CustomTaskFactory getCustomTaskFactory();

    ScheduleExpression getScheduleExpression();

    Optional<? extends CustomTaskOccurrence> getLastOccurrence();

    Optional<? extends CustomTaskOccurrence> getOccurrence(Long id);

    void setNextExecution(Instant instant);

    void setScheduleExpression(ScheduleExpression scheduleExpression);

    void setName(String name);

    void setProperty(String key, Object value);

    void triggerNow();

    void updateLastRun(Instant triggerTime);

    History<CustomTask> getHistory();

    Optional<ScheduleExpression> getScheduleExpression(Instant at);

    int getLogLevel();

    void setLogLevel(int newLevel);

    void setNextRecurrentTasks(List<RecurrentTask> nextRecurrentTasks);

    List<RecurrentTask> getNextRecurrentTasks();

    List<RecurrentTask> getPrevRecurrentTasks();

}
