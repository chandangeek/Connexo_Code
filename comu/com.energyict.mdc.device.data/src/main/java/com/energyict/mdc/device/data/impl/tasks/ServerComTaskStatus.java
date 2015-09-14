package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.impl.ServerComTaskExecution;
import com.energyict.mdc.device.data.impl.ClauseAwareSqlBuilder;
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
            return task.isOnHold() && !task.isExecuting();
        }

        @Override
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
            super.completeFindBySqlBuilder(sqlBuilder, now);
            sqlBuilder.append("and cte.nextExecutionTimestamp is null and cte.comport is null");
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
            return task.isExecuting() && ((!task.getConnectionTask().isPresent()) || (task.getConnectionTask().get().isExecuting()));
        }

        @Override
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
            super.completeFindBySqlBuilder(sqlBuilder, now);
            sqlBuilder.append("and ((comport is not null) or ((exists (select * from busytask");
            sqlBuilder.append(" where busytask.connectiontask = cte.connectiontask)) and cte.nextExecutionTimestamp is not null and cte.nextexecutiontimestamp <=");
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
            return !task.isExecuting()
                    && nextExecutionTimestamp != null
                    && now.compareTo(nextExecutionTimestamp) >= 0;
        }

        @Override
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
            super.completeFindBySqlBuilder(sqlBuilder, now);
            sqlBuilder.append("and comport is null and not exists (select * from busytask");
            sqlBuilder.append(" where busytask.connectiontask = cte.connectiontask) and cte.nextexecutiontimestamp <=");
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
            Instant nextExecutionTimestamp = task.getNextExecutionTimestamp();
            return task.getLastSuccessfulCompletionTimestamp() == null
                    && task.getExecutingComPort() == null
                    && task.getCurrentTryCount() == 0
                    && nextExecutionTimestamp != null
                    && nextExecutionTimestamp.isAfter(now);
        }

        @Override
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
            super.completeFindBySqlBuilder(sqlBuilder, now);
            sqlBuilder.append("and lastSuccessfulCompletion is null ");
            sqlBuilder.append("and comport is null ");
            sqlBuilder.append("and currentretrycount = 0 ");
            sqlBuilder.append("and nextExecutionTimestamp is not null ");
            sqlBuilder.append("and nextExecutionTimestamp > ");
            sqlBuilder.addLong(this.asSeconds(now));
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
            return task.getNextExecutionTimestamp() != null
                    && !task.isExecuting()
                    && retryCount > 0;
        }

        @Override
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
            super.completeFindBySqlBuilder(sqlBuilder, now);
            sqlBuilder.append("and nextexecutiontimestamp > ");
            sqlBuilder.addLong(this.asSeconds(now));
            sqlBuilder.append("and comport is null ");
            sqlBuilder.append("and currentretrycount > 0");  // currentRetryCount is only incremented when task fails. It is reset to 0 when the maxTries is reached
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
            Instant nextExecutionTimestamp = task.getNextExecutionTimestamp();
            int retryCount = task.getCurrentTryCount() - 1;
            return nextExecutionTimestamp != null
                    && nextExecutionTimestamp.isAfter(now)
                    && task.getLastSuccessfulCompletionTimestamp() != null
                    && task.isLastExecutionFailed()
                    && retryCount == 0;
        }

        @Override
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
            super.completeFindBySqlBuilder(sqlBuilder, now);
            sqlBuilder.append("and nextExecutionTimestamp is not null ");
            sqlBuilder.append("and nextExecutionTimestamp >");
            sqlBuilder.addLong(this.asSeconds(now));
            sqlBuilder.append("and lastSuccessfulCompletion is not null ");
            sqlBuilder.append("and lastExecutionFailed = 1 ");
            sqlBuilder.append("and currentretrycount = 0");
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
            Instant nextExecutionTimestamp = task.getNextExecutionTimestamp();
            return !task.isExecuting()
                    && task.getLastSuccessfulCompletionTimestamp() != null
                    && nextExecutionTimestamp != null
                    && nextExecutionTimestamp.isAfter(now)
                    && !task.isLastExecutionFailed();
        }

        @Override
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
            super.completeFindBySqlBuilder(sqlBuilder, now);
            sqlBuilder.append("and comport is null ");
            sqlBuilder.append("and lastSuccessfulCompletion is not null ");
            sqlBuilder.append("and nextexecutiontimestamp >");
            sqlBuilder.addLong(this.asSeconds(now));
            sqlBuilder.append("and lastExecutionFailed = 0");
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