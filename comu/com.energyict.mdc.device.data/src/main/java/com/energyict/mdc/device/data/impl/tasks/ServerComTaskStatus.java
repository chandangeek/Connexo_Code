/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ServerComTaskExecution;
import com.energyict.mdc.common.tasks.TaskStatus;
import com.energyict.mdc.device.data.impl.ClauseAwareSqlBuilder;

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
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
            super.completeFindBySqlBuilder(sqlBuilder, now);
            sqlBuilder.append("and cte.onhold = 0 and ");
            isExecuting(sqlBuilder, now);
        }

        @Override
        public void completeCountSqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
            sqlBuilder.appendWhereOrAnd();
            sqlBuilder.append("cte.onhold = 0 and ((cte.comport is not null) or ((cte.thereisabusytask is not null) and cte.nextexecutiontimestamp <=");
            // Merge feature/CXO-2099: add cte.onhold = 0 to above line
            sqlBuilder.addLong(asSeconds(now));
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
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
            super.completeFindBySqlBuilder(sqlBuilder, now);
            sqlBuilder.append(isNotPriorityTask() +
                    "and cte.onhold = 0 and ((cte.comport is null) and " +
                    " (not exists (select * from busytask where busytask.comport is not null and busytask.connectiontask = cte.connectiontask " +
                    " and busytask.lastcommunicationstart <= cte.lastexecutiontimestamp " +
                    " and busytask.lastCommunicationStart > cte.nextexecutiontimestamp)) " +
                    " and cte.nextExecutionTimestamp is not null and cte.nextexecutiontimestamp <=");
            sqlBuilder.addLong(asSeconds(now));
            sqlBuilder.append(")");
        }

        @Override
        public void completeCountSqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
            sqlBuilder.appendWhereOrAnd();
            sqlBuilder.append("cte.onhold = 0 and cte.comport is null and cte.thereisabusytask is null and cte.nextexecutiontimestamp <=");
            // Merge feature/CXO-2099: add cte.onhold = 0 to above line
            sqlBuilder.addLong(asSeconds(now));
        }
    },

    /**
     * @see TaskStatus#Pending
     */
    PendingWithPriority {
        @Override
        public TaskStatus getPublicStatus() {
            return TaskStatus.PendingWithPriority;
        }

        @Override
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
            super.completeFindBySqlBuilder(sqlBuilder, now);
            sqlBuilder.append("and cte.onhold = 0 and ((cte.comport is null) and " +
                    " (not exists (select * from busytask where busytask.comport is not null and busytask.connectiontask = cte.connectiontask " +
                    " and busytask.lastcommunicationstart <= cte.lastexecutiontimestamp " +
                    " and busytask.lastCommunicationStart > cte.nextexecutiontimestamp)) " +
                    " and cte.nextExecutionTimestamp is not null and cte.nextexecutiontimestamp <=");
            sqlBuilder.addLong(asSeconds(now));
            sqlBuilder.append(")");
        }

        @Override
        public void completeCountSqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
            sqlBuilder.appendWhereOrAnd();
            sqlBuilder.append("cte.onhold = 0 and cte.comport is null and comtaskexecution = cte.id and cte.thereisabusytask is null and cte.nextexecutiontimestamp <=");
            // Merge feature/CXO-2099: add cte.onhold = 0 to above line
            sqlBuilder.addLong(asSeconds(now));
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
            sqlBuilder.addLong(asSeconds(now));
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
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
            super.completeFindBySqlBuilder(sqlBuilder, now);
            this.completeCountSqlBuilder(sqlBuilder, now);
        }

        @Override
        public void completeCountSqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
            sqlBuilder.appendWhereOrAnd();
            sqlBuilder.append("cte.onhold = 0 ");
            sqlBuilder.append(ServerComTaskStatus.isNotPriorityTask());
            sqlBuilder.append("and cte.nextexecutiontimestamp >");
            sqlBuilder.addLong(asSeconds(now));
            sqlBuilder.append("and cte.comport is null ");
            sqlBuilder.append("and cte.currentretrycount > 0");  // currentRetryCount is only incremented when task fails. It is reset to 0 when the maxTries is reached
        }
    },

    /**
     * @see TaskStatus#Retrying
     */
    RetryingWithPriority {
        @Override
        public TaskStatus getPublicStatus() {
            return TaskStatus.RetryingWithPriority;
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
            sqlBuilder.addLong(asSeconds(now));
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
        public void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
            super.completeFindBySqlBuilder(sqlBuilder, now);
            this.completeCountSqlBuilder(sqlBuilder, now);
        }

        @Override
        public void completeCountSqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
            sqlBuilder.appendWhereOrAnd();
            sqlBuilder.append("cte.onhold = 0 ");
            sqlBuilder.append("and cte.lastSuccessfulCompletion is not null ");
            sqlBuilder.append("and cte.LASTEXECUTIONTIMESTAMP > lastSuccessfulCompletion ");
            sqlBuilder.append("and cte.lastExecutionFailed = 1 ");
            sqlBuilder.append("and cte.currentretrycount = 0");
        }
    },

    /**
     * @see TaskStatus#Waiting
     */
    WaitingWithPriority {
        @Override
        public TaskStatus getPublicStatus() {
            return TaskStatus.WaitingWithPriority;
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
            sqlBuilder.addLong(asSeconds(now));
            sqlBuilder.append("))");
        }
    },
    Waiting {
        @Override
        public TaskStatus getPublicStatus() {
            return TaskStatus.Waiting;
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
            sqlBuilder.append(ServerComTaskStatus.isNotPriorityTask());
            sqlBuilder.append("and cte.comport is null ");
            sqlBuilder.append("and cte.lastExecutionFailed = 0 ");
            sqlBuilder.append("and cte.currentretrycount = 0 ");
            sqlBuilder.append("and (cte.lastExecutionTimestamp is null or cte.lastSuccessfulCompletion is not null) ");
            sqlBuilder.append("and (cte.nextexecutiontimestamp IS NULL OR (cte.nextexecutiontimestamp >");
            sqlBuilder.addLong(asSeconds(now));
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

    private static long asSeconds(Instant date) {
        if (date == null) {
            return 0;
        } else {
            return date.getEpochSecond();
        }
    }

    protected static boolean plannedAndNextExecTimeStampForWaitingStates(ServerComTaskExecution task, Instant now) {
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

    public final void completeFindBySqlBuilder(ClauseAwareSqlBuilder sqlBuilder, Clock clock) {
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
        if (task.isOnHold()) {
            return TaskStatus.OnHold;
        }
        if (task.isExecuting()) {
            return TaskStatus.Busy;
        }
        int currentTryCount = task.getCurrentTryCount();
        Instant nextExecutionTimestamp = task.getNextExecutionTimestamp();
        if (nextExecutionTimestamp != null) {
            if (now.isAfter(nextExecutionTimestamp)) {
                if (isPriorityTask(task)) {
                    return TaskStatus.PendingWithPriority;
                }
                return TaskStatus.Pending;
            } else if (currentTryCount > 1) {
                if (isPriorityTask(task)) {
                    return TaskStatus.RetryingWithPriority;
                }
                return TaskStatus.Retrying;
            }
        }
        if (currentTryCount == 1) {
            Instant lastExecutionStartTimestamp = task.getLastExecutionStartTimestamp();
            if (!task.isLastExecutionFailed()) {
                if ((lastExecutionStartTimestamp == null || task.getLastSuccessfulCompletionTimestamp() != null)
                        && plannedAndNextExecTimeStampForWaitingStates(task, now)) {
                    if (isPriorityTask(task)) {
                        return TaskStatus.WaitingWithPriority;
                    }
                    return TaskStatus.Waiting;
                }
            } else {
                if (lastExecutionStartTimestamp != null) {
                    Instant lastSuccessfulCompletionTimestamp = task.getLastSuccessfulCompletionTimestamp();
                    if (lastSuccessfulCompletionTimestamp != null
                            && lastExecutionStartTimestamp.isAfter(lastSuccessfulCompletionTimestamp)) {
                        return TaskStatus.Failed;
                    }
                }
            }
        }
        return TaskStatus.NeverCompleted;
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

    private static boolean isPriorityTask(ServerComTaskExecution task) {
        return task.shouldExecuteWithPriority();
    }

    private static String isNotPriorityTask() {
        return " and (not exists (select * from DDC_HIPRIOCOMTASKEXEC where comtaskexecution is not null and comtaskexecution = cte.id )) ";
    }

    private static void isExecuting(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
        sqlBuilder.append("(cte.comport is not null or ");
        isConnectionExecuting(sqlBuilder, now);
        sqlBuilder.append(")");
    }

    private static void isConnectionExecuting(ClauseAwareSqlBuilder sqlBuilder, Instant now) {
        sqlBuilder.append(
                " exists (select * from busytask where busytask.comport is not null and busytask.connectiontask = cte.connectiontask " +
                        " AND busytask.lastcommunicationstart IS NOT NULL " +
                        " AND busytask.lastcommunicationstart <= cte.lastexecutiontimestamp " +
                        " AND (cte.ignorenextexecspecs = 1 " +
                        " OR (cte.nextexecutiontimestamp IS NOT NULL " +
                        " AND cte.nextexecutiontimestamp <= ");
        sqlBuilder.addLong(asSeconds(now));
        sqlBuilder.append(" AND busytask.lastcommunicationstart > cte.nextexecutiontimestamp)))");
    }

}