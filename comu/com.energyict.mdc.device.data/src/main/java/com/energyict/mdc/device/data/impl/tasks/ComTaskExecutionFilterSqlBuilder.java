/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.streams.FancyJoiner;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.ConnectionTypePluggableClass;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.TaskStatus;
import com.energyict.mdc.common.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.common.tasks.history.CompletionCode;
import com.energyict.mdc.device.data.impl.ClauseAwareSqlBuilder;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;

import java.time.Clock;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Builds the SQL query that finds all {@link ComTaskExecution}s
 * that match a {@link ComTaskExecutionFilterSpecification}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-22 (15:07)
 */
public class ComTaskExecutionFilterSqlBuilder extends AbstractComTaskExecutionFilterSqlBuilder {

    private static final String BUSY_ALIAS_NAME = ServerConnectionTaskStatus.BUSY_TASK_ALIAS_NAME;

    private Set<ServerComTaskStatus> taskStatuses;
    private Set<CompletionCode> completionCodes;
    public Interval lastSessionStart = null;
    public Interval lastSessionEnd = null;
    private final Set<ConnectionTypePluggableClass> connectionTypes;
    private List<Long> connectionTasksIds;

    public ComTaskExecutionFilterSqlBuilder(ComTaskExecutionFilterSpecification filterSpecification, Clock clock, QueryExecutor<Device> queryExecutor) {
        super(clock, filterSpecification, queryExecutor);
        this.validate(filterSpecification);
        this.copyTaskStatuses(filterSpecification);
        this.completionCodes = EnumSet.noneOf(CompletionCode.class);
        this.completionCodes.addAll(filterSpecification.latestResults);
        this.lastSessionStart = filterSpecification.lastSessionStart;
        this.lastSessionEnd = filterSpecification.lastSessionEnd;
        this.connectionTasksIds = filterSpecification.connectionMethods;
        this.connectionTypes = new HashSet<>(filterSpecification.connectionTypes);
    }

    private void copyTaskStatuses(ComTaskExecutionFilterSpecification filterSpecification) {
        this.taskStatuses = EnumSet.noneOf(ServerComTaskStatus.class);
        for (TaskStatus taskStatus : filterSpecification.taskStatuses) {
            this.taskStatuses.add(ServerComTaskStatus.forTaskStatus(taskStatus));
        }
    }

    /**
     * Validates that all specification are correct and coherent,
     * i.e. that none of the specifications contradict each other.
     *
     * @param filterSpecification The ComTaskExecutionFilterSpecification
     * @throws IllegalArgumentException Thrown when the specifications are not valid
     */
    protected void validate(ComTaskExecutionFilterSpecification filterSpecification) throws IllegalArgumentException {
        if (!filterSpecification.latestResults.isEmpty()
                && !this.isNull(filterSpecification.lastSessionEnd)) {
            throw new IllegalArgumentException("Latest result and last session end in interval cannot be combined");
        }
        if (!filterSpecification.comTasks.isEmpty()
                && !filterSpecification.comSchedules.isEmpty()) {
            throw new IllegalArgumentException("Communication tasks and communication schedules cannot be combined");
        }
    }

    public ClauseAwareSqlBuilder build(SqlBuilder sqlBuilder) {
        return this.build(sqlBuilder, communicationTaskAliasName());
    }

    public ClauseAwareSqlBuilder build(SqlBuilder sqlBuilder, String communicationTaskAliasName) {
        ClauseAwareSqlBuilder actualBuilder = this.newActualBuilder();
        WithClauses.BUSY_CONNECTION_TASK.append(actualBuilder, BUSY_ALIAS_NAME);
        StringBuilder sqlStartClause = new StringBuilder(sqlBuilder.toString());
        sqlStartClause.insert(sqlStartClause.indexOf("from"), " , CASE WHEN bt.connectiontask IS NULL THEN 0 ELSE 1 END as busytask_exists ");
        this.append(", allctdata as (");
        this.append(sqlStartClause);
        this.appendDeviceAndBusyTaskStateJoinClauses(communicationTaskAliasName);
        Iterator<ServerComTaskStatus> statusIterator = this.taskStatuses.iterator();
        this.getActualBuilder().appendWhereOrAnd();
        if (statusIterator.hasNext()) {
            this.appendWhereClause(statusIterator.next());
            this.append(getBuilderForRestrictedStages().getText());
        }
        this.append(" ) ");
        if (statusIterator.hasNext()) {
            sqlStartClause.replace(sqlStartClause.indexOf(" , CASE"), sqlStartClause.length(), " FROM allctdata " + communicationTaskAliasName + " ");
            this.append(sqlStartClause);
            this.getActualBuilder().resetToWith();

            this.getActualBuilder().appendWhereOrAnd();
            while (statusIterator.hasNext()) {
                this.appendWhereClause(statusIterator.next());
                if (statusIterator.hasNext()) {
                    this.or();
                    this.getActualBuilder().resetToWhere();
                }
            }
        }

        if (this.taskStatuses.isEmpty()) {
            this.appendNonStatusWhereClauses();
        }
        if (!this.connectionTasksIds.isEmpty()) {
            this.appendWhereOrAnd();
            this.append("cte.connectiontask IN (" + connectionTasksIds.stream().collect(FancyJoiner.joining(",", "")) + ")");
        }

        return this.getActualBuilder();
    }

    public SqlBuilder build(DataMapper<ComTaskExecution> dataMapper, int pageStart, int pageSize) {
        ClauseAwareSqlBuilder sqlBuilder = build(dataMapper.builder(communicationTaskAliasName()));
        sqlBuilder.append(" order by lastexecutiontimestamp desc");
        return sqlBuilder.asPageBuilder(pageStart, pageStart + pageSize);
    }

    @Override
    protected void appendWhereClause(ServerComTaskStatus taskStatus) {
        super.appendWhereClause(taskStatus);
        this.appendDeviceInGroupSql();
    }

    @Override
    protected void appendWhereClauseWithEmptyStatus(ServerComTaskStatus taskStatus) {
        super.appendWhereClauseWithEmptyStatus(taskStatus);
        this.appendDeviceInGroupSql();
    }

    @Override
    protected void appendStatusWhereClauses(ServerComTaskStatus taskStatus) {
        taskStatus.completeFindBySqlBuilder(this.getActualBuilder(), this.getClock());
    }

    @Override
    protected void appendNonStatusWhereClauses() {
        super.appendNonStatusWhereClauses();
        this.appendCompletionCodeClause();
        this.appendLastSessionIntervalWhereClause();
        this.appendDeviceInGroupSql();
        this.appendConnectionTypeSql();
    }

    private void appendCompletionCodeClause() {
        if (this.requiresCompletionCodeClause()) {
            this.appendWhereOrAnd();
            this.append("cte.lastsess_highestpriocomplcode in (");
            boolean notFirst = false;
            for (CompletionCode completionCode : this.completionCodes) {
                if (notFirst) {
                    this.append(",");
                }
                this.addInt(completionCode.dbValue());
                notFirst = true;
            }
            this.append(")");
        }
    }

    private boolean requiresCompletionCodeClause() {
        return !this.completionCodes.isEmpty();
    }

    private void appendLastSessionIntervalWhereClause() {
        if (!this.isNull(this.lastSessionStart) || !this.isNull(this.lastSessionEnd)) {
            this.appendWhereOrAnd();
            this.append(" exists (select * from ");
            this.append(TableSpecs.DDC_COMSESSION.name());
            this.append(" cs, ");
            this.append(TableSpecs.DDC_COMTASKEXECSESSION.name());
            this.append(" ctes where ctes.comtaskexec = ");
            this.append(communicationTaskAliasName());
            this.append(".id and ctes.comsession = cs.id ");
            if (!this.isNull(this.lastSessionStart)) {
                this.appendWhereOrAnd();
                this.appendIntervalWhereClause("cte", "LASTEXECUTIONTIMESTAMP", this.lastSessionStart, IntervalBindStrategy.SECONDS);
            }
            if (!this.isNull(this.lastSessionEnd)) {
                this.append("and (ctes.successindicator =");
                this.addInt(ComTaskExecutionSession.SuccessIndicator.Success.ordinal());
                this.appendWhereOrAnd();
                this.appendIntervalWhereClause("cte", "LASTSUCCESSFULCOMPLETION", this.lastSessionEnd, IntervalBindStrategy.SECONDS);
                this.append(" )");
            }
            this.append(")");
        }
    }

    private void appendConnectionTypeSql() {
        if (!this.connectionTypes.isEmpty()) {
            this.appendWhereOrAnd();
            this.append(" exists (select * from ");
            this.append(TableSpecs.DDC_CONNECTIONTASK.name());
            this.append(" contask where contask.id = ");
            this.append(communicationTaskAliasName());
            this.append(".connectiontask ");
            this.appendWhereOrAnd();
            this.appendInClause("contask.connectiontypepluggableClass", this.connectionTypes);
            this.append(")");
        }
    }

    private boolean isNull(Interval interval) {
        return interval == null
                || ((interval.getStart() == null)
                && (interval.getEnd() == null));
    }

}