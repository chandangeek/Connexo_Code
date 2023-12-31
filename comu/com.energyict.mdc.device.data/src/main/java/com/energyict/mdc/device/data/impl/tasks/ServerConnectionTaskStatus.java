/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.TaskStatus;
import com.energyict.mdc.device.data.impl.ClauseAwareSqlBuilder;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFields;

import java.time.Clock;
import java.time.Instant;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Represents the counterpart of {@link TaskStatus} for {@link ScheduledConnectionTask}s
 * and adds behavior that is reserved for server components.
 */
public enum ServerConnectionTaskStatus {

    /**
     * @see TaskStatus#OnHold
     */
    OnHold {
        @Override
        public TaskStatus getPublicStatus() {
            return TaskStatus.OnHold;
        }

        @Override
        public boolean appliesTo(ScheduledConnectionTask task, Instant now) {
            return !task.getStatus().equals(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE)
                    || task.getNextExecutionTimestamp() == null;
        }

        @Override
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Clock clock, String connectionTaskTableName) {
            super.completeFindBySqlBuilder(sqlBuilder, clock, connectionTaskTableName);
            sqlBuilder.append("and (    not exists (select * from busytask where busytask.connectiontask = ").append(connectionTaskTableName).append(".id and comport is not null)");
            sqlBuilder.append("     and ").append(connectionTaskTableName).append("." + ConnectionTaskFields.COM_PORT.fieldName() + " is null) ");
            sqlBuilder.append("and (   (discriminator =").addObject(ConnectionTaskImpl.INBOUND_DISCRIMINATOR).append(" and ").append(connectionTaskTableName).append(".status > 0)");
            sqlBuilder.append("     or (discriminator =").addObject(ConnectionTaskImpl.SCHEDULED_DISCRIMINATOR).append(" and (").append(connectionTaskTableName).append(".status > 0 or ").append(connectionTaskTableName).append(".nextExecutionTimestamp is null)))");
        }

        @Override
        public void appendBreakdownCaseClause(SqlBuilder sqlBuilder, Clock clock) {
            sqlBuilder.append("        WHEN (discriminator = ");
            sqlBuilder.addObject(ConnectionTaskImpl.INBOUND_DISCRIMINATOR);
            sqlBuilder.append("and status > 0)");
            sqlBuilder.append("          OR (discriminator = ");
            sqlBuilder.addObject(ConnectionTaskImpl.SCHEDULED_DISCRIMINATOR);
            sqlBuilder.append("and (status > 0 or nextExecutionTimestamp is null))");
            this.appendBreakdownThenClause(sqlBuilder);
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
        public boolean appliesTo(ScheduledConnectionTask task, Instant now) {
            return task.isExecuting() || task.getScheduledComTasks().stream().anyMatch(ComTaskExecution::isExecuting);
        }

        @Override
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Clock clock, String connectionTaskTableName) {
            super.completeFindBySqlBuilder(sqlBuilder, clock, connectionTaskTableName);
            sqlBuilder.append("and ").append(connectionTaskTableName).append(".status = 0 ");
            sqlBuilder.append("and ").append(connectionTaskTableName).append(".nextexecutiontimestamp is not null ");
            sqlBuilder.append("and ").append("(   exists (select * from busytask where busytask.connectiontask = ").append(connectionTaskTableName).append(".id and comport is not null)");
            sqlBuilder.append("                or ").append(connectionTaskTableName).append("." + ConnectionTaskFields.COM_PORT.fieldName() + " is not null)");
        }

        @Override
        public void appendBreakdownCaseClause(SqlBuilder sqlBuilder, Clock clock) {
            throw new IllegalStateException("Busy connection tasks are not handled with a case statement but with a separate select");
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
        public boolean appliesTo(ScheduledConnectionTask task, Instant now) {
            Instant nextExecutionTimestamp = task.getNextExecutionTimestamp();
            return nextExecutionTimestamp != null && now.isAfter(nextExecutionTimestamp);
        }

        @Override
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Clock clock, String connectionTaskTableName) {
            super.completeFindBySqlBuilder(sqlBuilder, clock, connectionTaskTableName);
            sqlBuilder.appendWhereOrAnd();
            sqlBuilder.append(connectionTaskTableName).append(".status = 0 ");
            sqlBuilder.append(" and (   (").append(connectionTaskTableName).append(".discriminator = ").append(ConnectionTaskImpl.INBOUND_DISCRIMINATOR).append(")");
            sqlBuilder.append("      or ((    not exists (select * from busytask where busytask.connectiontask = ").append(connectionTaskTableName).append(".id and comport is not null)");
            sqlBuilder.append("           and ").append(connectionTaskTableName).append("." + ConnectionTaskFields.COM_PORT.fieldName() + " is null) ");
            sqlBuilder.appendWhereOrAnd();
            sqlBuilder.append(connectionTaskTableName).append(".nextexecutiontimestamp <=").addLong(this.asSeconds(clock.instant()));
            sqlBuilder.append("))");
        }

        @Override
        public void appendBreakdownCaseClause(SqlBuilder sqlBuilder, Clock clock) {
            sqlBuilder.append(" WHEN status = 0");
            sqlBuilder.append("  AND nextexecutiontimestamp <=");
            sqlBuilder.addLong(this.asSeconds(clock.instant()));
            this.appendBreakdownThenClause(sqlBuilder);
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
        public boolean appliesTo(ScheduledConnectionTask task, Instant now) {
            return task.getLastSuccessfulCommunicationEnd() == null && task.getCurrentRetryCount() == 0;
        }

        @Override
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Clock clock, String connectionTaskTableName) {
            super.completeFindBySqlBuilder(sqlBuilder, clock, connectionTaskTableName);
            sqlBuilder.append("and (   not exists (select * from busytask where busytask.connectiontask = ").append(connectionTaskTableName).append(".id and comport is not null)");
            sqlBuilder.append("     or ").append(connectionTaskTableName).append("." + ConnectionTaskFields.COM_PORT.fieldName() + " is null) ");
            sqlBuilder.append("and ").append(connectionTaskTableName).append(".status = 0 ");
            sqlBuilder.append("and ").append(connectionTaskTableName).append(".currentretrycount = 0 ");
            sqlBuilder.append("and ").append(connectionTaskTableName).append(".nextexecutiontimestamp >").addLong(this.asSeconds(clock.instant()));
            sqlBuilder.append("and ").append(connectionTaskTableName).append(".lastsuccessfulcommunicationend is null");
        }

        @Override
        public void appendBreakdownCaseClause(SqlBuilder sqlBuilder, Clock clock) {
            sqlBuilder.append("        WHEN currentretrycount = 0 ");
            sqlBuilder.append("         AND status = 0 ");
            sqlBuilder.append("         AND nextexecutiontimestamp >");
            sqlBuilder.addLong(this.asSeconds(clock.instant()));
            sqlBuilder.append("         AND lastsuccessfulcommunicationend is null");
            this.appendBreakdownThenClause(sqlBuilder);
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
        public boolean appliesTo(ScheduledConnectionTask task, Instant now) {
            return this.strictlyBetween(task.getCurrentRetryCount(), 0, task.getMaxNumberOfTries());
        }

        @Override
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Clock clock, String connectionTaskTableName) {
            super.completeFindBySqlBuilder(sqlBuilder, clock, connectionTaskTableName);
            sqlBuilder.append("and (    not exists (select * from busytask where busytask.connectiontask = ").append(connectionTaskTableName).append(".id and comport is not null)");
            sqlBuilder.append("     and ").append(connectionTaskTableName).append("." + ConnectionTaskFields.COM_PORT.fieldName() + " is null) ");
            sqlBuilder.append("and ").append(connectionTaskTableName).append(".status = 0 ");
            sqlBuilder.append("and ").append(connectionTaskTableName).append(".currentretrycount > 0 ");
            sqlBuilder.append("and ").append(connectionTaskTableName).append(".nextexecutiontimestamp >").addLong(this.asSeconds(clock.instant()));
        }

        @Override
        public void appendBreakdownCaseClause(SqlBuilder sqlBuilder, Clock clock) {
            sqlBuilder.append("        WHEN currentretrycount > 0 ");
            sqlBuilder.append("         AND status = 0 ");
            sqlBuilder.append("         AND nextexecutiontimestamp > ");
            sqlBuilder.addLong(this.asSeconds(clock.instant()));
            this.appendBreakdownThenClause(sqlBuilder);
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
        public boolean appliesTo(ScheduledConnectionTask task, Instant now) {
            return task.lastExecutionFailed()
                    && task.getCurrentRetryCount() == 0;
        }

        @Override
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Clock clock, String connectionTaskTableName) {
            super.completeFindBySqlBuilder(sqlBuilder, clock, connectionTaskTableName);
            sqlBuilder.append("and (    not exists (select * from busytask where busytask.connectiontask = ").append(connectionTaskTableName).append(".id and comport is not null) ");
            sqlBuilder.append("     and ").append(connectionTaskTableName).append("." + ConnectionTaskFields.COM_PORT.fieldName() + " is null) ");
            sqlBuilder.append("and ").append(connectionTaskTableName).append(".status = 0 ");
            sqlBuilder.append("and ").append(connectionTaskTableName).append(".currentretrycount = 0 ");
            sqlBuilder.append("and ").append(connectionTaskTableName).append(".lastExecutionFailed = 1 ");
            sqlBuilder.append("and ").append(connectionTaskTableName).append(".nextexecutiontimestamp >").addLong(this.asSeconds(clock.instant()));
            sqlBuilder.append("and ").append(connectionTaskTableName).append(".lastsuccessfulcommunicationend is not null");
        }

        @Override
        public void appendBreakdownCaseClause(SqlBuilder sqlBuilder, Clock clock) {
            sqlBuilder.append("        WHEN currentretrycount = 0");
            sqlBuilder.append("         AND status = 0 ");
            sqlBuilder.append("         AND lastExecutionFailed = 1");
            sqlBuilder.append("         AND nextexecutiontimestamp >");
            sqlBuilder.addLong(this.asSeconds(clock.instant()));
            sqlBuilder.append("         AND lastsuccessfulcommunicationend is not null");
            this.appendBreakdownThenClause(sqlBuilder);
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
        public boolean appliesTo(ScheduledConnectionTask task, Instant now) {
            Instant nextExecutionTimestamp = task.getNextExecutionTimestamp();
            return nextExecutionTimestamp != null && nextExecutionTimestamp.isAfter(now);
        }

        @Override
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Clock clock, String connectionTaskTableName) {
            super.completeFindBySqlBuilder(sqlBuilder, clock, connectionTaskTableName);
            sqlBuilder.append("and (    not exists (select * from busytask where busytask.connectiontask = ").append(connectionTaskTableName).append(".id and busytask.comport is not null) ");
            sqlBuilder.append("     and ").append(connectionTaskTableName).append("." + ConnectionTaskFields.COM_PORT.fieldName() + " is null) ");
            sqlBuilder.append("and ").append(connectionTaskTableName).append(".status = 0 ");
            sqlBuilder.append("and ").append(connectionTaskTableName).append(".currentretrycount = 0 ");
            sqlBuilder.append("and ").append(connectionTaskTableName).append(".lastExecutionFailed = 0 ");
            sqlBuilder.append("and ").append(connectionTaskTableName).append(".nextexecutiontimestamp >").addLong(this.asSeconds(clock.instant()));
            sqlBuilder.append("and ").append(connectionTaskTableName).append(".lastsuccessfulcommunicationend is not null");
        }

        @Override
        public void appendBreakdownCaseClause(SqlBuilder sqlBuilder, Clock clock) {
            sqlBuilder.append("        WHEN currentretrycount = 0");
            sqlBuilder.append("         AND status = 0 ");
            sqlBuilder.append("         AND lastExecutionFailed = 0");
            sqlBuilder.append("         AND nextexecutiontimestamp >");
            sqlBuilder.addLong(this.asSeconds(clock.instant()));
            sqlBuilder.append("         AND lastsuccessfulcommunicationend is not null");
            this.appendBreakdownThenClause(sqlBuilder);
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
        public boolean appliesTo(ScheduledConnectionTask task, Instant now) {
            return false;
        }

        @Override
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Clock clock, String connectionTaskTableName) {
            super.completeFindBySqlBuilder(sqlBuilder, clock, connectionTaskTableName);
            sqlBuilder.append("and (1 = 0)");
        }

        @Override
        public void appendBreakdownCaseClause(SqlBuilder sqlBuilder, Clock clock) {
        }
    };

    public static final String BUSY_TASK_ALIAS_NAME = "busytask";

    /**
     * Gets the {@link TaskStatus} that applies to the specified {@link ScheduledConnectionTask}.
     *
     * @param task The ServerOutboundConnectionTask
     * @return The applicable TaskStatus
     */
    public static TaskStatus getApplicableStatusFor(ScheduledConnectionTask task, Instant now) {
        /* Implementation note:
         * Changing the order of the enum values
         * will/can have an effect on the outcome. */
        for (ServerConnectionTaskStatus serverConnectionTaskStatus : values()) {
            if (serverConnectionTaskStatus.appliesTo(task, now)) {
                return serverConnectionTaskStatus.getPublicStatus();
            }
        }
        return TaskStatus.initial();
    }

    /**
     * Converts the {@link TaskStatus} to the corresponding {@link ServerConnectionTaskStatus}.
     *
     * @param taskStatus The TaskStatus
     * @return The corresponding ServerConnectionTaskStatus
     */
    public static ServerConnectionTaskStatus forTaskStatus(TaskStatus taskStatus) {
        for (ServerConnectionTaskStatus serverComTaskStatus : values()) {
            if (serverComTaskStatus.getPublicStatus().equals(taskStatus)) {
                return serverComTaskStatus;
            }
        }
        throw new IllegalArgumentException("unrecognized enum value " + taskStatus);
    }

    /**
     * Returns the public counterpart of this ServerConnectionTaskStatus.
     *
     * @return The public counterpart
     */
    public abstract TaskStatus getPublicStatus();

    /**
     * Checks if this ServerConnectionTaskStatus
     * applies to the {@link ScheduledConnectionTask}.
     *
     * @param task The ConnectionTaskExecutionAspects
     * @param now  The current time
     * @return <code>true</code> iff this ServerConnectionTaskStatus applies to the ServerOutboundConnectionTask
     */
    public abstract boolean appliesTo(ScheduledConnectionTask task, Instant now);

    public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Clock clock, String connectionTaskTableName) {
        sqlBuilder.appendWhereOrAnd();
        sqlBuilder.append(connectionTaskTableName);
        sqlBuilder.append(".obsolete_date is null ");
    }

    public abstract void appendBreakdownCaseClause(SqlBuilder sqlBuilder, Clock clock);

    protected void appendBreakdownThenClause(SqlBuilder sqlBuilder) {
        sqlBuilder.append(" THEN '");
        sqlBuilder.append(this.name());
        sqlBuilder.append("'");
    }

    /**
     * Builds the Condition that is necessary to select
     * all {@link ConnectionTask}s
     * that are in this status.
     *
     * @return The Condition
     */
    public Condition condition() {
        return where("obsoleteDate").isNull();
    }

    protected long asSeconds(Instant date) {
        if (date == null) {
            return 0;
        } else {
            return date.getEpochSecond();
        }
    }

    protected boolean strictlyBetween(int aNumber, int lower, int upper) {
        return lower < aNumber && aNumber < upper;
    }

}