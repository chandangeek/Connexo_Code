package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskLogEntry;
import com.elster.jupiter.tasks.TaskLogHandler;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.logging.LogEntryFinder;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import static com.elster.jupiter.util.conditions.Where.where;

class TaskOccurrenceImpl implements TaskOccurrence {

    private long id;
    private long recurrentTaskId;
    private RecurrentTask recurrentTask;
    private Instant triggerTime;

    private List<TaskLogEntry> logEntries = new ArrayList<>();

    private final DataModel dataModel;
    private transient TaskLogEntryFinder taskLogEntryFinder;

    @Inject
	TaskOccurrenceImpl(DataModel dataModel) {
        // for persistence
        this.dataModel = dataModel;
    }

    static TaskOccurrenceImpl from(DataModel dataModel, RecurrentTask recurrentTask, Instant triggerTime) {
        return dataModel.getInstance(TaskOccurrenceImpl.class).init(recurrentTask, triggerTime);
    }

    @Override
    public String getPayLoad() {
        return getRecurrentTask().getPayLoad();
    }

    @Override
    public Instant getTriggerTime() {
        return triggerTime;
    }

    @Override
    public RecurrentTask getRecurrentTask() {
        if (recurrentTask == null) {
            recurrentTask = dataModel.mapper(RecurrentTask.class).getExisting(recurrentTaskId);
        }
        return recurrentTask;
    }

    @Override
    public void save() {
        if (id == 0) {
            dataModel.mapper(TaskOccurrence.class).persist(this);
        } else {
            dataModel.mapper(TaskOccurrence.class).update(this);
        }
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public List<TaskLogEntry> getLogs() {
        return Collections.unmodifiableList(logEntries);
    }

    @Override
    public LogEntryFinder getLogsFinder() {
        Condition condition = where("taskOccurrence").isEqualTo(this);
        Order[] orders = new Order[] {Order.descending("timeStamp"), Order.ascending("position")};
        LogEntryFinder finder = new TaskLogEntryFinder(dataModel.query(TaskLogEntry.class), condition, orders);
        return finder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskOccurrenceImpl that = (TaskOccurrenceImpl) o;

        if (id != that.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public TaskLogHandler createTaskLogHandler() {
        return new TaskLogHandlerImpl(this);
    }

    TaskOccurrenceImpl init(RecurrentTask recurrentTask, Instant triggerTime) {
        this.recurrentTask = recurrentTask;
        this.recurrentTaskId = recurrentTask.getId();
        this.triggerTime = triggerTime;
        return this;
    }

    void hasRun() {
        ((RecurrentTaskImpl) recurrentTask).updateLastRun(getTriggerTime());
    }

    void log(Level level, Instant timestamp, String message) {
        logEntries.add(dataModel.getInstance(TaskLogEntryImpl.class).init(this, timestamp, level, message));
    }
}
