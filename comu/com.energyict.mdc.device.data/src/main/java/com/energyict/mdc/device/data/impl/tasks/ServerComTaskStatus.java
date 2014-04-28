package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.common.SqlBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.TaskStatus;

import java.util.Date;

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
        public TaskStatus getPublicStatus () {
            return TaskStatus.OnHold;
        }

        @Override
        public boolean appliesTo(ServerComTaskExecution task, Date now) {
            return task.isOnHold();
        }

        @Override
        public void completeFindBySqlBuilder (SqlBuilder sqlBuilder, long now) {
            super.completeFindBySqlBuilder(sqlBuilder, now);
            sqlBuilder.append("and nextExecutionTimestamp is null");
        }
    },
    /**
     * @see TaskStatus#Busy
     */
    Busy {
        @Override
        public TaskStatus getPublicStatus () {
            return TaskStatus.Busy;
        }

        @Override
        public boolean appliesTo(ServerComTaskExecution task, Date now) {
            return task.isExecuting() && !task.isOnHold();
        }

        @Override
        public void completeFindBySqlBuilder (SqlBuilder sqlBuilder, long now) {
            super.completeFindBySqlBuilder(sqlBuilder, now);
            sqlBuilder.append("and ((comport is not null) or " +
                    "((exists (select * from mdcconnectiontask where mdcconnectiontask.comserver is not null and mdcconnectiontask.id = mdccomtaskexec.connectiontask))" +
                    " and mdccomtaskexec.nextExecutionTimestamp is not null and mdccomtaskexec.nextexecutiontimestamp <= ?))");
            sqlBuilder.bindLong(now);
        }
    },

    /**
     * @see TaskStatus#Pending
     */
    Pending {
        @Override
        public TaskStatus getPublicStatus () {
            return TaskStatus.Pending;
        }

        @Override
        public boolean appliesTo(ServerComTaskExecution task, Date now) {
            Date nextExecutionTimestamp = task.getNextExecutionTimestamp();
            return !task.isExecuting()
                    && nextExecutionTimestamp != null
                    && now.compareTo(nextExecutionTimestamp) >= 0;
        }

        @Override
        public void completeFindBySqlBuilder (SqlBuilder sqlBuilder, long now) {
            super.completeFindBySqlBuilder(sqlBuilder, now);
            sqlBuilder.append("and comport is null and not exists (select * from mdcconnectiontask where mdcconnectiontask.comserver is not null and mdcconnectiontask.id = mdccomtaskexec.connectiontask)");
            sqlBuilder.append("and nextexecutiontimestamp <= ?");
            sqlBuilder.bindLong(now);
        }
    },

    /**
     * @see TaskStatus#NeverCompleted
     */
    NeverCompleted {
        @Override
        public TaskStatus getPublicStatus () {
            return TaskStatus.NeverCompleted;
        }

        @Override
        public boolean appliesTo(ServerComTaskExecution task, Date now) {
            Date nextExecutionTimestamp = task.getNextExecutionTimestamp();
            return task.getLastSuccessfulCompletionTimestamp() == null
                    && task.getExecutingComPort() == null
                    && task.getCurrentTryCount() == 0
                    && nextExecutionTimestamp != null
                    && nextExecutionTimestamp.after(now);
        }

        @Override
        public void completeFindBySqlBuilder (SqlBuilder sqlBuilder, long now) {
            super.completeFindBySqlBuilder(sqlBuilder, now);
            sqlBuilder.append("and lastSuccessfulCompletion is null ");
            sqlBuilder.append("and comport is null ");
            sqlBuilder.append("and currentretrycount = 0 ");
            sqlBuilder.append("and nextExecutionTimestamp is not null ");
            sqlBuilder.append("and nextExecutionTimestamp > ?");
            sqlBuilder.bindLong(now);
        }
    },

    /**
     * @see TaskStatus#Retrying
     */
    Retrying {
        @Override
        public TaskStatus getPublicStatus () {
            return TaskStatus.Retrying;
        }

        @Override
        public boolean appliesTo(ServerComTaskExecution task, Date now) {
            int retryCount = task.getCurrentTryCount() - 1;
            return task.getNextExecutionTimestamp() != null
                && !task.isExecuting()
                && retryCount > 0;
        }

        @Override
        public void completeFindBySqlBuilder (SqlBuilder sqlBuilder, long now) {
            super.completeFindBySqlBuilder(sqlBuilder, now);
            sqlBuilder.append("and nextexecutiontimestamp > ? ");
            sqlBuilder.bindLong(now);
            sqlBuilder.append("and comport is null ");
            sqlBuilder.append("and currentretrycount > 0");  // currentRetryCount is only incremented when task fails. It is reset to 0 when the maxTries is reached
        }
    },

    /**
     * @see TaskStatus#Failed
     */
    Failed {
        @Override
        public TaskStatus getPublicStatus () {
            return TaskStatus.Failed;
        }

        @Override
        public boolean appliesTo(ServerComTaskExecution task, Date now) {
            Date nextExecutionTimestamp = task.getNextExecutionTimestamp();
            int retryCount = task.getCurrentTryCount() - 1;
            return nextExecutionTimestamp != null
                && nextExecutionTimestamp.after(now)
                && task.getLastSuccessfulCompletionTimestamp() != null
                && task.lastExecutionFailed()
                && retryCount == 0;
        }

        @Override
        public void completeFindBySqlBuilder (SqlBuilder sqlBuilder, long now) {
            super.completeFindBySqlBuilder(sqlBuilder, now);
            sqlBuilder.append("and nextExecutionTimestamp is not null ");
            sqlBuilder.append("and nextExecutionTimestamp > ? ");
            sqlBuilder.bindLong(now);
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
        public TaskStatus getPublicStatus () {
            return TaskStatus.Waiting;
        }

        @Override
        public boolean appliesTo(ServerComTaskExecution task, Date now) {
            Date nextExecutionTimestamp = task.getNextExecutionTimestamp();
            return !task.isExecuting()
                    && task.getLastSuccessfulCompletionTimestamp() != null
                    && nextExecutionTimestamp != null
                    && nextExecutionTimestamp.after(now)
                    && !task.lastExecutionFailed();
        }

        @Override
        public void completeFindBySqlBuilder (SqlBuilder sqlBuilder, long now) {
            super.completeFindBySqlBuilder(sqlBuilder, now);
            sqlBuilder.append("and comport is null ");
            sqlBuilder.append("and lastSuccessfulCompletion is not null ");
            sqlBuilder.append("and nextexecutiontimestamp > ?");
            sqlBuilder.bindLong(now);
            sqlBuilder.append("and lastExecutionFailed = 0");
        }
    };

    /**
     * Returns the public counterpart of this ServerTaskStatus.
     *
     * @return The public counterpart
     */
    public abstract TaskStatus getPublicStatus ();

    /**
     * Checks if this ServerTaskStatus
     * applies to the {@link ComTaskExecution}.
     *
     *
     * @param task The ComTaskExecution
     * @param now
     * @return <code>true</code> iff this ServerTaskStatus applies to the ComTaskExecution
     */
    public abstract boolean appliesTo(ServerComTaskExecution task, Date now);

    public void completeFindBySqlBuilder (SqlBuilder sqlBuilder, long now) {
        sqlBuilder.append("obsolete_date is null ");
    }

    /**
     * Gets the {@link TaskStatus} that applies to the specified {@link ComTaskExecution}.
     *
     * @param task The ComTaskExecution
     * @param now
     * @return The applicable TaskStatus
     */
    public static TaskStatus getApplicableStatusFor(ServerComTaskExecution task, Date now) {
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
    public static ServerComTaskStatus forTaskStatus (TaskStatus taskStatus) {
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