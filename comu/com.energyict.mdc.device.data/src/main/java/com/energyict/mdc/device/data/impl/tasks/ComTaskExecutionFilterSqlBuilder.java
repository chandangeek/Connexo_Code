/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.ClauseAwareSqlBuilder;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;

import java.time.Clock;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Builds the SQL query that finds all {@link com.energyict.mdc.device.data.tasks.ComTaskExecution}s
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

    public ComTaskExecutionFilterSqlBuilder(ComTaskExecutionFilterSpecification filterSpecification, Clock clock, QueryExecutor<Device> queryExecutor) {
        super(clock, filterSpecification, queryExecutor);
        this.validate(filterSpecification);
        this.copyTaskStatuses(filterSpecification);
        this.completionCodes = EnumSet.noneOf(CompletionCode.class);
        this.completionCodes.addAll(filterSpecification.latestResults);
        this.lastSessionStart = filterSpecification.lastSessionStart;
        this.lastSessionEnd = filterSpecification.lastSessionEnd;
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
        if (   !filterSpecification.latestResults.isEmpty()
            && !this.isNull(filterSpecification.lastSessionEnd)) {
            throw new IllegalArgumentException("Latest result and last session end in interval cannot be combined");
        }
        if (   !filterSpecification.comTasks.isEmpty()
            && !filterSpecification.comSchedules.isEmpty()) {
            throw new IllegalArgumentException("Communiation tasks and communication schedules cannot be combined");
        }
    }

    public ClauseAwareSqlBuilder build(SqlBuilder sqlBuilder) {
        return this.build(sqlBuilder, communicationTaskAliasName());
    }

    public ClauseAwareSqlBuilder build(SqlBuilder sqlBuilder, String communicationTaskAliasName) {
        ClauseAwareSqlBuilder actualBuilder = this.newActualBuilderForRestrictedStages();
        WithClauses.BUSY_CONNECTION_TASK.appendTo(actualBuilder, BUSY_ALIAS_NAME);
        actualBuilder.append(sqlBuilder);
        this.appendDeviceStateJoinClauses(communicationTaskAliasName);
        String sqlStartClause = sqlBuilder.getText();
        Iterator<ServerComTaskStatus> statusIterator = this.taskStatuses.iterator();
        while (statusIterator.hasNext()) {
            this.appendWhereClause(statusIterator.next());
            if (statusIterator.hasNext()) {
                this.unionAll();
                this.append(sqlStartClause);
            }
        }
        if (this.taskStatuses.isEmpty()) {
            this.appendNonStatusWhereClauses();
        }
        this.appendWhereOrAnd();
        this.append("obsolete_date is null");
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
    protected void appendStatusWhereClauses(ServerComTaskStatus taskStatus) {
        taskStatus.completeFindBySqlBuilder(this.getActualBuilder(), this.getClock());
    }

    @Override
    protected void appendNonStatusWhereClauses() {
        super.appendNonStatusWhereClauses();
        this.appendCompletionCodeClause();
        this.appendLastSessionIntervalWhereClause();
        this.appendDeviceInGroupSql();
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
        if (!this.isNull(this.lastSessionStart)
                || !this.isNull(this.lastSessionEnd)) {
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
                this.appendIntervalWhereClause("cs", "startdate", this.lastSessionStart, IntervalBindStrategy.MILLIS);
            }
            if (!this.isNull(this.lastSessionEnd)) {
                this.append("and (ctes.successindicator =");
                this.addInt(ComTaskExecutionSession.SuccessIndicator.Success.ordinal());
                this.appendWhereOrAnd();
                this.appendIntervalWhereClause("cs", "stopdate", this.lastSessionEnd, IntervalBindStrategy.MILLIS);
                this.append(" )");
            }
            this.append(")");
        }
    }

    private boolean isNull(Interval interval) {
        return interval == null
            || (   (interval.getStart() == null)
                && (interval.getEnd() == null));
    }

}