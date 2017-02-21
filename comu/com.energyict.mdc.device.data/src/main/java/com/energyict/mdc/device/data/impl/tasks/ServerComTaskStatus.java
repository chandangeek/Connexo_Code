/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.impl.ClauseAwareSqlBuilder;
import com.energyict.mdc.device.data.impl.ServerComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.TaskStatus;

import java.time.Clock;
import java.time.Instant;

/**
 * Represents the counterpart of {@link TaskStatus} for {@link ComTaskExecution}s
 * and adds behavior that is reserved for server components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-08 (09:04)
 */
public enum ServerComTaskStatus {

    /**
     * @see TaskStatus#OnHold
     */
    OnHold {
        @Override
        public TaskStatus getPublicStatus() {
            return TaskStatus.OnHold;
        }

        @Override
        public boolean appliesTo(ServerComTaskExecution task, Instant now) {
            return task.isOnHold();
        }

        @Override
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
            super.completeFindBySqlBuilder(sqlBuilder, now);
            this.completeCountSqlBuilder(sqlBuilder, now);
        }

        @Override
        public void completeCountSqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
            sqlBuilder.appendWhereOrAnd();
            sqlBuilder.append("cte.onhold <> 0 ");
        }
    },

    /**
     * @see TaskStatus#Busy
     */
    Busy {
        @Override
        public TaskStatus getPublicStatus() {
            return TaskStatus.Busy;
        }

        @Override
        public boolean appliesTo(ServerComTaskExecution task, Instant now) {
            return !task.isOnHold() && task.isExecuting();
        }

        @Override
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
            super.completeFindBySqlBuilder(sqlBuilder, now);
            sqlBuilder.append("and cte.onhold = 0 and ((cte.comport is not null) or " +
                    " ((exists (select * from busytask where busytask.comserver is not null and busytask.connectiontask = cte.connectiontask " +
                    " and busytask.lastCommunicationStart > cte.nextexecutiontimestamp)) " +
                    " and cte.nextExecutionTimestamp is not null and cte.nextexecutiontimestamp <= ");
            sqlBuilder.addLong(this.asSeconds(now));
            sqlBuilder.append("))");
        }

        @Override
        public void completeCountSqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
            sqlBuilder.appendWhereOrAnd();
            sqlBuilder.append("cte.onhold = 0 and ((cte.comport is not null) or ((cte.thereisabusytask is not null) and cte.nextexecutiontimestamp <=");
            // Merge feature/CXO-2099: add cte.onhold = 0 to above line
            sqlBuilder.addLong(this.asSeconds(now));
            sqlBuilder.append("))");
        }
    },

    /**
     * @see TaskStatus#Pending
     */
    Pending {
        @Override
        public TaskStatus getPublicStatus() {
            return TaskStatus.Pending;
        }

        @Override
        public boolean appliesTo(ServerComTaskExecution task, Instant now) {
            Instant nextExecutionTimestamp = task.getNextExecutionTimestamp();
            return !task.isOnHold()
                && !task.isExecuting()
                && nextExecutionTimestamp != null
                && now.isAfter(nextExecutionTimestamp);
        }

        @Override
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
            super.completeFindBySqlBuilder(sqlBuilder, now);
            sqlBuilder.append("and cte.onhold = 0 and ((comport is null) and " +
                    " (not exists (select * from busytask where busytask.comserver is not null and busytask.connectiontask = cte.connectiontask " +
                    " and busytask.lastCommunicationStart > cte.nextexecutiontimestamp)) " +
                    " and cte.nextExecutionTimestamp is not null and cte.nextexecutiontimestamp <=");
            sqlBuilder.addLong(this.asSeconds(now));
            sqlBuilder.append(")");
        }

        @Override
        public void completeCountSqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
            sqlBuilder.appendWhereOrAnd();
            sqlBuilder.append("cte.onhold = 0 and cte.comport is null and cte.thereisabusytask is null and cte.nextexecutiontimestamp <=");
            // Merge feature/CXO-2099: add cte.onhold = 0 to above line
            sqlBuilder.addLong(this.asSeconds(now));
        }
    },

    /**
     * @see TaskStatus#NeverCompleted
     */
    NeverCompleted {
        @Override
        public TaskStatus getPublicStatus() {
            return TaskStatus.NeverCompleted;
        }

        @Override
        public boolean appliesTo(ServerComTaskExecution task, Instant now) {
            return !task.isOnHold()
                && !task.isExecuting()
                && task.getExecutingComPort() == null
                && task.getCurrentTryCount() == 1
                && task.getLastSuccessfulCompletionTimestamp() == null
                && task.getLastExecutionStartTimestamp() != null
                && plannedAndNextExecTimeStampForWaitingStates(task, now);
        }

        @Override
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
            super.completeFindBySqlBuilder(sqlBuilder, now);
            this.completeCountSqlBuilder(sqlBuilder, now);
        }

        @Override
        public void completeCountSqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
            sqlBuilder.appendWhereOrAnd();
            sqlBuilder.append("    cte.onhold = 0 ");
            sqlBuilder.append("and cte.comport is null ");
            sqlBuilder.append("and cte.currentretrycount = 0 ");
            sqlBuilder.append("and cte.lastSuccessfulCompletion is null ");
            sqlBuilder.append("and cte.lastExecutionTimestamp is not null ");
            sqlBuilder.append("and (cte.nextexecutiontimestamp IS NULL OR (cte.nextexecutiontimestamp >");
            sqlBuilder.addLong(this.asSeconds(now));
            sqlBuilder.append("))");
        }
    },

    /**
     * @see TaskStatus#Retrying
     */
    Retrying {
        @Override
        public TaskStatus getPublicStatus() {
            return TaskStatus.Retrying;
        }

        @Override
        public boolean appliesTo(ServerComTaskExecution task, Instant now) {
            int retryCount = task.getCurrentTryCount() - 1;
            return !task.isOnHold()
                && (task.getNextExecutionTimestamp() != null)
                && !task.isExecuting()
                && retryCount > 0;
        }

        @Override
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
            super.completeFindBySqlBuilder(sqlBuilder, now);
            this.completeCountSqlBuilder(sqlBuilder, now);
        }

        @Override
        public void completeCountSqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
            sqlBuilder.appendWhereOrAnd();
            sqlBuilder.append("cte.onhold = 0 ");
            sqlBuilder.append("and cte.nextexecutiontimestamp >");
            sqlBuilder.addLong(this.asSeconds(now));
            sqlBuilder.append("and cte.comport is null ");
            sqlBuilder.append("and cte.currentretrycount > 0");  // currentRetryCount is only incremented when task fails. It is reset to 0 when the maxTries is reached
        }
    },

    /**
     * @see TaskStatus#Failed
     */
    Failed {
        @Override
        public TaskStatus getPublicStatus() {
            return TaskStatus.Failed;
        }

        @Override
        public boolean appliesTo(ServerComTaskExecution task, Instant now) {
            int retryCount = task.getCurrentTryCount() - 1;
            return !task.isOnHold()
                && task.getLastSuccessfulCompletionTimestamp() != null
                && (   task.getLastExecutionStartTimestamp() != null
                    && task.getLastExecutionStartTimestamp().isAfter(task.getLastSuccessfulCompletionTimestamp()))
                && task.isLastExecutionFailed()
                && retryCount == 0;
        }

        @Override
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
            super.completeFindBySqlBuilder(sqlBuilder, now);
            this.completeCountSqlBuilder(sqlBuilder, now);
        }

        @Override
        public void completeCountSqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
            sqlBuilder.appendWhereOrAnd();
            sqlBuilder.append("cte.onhold = 0 ");
            sqlBuilder.append("and cte.nextExecutionTimestamp >");
            sqlBuilder.addLong(this.asSeconds(now));
            sqlBuilder.append("and cte.lastSuccessfulCompletion is not null ");
            sqlBuilder.append("and cte.LASTEXECUTIONTIMESTAMP > lastSuccessfulCompletion ");
            sqlBuilder.append("and cte.lastExecutionFailed = 1 ");
            sqlBuilder.append("and cte.currentretrycount = 0");
        }
    },

    /**
     * @see TaskStatus#Waiting
     */
    Waiting {
        @Override
        public TaskStatus getPublicStatus() {
            return TaskStatus.Waiting;
        }

        @Override
        public boolean appliesTo(ServerComTaskExecution task, Instant now) {
            return !task.isOnHold()
                && !task.isExecuting()
                && !task.isLastExecutionFailed()
                && task.getCurrentTryCount() == 1
                && (task.getLastExecutionStartTimestamp() == null || task.getLastSuccessfulCompletionTimestamp() != null)
                && plannedAndNextExecTimeStampForWaitingStates(task, now);
        }

        @Override
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
            super.completeFindBySqlBuilder(sqlBuilder, now);
            this.completeCountSqlBuilder(sqlBuilder, now);
        }

        @Override
        public void completeCountSqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
            sqlBuilder.appendWhereOrAnd();
            sqlBuilder.append("cte.onhold = 0 ");
            sqlBuilder.append("and cte.comport is null ");
            sqlBuilder.append("and cte.lastExecutionFailed = 0 ");
            sqlBuilder.append("and cte.currentretrycount = 0 ");
            sqlBuilder.append("and (cte.lastExecutionTimestamp is null or cte.lastSuccessfulCompletion is not null) ");
            sqlBuilder.append("and (cte.nextexecutiontimestamp IS NULL OR (cte.nextexecutiontimestamp >");
            sqlBuilder.addLong(this.asSeconds(now));
            sqlBuilder.append("))");
        }
    },

    /**
     * Purely technical state which serves as an indication that there is some inconsitent state in a particular communication task
     */
    ProcessingError {
        @Override
        public TaskStatus getPublicStatus() {
            return TaskStatus.ProcessingError;
        }

        @Override
        public boolean appliesTo(ServerComTaskExecution task, Instant now) {
            return false;
        }

        @Override
        public void completeCountSqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
            sqlBuilder.appendWhereOrAnd();
            sqlBuilder.append("1 = 0");
        }

        @Override
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
            super.completeFindBySqlBuilder(sqlBuilder, now);
            this.completeCountSqlBuilder(sqlBuilder, now);
        }
    };

    protected long asSeconds(Instant date) {
        if (date == null) {
            return 0;
        }
        else {
            return date.getEpochSecond();
        }
    }

    protected boolean plannedAndNextExecTimeStampForWaitingStates(ServerComTaskExecution task, Instant now) {
        return (task.getPlannedNextExecutionTimestamp() == null && task.getNextExecutionTimestamp() == null)
            || (   task.isAdHoc()
                && (   task.getNextExecutionTimestamp() == null
                    || (task.getNextExecutionTimestamp().isAfter(now))))
            || (   task.getNextExecutionSpecs().isPresent()
                && (   task.getNextExecutionTimestamp() == null
                    || task.getNextExecutionTimestamp().isAfter(now)));
    }
    /**
     * Returns the public counterpart of this ServerTaskStatus.
     *
     * @return The public counterpart
     */
    public abstract TaskStatus getPublicStatus();

    /**
     * Checks if this ServerTaskStatus applies to the {@link ComTaskExecution}.
     *
     * @param task The ComTaskExecution
     * @param now  The current time
     * @return <code>true</code> iff this ServerTaskStatus applies to the ComTaskExecution
     */
    public abstract boolean appliesTo(ServerComTaskExecution task, Instant now);

    public final void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Clock clock) {
        sqlBuilder.appendWhereOrAnd();
        this.completeFindBySqlBuilder(sqlBuilder, clock.instant());
    }

    protected void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant date) {
        sqlBuilder.append("cte.obsolete_date is null ");
    }

    public abstract void completeCountSqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant now);

    /**
     * Gets the {@link TaskStatus} that applies to the specified {@link ComTaskExecution}.
     *
     * @param task The ComTaskExecution
     * @param now  The current time
     * @return The applicable TaskStatus
     */
    public static TaskStatus getApplicableStatusFor(ServerComTaskExecution task, Instant now) {
        /* Implementation note:
         * Changing the order of the enum values
         * will/can have an effect on the outcome. */
        for (ServerComTaskStatus serverComTaskStatus : values()) {
            if (serverComTaskStatus.appliesTo(task, now)) {
                return serverComTaskStatus.getPublicStatus();
            }
        }
        return TaskStatus.initial();
    }

    /**
     * Converts the {@link TaskStatus} to the corresponding {@link ServerComTaskStatus}.
     *
     * @param taskStatus The TaskStatus
     * @return The corresponding ServerTaskStatus
     */
    public static ServerComTaskStatus forTaskStatus(TaskStatus taskStatus) {
        for (ServerComTaskStatus serverComTaskStatus : values()) {
            if (serverComTaskStatus.getPublicStatus().equals(taskStatus)) {
                return serverComTaskStatus;
            }
        }
//        throw CodingException.unrecognizedEnumValue(TaskStatus.class, taskStatus.ordinal());
        //TODO JP-1125
        throw new RuntimeException("UnrecognizedEnumValue ...");
    }

}