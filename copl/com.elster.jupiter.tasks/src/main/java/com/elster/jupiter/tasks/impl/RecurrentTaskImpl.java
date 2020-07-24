/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.domain.util.HasNoBlacklistedCharacters;
import com.elster.jupiter.domain.util.HasNotAllowedChars;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.tasks.NextRecurrentTask;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskAdHocExecution;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.sql.Fetcher;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.elster.jupiter.util.time.ScheduleExpressionParser;

import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.NOT_UNIQUE + "}")
class RecurrentTaskImpl implements RecurrentTask {

    private static final Logger LOGGER = Logger.getLogger(RecurrentTaskImpl.class.getName());

    @SuppressWarnings("unused") // Managed by ORM
    private long id;
    private String application;
    @NotNull(message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    @Size(min = 1, max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    @HasNoBlacklistedCharacters(balcklistedCharRegEx = HasNotAllowedChars.Constant.SCRIPT_CHARS)
    private String name;
    private transient ScheduleExpression scheduleExpression;
    private String cronString;
    private Instant nextExecution;
    private String payload;
    private String destination;
    private int priority;
    private Instant lastRun;
    private Instant suspendUntilTime;
    private transient DestinationSpec destinationSpec;
    private int logLevel;

    private final Clock clock;
    private final ScheduleExpressionParser scheduleExpressionParser;
    private final MessageService messageService;
    private final DataModel dataModel;
    private final JsonService jsonService;
    @Valid
    private List<TaskAdHocExecution> adhocExecutions = new ArrayList<>();
    private List<NextRecurrentTask> nextRecurrentTasks = new ArrayList<>();
    private List<NextRecurrentTask> prevRecurrentTasks = new ArrayList<>();
    private List<RecurrentTask> newNextRecurrentTasks = new ArrayList<>();

    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;

    @Inject
    RecurrentTaskImpl(DataModel dataModel, ScheduleExpressionParser scheduleExpressionParser, MessageService messageService, JsonService jsonService, Clock clock) {
        this.dataModel = dataModel;
        this.scheduleExpressionParser = scheduleExpressionParser;
        this.messageService = messageService;
        this.jsonService = jsonService;
        // for persistence
        this.clock = clock;
        this.setLogLevel(Level.WARNING.intValue());
        this.priority = DEFAULT_PRIORITY;
    }

    RecurrentTaskImpl init(String application, String name, ScheduleExpression scheduleExpression, DestinationSpec destinationSpec, String payload, int logLevel, int priority) {
        this.application = application;
        this.destinationSpec = destinationSpec;
        this.destination = destinationSpec.getName();
        this.payload = payload;
        this.cronString = scheduleExpression.encoded();
        this.name = name;
        this.scheduleExpression = scheduleExpression;
        this.setLogLevel(logLevel);
        this.priority = priority;
        return this;
    }

    static RecurrentTaskImpl from(DataModel dataModel, String application, String name, ScheduleExpression scheduleExpression, DestinationSpec destinationSpec, String payload, int logLevel, int priority) {
        return dataModel.getInstance(RecurrentTaskImpl.class).init(application, name, scheduleExpression, destinationSpec, payload, logLevel, priority);
    }

    @Override
    public long getId() {
        return id;
    }

    void setId(long id) {
        this.id = id;
    }

    @Override
    public void updateNextExecution() {
        Instant time = clock.instant();

        if(suspendUntilTime!=null && suspendUntilTime.isAfter(time)) {
            time = suspendUntilTime;
        }
        //else{
            suspendUntilTime = null;
        //}
        ZonedDateTime now = ZonedDateTime.ofInstant(time, ZoneId.systemDefault());
        Optional<ZonedDateTime> nextOccurrence = getScheduleExpression().nextOccurrence(now);
        nextExecution = nextOccurrence.map(ZonedDateTime::toInstant).orElse(null);
    }

    public ScheduleExpression getScheduleExpression() {
        if (scheduleExpression == null) {
            scheduleExpression = scheduleExpressionParser.parse(cronString).get();
        }
        return scheduleExpression;
    }

    @Override
    public DestinationSpec getDestination() {
        if (destinationSpec == null) {
            destinationSpec = messageService.getDestinationSpec(destination).get();
        }
        return destinationSpec;
    }

    @Override
    public String getPayLoad() {
        return payload;
    }

    @Override
    public Instant getNextExecution() {
        List<Instant> executions = adhocExecutions.stream().map(exec -> exec.getNextExecution()).collect(Collectors.toList());
        if (nextExecution != null) {
            executions.add(nextExecution);
        }
        return executions.stream().min(Instant::compareTo).orElse(null);
    }

    TaskOccurrenceImpl createScheduledTaskOccurrence() {
        TaskOccurrenceImpl occurrence = TaskOccurrenceImpl.createScheduled(dataModel, this, nextExecution != null ? nextExecution : clock.instant());
        occurrence.save();
        return occurrence;
    }

    TaskOccurrenceImpl createAdHocTaskOccurrence() {
        TaskOccurrenceImpl occurrence = TaskOccurrenceImpl.createAdHoc(dataModel, this, clock.instant());
        occurrence.save();
        return occurrence;
    }

    TaskOccurrenceImpl createAdHocTaskOccurrence(Instant adhocTime) {
        TaskOccurrenceImpl occurrence = TaskOccurrenceImpl.createAdHoc(dataModel, this, clock.instant(), adhocTime);
        occurrence.save();
        return occurrence;
    }

    TaskOccurrenceImpl createAdHocTaskOccurrence(TaskOccurrence taskOccurrence) {
        TaskOccurrenceImpl occurrence = TaskOccurrenceImpl.createRetryAdHoc(dataModel, this, clock.instant(),
                taskOccurrence.getRetryTime().isPresent() ? taskOccurrence.getRetryTime().get() : taskOccurrence.getStartDate().get(),
                taskOccurrence.getAdhocTime().isPresent() ? taskOccurrence.getAdhocTime().get() : null);
        occurrence.save();
        return occurrence;
    }

    @Override
    public void save() {
        if (id == 0) {
            Save.CREATE.save(dataModel, this);
            postSave();
        } else {
            Save.UPDATE.save(dataModel, this);
            postSave();
        }
    }

    private void postSave() {
        nextRecurrentTasks.clear();
        newNextRecurrentTasks.forEach(newNextRecurrentTask -> {
            NextRecurrentTaskImpl nextRecurrentTask = dataModel.getInstance(NextRecurrentTaskImpl.class).init(this, newNextRecurrentTask);
            Save.CREATE.validate(dataModel, newNextRecurrentTask);
            nextRecurrentTasks.add(nextRecurrentTask);
        });
    }

    @Override
    public void delete() {
        dataModel.mapper(TaskOccurrenceImpl.class).remove(getOccurrences());
        dataModel.mapper(RecurrentTask.class).remove(this);
    }

    private List<TaskOccurrenceImpl> getOccurrences() {
        return dataModel.mapper(TaskOccurrenceImpl.class).find("recurrentTask", this);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setNextExecution(Instant nextExecution) {
        this.nextExecution = nextExecution;
        this.suspendUntilTime = null;
    }

    @Override
    public void resume() {
        updateNextExecution();
        save();
    }

    @Override
    public void suspend() {
        this.nextExecution = null;
        save();
    }

    @Override
    public void setScheduleExpression(ScheduleExpression scheduleExpression) {
        this.scheduleExpression = scheduleExpression;
        this.cronString = scheduleExpression.encoded();
    }

    @Override
    public void setScheduleExpressionString(String expression) {
        this.cronString = expression;
        this.scheduleExpression = scheduleExpressionParser.parse(cronString).orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public void triggerNow() {
        TaskOccurrenceImpl taskOccurrence = createAdHocTaskOccurrence();
        enqueue(taskOccurrence);
    }

    @Override
    public void triggerAt(Instant at, Instant trigger) {
        addAdHocExecution(at, trigger);
    }

    @Override
    public void triggerNow(TaskOccurrence taskOccurrence) {
        TaskOccurrenceImpl newTaskOccurrence = createAdHocTaskOccurrence(taskOccurrence);
        enqueue(newTaskOccurrence);
    }

    @Override
    public TaskOccurrenceImpl runNow(TaskExecutor executor) {
        TaskOccurrenceImpl taskOccurrence = createAdHocTaskOccurrence();
        taskOccurrence.start();
        boolean success = false;
        try {
            executor.execute(taskOccurrence);
            executor.postExecute(taskOccurrence);
            success = true;
        } finally {
            taskOccurrence.hasRun(success);
            taskOccurrence.save();
        }
        return taskOccurrence;

    }

    public void triggerNewRecurrentTasks() {
        this.getNextRecurrentTasks().stream().forEach(
                nextRecurrentTask -> nextRecurrentTask.triggerNow()
        );
    }

    private void enqueue(TaskOccurrenceImpl taskOccurrence) {
        String json = toJson(taskOccurrence);
        buildMessage(json, taskOccurrence.getRecurrentTask().getPriority()).send();
    }

    private MessageBuilder buildMessage(String json, int priority) {
        MessageBuilder messageBuilder = getDestination().message(json);
        if (getDestination().isPrioritized()) {
            messageBuilder.withPriority(priority);
        }
        return messageBuilder;
    }

    @TransactionRequired
    List<TaskOccurrenceImpl> launchOccurrence(Instant at) {
        List<TaskOccurrenceImpl> taskOccurrences = new ArrayList<>();
        try {
            if ((nextExecution != null) && (nextExecution.compareTo(at) <= 0)) {
                TaskOccurrenceImpl taskOccurrence = createScheduledTaskOccurrence();

                String json = toJson(taskOccurrence);
                buildMessage(json, taskOccurrence.getRecurrentTask().getPriority()).send();
                if (taskOccurrence.wasScheduled()) {
                    updateNextExecution();
                    dataModel.mapper(RecurrentTask.class).update(this, "nextExecution","suspendUntilTime");
                }
                taskOccurrences.add(taskOccurrence);
            }
            List<TaskAdHocExecution> taskExec = getAdhocExecutions().stream()
                    .filter(taskAdHocExecution -> taskAdHocExecution.getNextExecution().compareTo(at) <= 0)
                    .collect(Collectors.toList());
            taskExec.forEach(taskAdHocExecution1 -> {
                TaskOccurrenceImpl taskOccurrence = createAdHocTaskOccurrence(taskAdHocExecution1.getTriggerTime());

                String json = toJson(taskOccurrence);
                buildMessage(json, taskOccurrence.getRecurrentTask().getPriority()).send();
                getAdhocExecutions().removeIf(taskAdHocExecution2 -> taskAdHocExecution1.getId() == taskAdHocExecution2.getId());
                taskOccurrences.add(taskOccurrence);
            });

            return taskOccurrences;
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Failed to schedule task for RecurrentTask " + this.getName(), e);
            return taskOccurrences;
        }
    }


    @Override
    public Optional<Instant> getLastRun() {
        return Optional.ofNullable(lastRun);
    }

    @Override
    public Optional<TaskOccurrence> getLastOccurrence() {
        DataMapper<TaskOccurrenceImpl> mapper = dataModel.mapper(TaskOccurrenceImpl.class);
        SqlBuilder builder = new SqlBuilder();
        builder.append("select ID, RECURRENTTASKID, TRIGGERTIME, RETRYTIME, ADHOCTIME, SCHEDULED, STARTDATE, ENDDATE, STATUS ");
        builder.append("from TSK_TASK_OCCURRENCE ");
        builder.append("where ID in (");
        builder.append("select MAX(ID) from TSK_TASK_OCCURRENCE where RECURRENTTASKID = " + this.getId() + ")");
        try (Fetcher<TaskOccurrenceImpl> fetcher = mapper.fetcher(builder)) {
            Iterator<TaskOccurrenceImpl> occuranceTaskIterator = fetcher.iterator();
            return occuranceTaskIterator.hasNext() ? Optional.of((TaskOccurrence) occuranceTaskIterator.next()) : Optional.empty();
        }
    }

    @Override
    public List<TaskOccurrence> getTaskOccurrences() {
        return dataModel.mapper(TaskOccurrence.class).find("recurrentTask", this, Order.descending("id"));
    }

    public long getVersion() {
        return version;
    }

    public Instant getCreateTime() {
        return createTime;
    }

    public Instant getModTime() {
        return modTime;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public History<? extends RecurrentTask> getHistory() {
        return new History<>(dataModel.mapper(RecurrentTaskImpl.class).getJournal(this.getId()), this);
    }

    @Override
    public Optional<RecurrentTask> getVersionAt(Instant time) {
        List<JournalEntry<RecurrentTask>> journalEntries = dataModel.mapper(RecurrentTask.class).at(time).find(ImmutableMap.of("id", this.getId()));
        return journalEntries.stream()
                .map(JournalEntry::get)
                .findFirst();
    }

    void updateLastRun(Instant triggerTime) {
        lastRun = triggerTime;
        if (id == 0) {
            save();
        } else {
            dataModel.mapper(RecurrentTaskImpl.class).update(this, "lastRun");
        }
    }

    private String toJson(TaskOccurrenceImpl taskOccurrence) {
        return jsonService.serialize(taskOccurrence.asMessage());
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RecurrentTaskImpl)) {
            return false;
        }
        RecurrentTaskImpl that = (RecurrentTaskImpl) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String getApplication() {
        return application;
    }

    @Override
    public void setName(String name) {
        this.name = name;
        this.save();
    }

    public void setLogLevel(int newLevel) {
        this.logLevel = newLevel;
    }

    public int getLogLevel() {
        return logLevel;
    }

    public int getLogLevel(Instant at) {
        Optional<? extends RecurrentTask> recurrentTask = getHistory().getVersionAt(at);
        return recurrentTask.isPresent() ? recurrentTask.get().getLogLevel() : getLogLevel();
    }

    private void addAdHocExecution(Instant nextExecution, Instant triggerTime) {
        TaskAdHocExecutionImpl adHocExecution = dataModel.getInstance(TaskAdHocExecutionImpl.class).init(this, nextExecution, triggerTime);
        Save.CREATE.validate(dataModel, adHocExecution);
        adhocExecutions.add(adHocExecution);
        save();
    }

    public List<TaskAdHocExecution> getAdhocExecutions() {
        return adhocExecutions;
    }

    @Override
    public List<RecurrentTask> getNextRecurrentTasks() {
        return nextRecurrentTasks.stream()
                .map(nextRecurrentTask -> nextRecurrentTask.getNextRecurrentTask())
                .collect(Collectors.toList());
    }

    @Override
    public List<RecurrentTask> getPrevRecurrentTasks() {
        return prevRecurrentTasks.stream()
                .map(nextRecurrentTask -> nextRecurrentTask.getRecurrentTask())
                .collect(Collectors.toList());
    }

    @Override
    public RecurrentTask setNextRecurrentTasks(List<RecurrentTask> recurrentTasks) {
        newNextRecurrentTasks = recurrentTasks;
        return this;
    }

    public void saveNextRecurrentTask(RecurrentTask nextRecurrentTask) {
        NextRecurrentTaskImpl nrt = dataModel.getInstance(NextRecurrentTaskImpl.class).init(this, nextRecurrentTask);
        Save.CREATE.validate(dataModel, nrt);
        nextRecurrentTasks.add(nrt);
    }

    @Override
    public void setDestination(String destination) {
        this.destination = destination;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public void setSuspendUntil(Instant suspendUntilTime) {
        this.nextExecution = this.suspendUntilTime = suspendUntilTime;
        save();
    }

    @Override
    public Instant getSuspendUntil(){
        return suspendUntilTime;
    }

}