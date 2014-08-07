package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.impl.ClauseAwareSqlBuilder;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;

import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.time.Clock;
import org.joda.time.DateTimeConstants;

import java.util.Date;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Represents the counterpart of {@link TaskStatus} for {@link ScheduledConnectionTask}s
 * and adds behavior that is reserved for server components.
 */
public enum ServerConnectionTaskStatus {

    /**
     * @see TaskStatus#OnHold
     */
    Paused {
        @Override
        public TaskStatus getPublicStatus() {
            return TaskStatus.OnHold;
        }

        @Override
        public boolean appliesTo(ScheduledConnectionTask task, Date now) {
            return !task.getStatus().equals(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE) || task.getNextExecutionTimestamp() == null;
        }

        @Override
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Clock clock) {
            super.completeFindBySqlBuilder(sqlBuilder, clock);
            sqlBuilder.append("and (not exists (select * from ");
            sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
            sqlBuilder.append(" cte where ");
            sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
            sqlBuilder.append(".id = cte.connectiontask and comport is not null) ");
            sqlBuilder.append("and ");
            sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
            sqlBuilder.append(".comserver is null) ");
            sqlBuilder.append("and (   (discriminator =");
            sqlBuilder.addObject(ConnectionTaskImpl.INBOUND_DISCRIMINATOR);
            sqlBuilder.append(" and status = 1)");
            sqlBuilder.append("     or (discriminator =");
            sqlBuilder.addObject(ConnectionTaskImpl.SCHEDULED_DISCRIMINATOR);
            sqlBuilder.append(" and (status = 1 or ");
            sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
            sqlBuilder.append(".nextExecutionTimestamp is null)))");
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
        public boolean appliesTo(ScheduledConnectionTask task, Date now) {
            if (task.isExecuting()) {
                return true;
            }
            else {
                for (ComTaskExecution comTaskExecution : task.getDevice().getComTaskExecutions()) {
                    if(comTaskExecution.isExecuting()){
                        return true;
                    }
                }
                return false;
            }
        }

        @Override
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Clock clock) {
            super.completeFindBySqlBuilder(sqlBuilder, clock);
            sqlBuilder.append("and (exists (select * from ");
            sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
            sqlBuilder.append(" cte where ");
            sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
            sqlBuilder.append(".id = cte.connectiontask and comport is not null)");
            sqlBuilder.append("     or " + TableSpecs.DDC_CONNECTIONTASK.name() + ".comserver is not null)");
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
        public boolean appliesTo(ScheduledConnectionTask task, Date now) {
            Date nextExecutionTimestamp = task.getNextExecutionTimestamp();
            return nextExecutionTimestamp != null && now.getTime() >= nextExecutionTimestamp.getTime();
        }

        @Override
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Clock clock) {
            super.completeFindBySqlBuilder(sqlBuilder, clock);
            sqlBuilder.append("and (not exists (select * from ");
            sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
            sqlBuilder.append(" cte where ");
            sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
            sqlBuilder.append(".id = cte.connectiontask and comport is not null) ");
            sqlBuilder.append("     and " + TableSpecs.DDC_CONNECTIONTASK.name() + ".comserver is null) ");
            sqlBuilder.append("and status = 0 ");
            sqlBuilder.append("and nextexecutiontimestamp <=");
            sqlBuilder.addLong(this.asSeconds(clock.now()));
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
        public boolean appliesTo(ScheduledConnectionTask task, Date now) {
            return task.getLastSuccessfulCommunicationEnd() == null && task.getCurrentRetryCount() == 0;
        }

        @Override
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Clock clock) {
            super.completeFindBySqlBuilder(sqlBuilder, clock);
            sqlBuilder.append("and (not exists (select * from ");
            sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
            sqlBuilder.append(" cte where ");
            sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
            sqlBuilder.append(".id = cte.connectiontask and comport is not null)");
            sqlBuilder.append("     and " + TableSpecs.DDC_CONNECTIONTASK.name() + ".comserver is null) ");
            sqlBuilder.append("and status = 0 ");
            sqlBuilder.append("and currentretrycount = 0 ");
            sqlBuilder.append("and nextexecutiontimestamp >");
            sqlBuilder.addLong(this.asSeconds(clock.now()));
            sqlBuilder.append("and lastsuccessfulcommunicationend is null");
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
        public boolean appliesTo(ScheduledConnectionTask task, Date now) {
            return this.strictlyBetween(task.getCurrentRetryCount(), 0, task.getMaxNumberOfTries());
        }

        @Override
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Clock clock) {
            super.completeFindBySqlBuilder(sqlBuilder, clock);
            sqlBuilder.append("and (not exists (select * from ");
            sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
            sqlBuilder.append(" cte where ");
            sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
            sqlBuilder.append(".id = cte.connectiontask and comport is not null)");
            sqlBuilder.append(" and ");
            sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
            sqlBuilder.append(".comserver is null) ");
            sqlBuilder.append("and status = 0 ");
            sqlBuilder.append("and currentretrycount > 0 ");
            sqlBuilder.append("and nextexecutiontimestamp >");
            sqlBuilder.addLong(this.asSeconds(clock.now()));
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
        public boolean appliesTo(ScheduledConnectionTask task, Date now) {
            return task.lastExecutionFailed()
                    && task.getCurrentRetryCount() == 0;
        }

        @Override
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Clock clock) {
            super.completeFindBySqlBuilder(sqlBuilder, clock);
            sqlBuilder.append("and (not exists (select * from ");
            sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
            sqlBuilder.append(" cte where ");
            sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
            sqlBuilder.append(".id = cte.connectiontask and comport is not null) ");
            sqlBuilder.append("and ");
            sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
            sqlBuilder.append(".comserver is null) ");
            sqlBuilder.append("and status = 0 ");
            sqlBuilder.append("and currentretrycount = 0 ");
            sqlBuilder.append("and lastExecutionFailed = 1 ");
            sqlBuilder.append("and nextexecutiontimestamp >");
            sqlBuilder.addLong(this.asSeconds(clock.now()));
            sqlBuilder.append("and lastsuccessfulcommunicationend is not null");
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
        public boolean appliesTo(ScheduledConnectionTask task, Date now) {
            Date nextExecutionTimestamp = task.getNextExecutionTimestamp();
            return nextExecutionTimestamp != null && nextExecutionTimestamp.getTime() >= now.getTime();
        }

        @Override
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Clock clock) {
            super.completeFindBySqlBuilder(sqlBuilder, clock);
            sqlBuilder.append("and (not exists (select * from ");
            sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
            sqlBuilder.append(" cte where ");
            sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
            sqlBuilder.append(".id = cte.connectiontask and comport is not null) ");
            sqlBuilder.append("and ");
            sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
            sqlBuilder.append(".comserver is null) ");
            sqlBuilder.append("and status = 0 ");
            sqlBuilder.append("and currentretrycount = 0 ");
            sqlBuilder.append("and lastExecutionFailed = 0 ");
            sqlBuilder.append("and nextexecutiontimestamp >");
            sqlBuilder.addLong(this.asSeconds(clock.now()));
            sqlBuilder.append("and lastsuccessfulcommunicationend is not null");
        }

    };

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
     * @return <code>true</code> iff this ServerConnectionTaskStatus applies to the ServerOutboundConnectionTask
     */
    public abstract boolean appliesTo(ScheduledConnectionTask task, Date now);

    public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Clock clock) {
        sqlBuilder.appendWhereOrAnd();
        sqlBuilder.append("obsolete_date is null ");
    }

    /**
     * Builds the Condition that is necessary to select
     * all {@link com.energyict.mdc.device.data.tasks.ConnectionTask}s
     * that are in this status.
     *
     * @return The Condition
     */
    public Condition condition() {
        return where("obsoleteDate").isNull();
    }

    protected long asSeconds(Date date) {
        if (date == null) {
            return 0;
        }
        else {
            return date.getTime() / DateTimeConstants.MILLIS_PER_SECOND;
        }
    }

    protected boolean strictlyBetween (int aNumber, int lower, int upper) {
        return lower < aNumber && aNumber < upper;
    }

    /**
     * Gets the {@link com.energyict.mdc.device.data.tasks.TaskStatus} that applies to the specified {@link ScheduledConnectionTask}.
     *
     * @param task The ServerOutboundConnectionTask
     * @return The applicable TaskStatus
     */
    public static TaskStatus getApplicableStatusFor(ScheduledConnectionTask task, Date now) {
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

}