/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.util.sql.SqlBuilder;
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
import java.util.stream.Collectors;

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
    private List<Long> connectionMethods;
    private final Long locationId;

    public ComTaskExecutionFilterSqlBuilder(ComTaskExecutionFilterSpecification filterSpecification, Clock clock, QueryExecutor<Device> queryExecutor) {
        super(clock, filterSpecification, queryExecutor);
        this.validate(filterSpecification);
        this.copyTaskStatuses(filterSpecification);
        this.completionCodes = new HashSet<>();
        this.completionCodes.addAll(filterSpecification.latestResults);
        this.lastSessionStart = filterSpecification.lastSessionStart;
        this.lastSessionEnd = filterSpecification.lastSessionEnd;
        this.connectionMethods = filterSpecification.connectionMethods;
        this.connectionTypes = new HashSet<>(filterSpecification.connectionTypes);
        this.locationId = filterSpecification.locationId;
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
        WithClauses.BUSY_CONNECTION_TASK.appendTo(actualBuilder, BUSY_ALIAS_NAME);

        String allctedataAlias = "allctedata";
        this.append(", " + allctedataAlias + " as (");
        String sqlStartClause = sqlBuilder.getText();
        this.append(sqlStartClause.replace("from " + TableSpecs.DDC_COMTASKEXEC.name() + " " + communicationTaskAliasName , " ,cte.id,cte.comport,cte.obsolete_date,cte.onhold,cte.nextexecutiontimestamp,cte.ignorenextexecspecs,cte.lastexecutiontimestamp,cte.connectiontask,cte.currentretrycount,cte.lastexecutionfailed,cte.lastsuccessfulcompletion, hp.COMTASKEXECUTION, dev.DEVICETYPE, dev.NAME, CASE WHEN bt.connectiontask IS NULL THEN 0 ELSE 1 END as busytask_exists from " + TableSpecs.DDC_COMTASKEXEC.name() + " " + communicationTaskAliasName));
        this.appendDeviceAndHighPrioAndBusyTaskJoinClauses();
        this.append(" where exists ( ");
        DeviceStageSqlBuilder.forExcludeStages(getRestrictedDeviceStages()).appendRestrictedStagesSelectClause(this.getActualBuilder().asBuilder(),this.getClock().instant());
        this.append(" ) ");
        this.append(" ) ");

        this.append(sqlStartClause.replace( TableSpecs.DDC_COMTASKEXEC.name() + " " + communicationTaskAliasName,   allctedataAlias + " " + communicationTaskAliasName + " "));
        this.appendLocationIdCondition(locationId);
        Iterator<ServerComTaskStatus> statusIterator = this.taskStatuses.iterator();
        if (statusIterator.hasNext()) {
            this.getActualBuilder().appendWhereOrAnd();
            this.append(" ( ");
            while (statusIterator.hasNext()) {
                this.appendWhereClause(statusIterator.next());
                if (statusIterator.hasNext()) {
                    this.appendWhereOrOr();
                }
            }
            this.append(" ) ");
        }

        if (this.taskStatuses.isEmpty()) {
            this.appendNonStatusWhereClauses();
        }
        if (!this.connectionMethods.isEmpty()) {

            this.appendWhereOrAnd();
            this.append("cte.connectiontask IN (" +
                    " select id from DDC_CONNECTIONTASK where PARTIALCONNECTIONTASK in (" +
                    connectionMethods.stream().map(Object::toString).collect(Collectors.joining(","))
                    + ") )");
        }
        return this.getActualBuilder();
    }

    public SqlBuilder build(DataMapper<ComTaskExecution> dataMapper, int pageStart, int pageSize) {
        ClauseAwareSqlBuilder sqlBuilder = build(dataMapper.builder(communicationTaskAliasName()));
        sqlBuilder.append(" order by lastexecutiontimestamp desc");
        return sqlBuilder.asPageBuilder(pageStart, pageStart + pageSize);
    }

    public SqlBuilder buildCount(DataMapper<ComTaskExecution> dataMapper) {
        ClauseAwareSqlBuilder sqlBuilder = build(dataMapper.builder(communicationTaskAliasName()));
        return sqlBuilder.asBuilder();
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
        this.appendConnectionTypeSql();
    }

    private void appendCompletionCodeClause() {
        if (this.requiresCompletionCodeClause()) {
            this.appendWhereOrAnd();
            this.append("(");
            boolean notFirst = false;
            boolean nullRequested = false;
            for (CompletionCode completionCode : this.completionCodes) {
                if (completionCode != null  && completionCode.dbValue() != 12) {
                    if (notFirst) {
                        this.append(",");
                    } else {
                        this.append("cte.lastsess_highestpriocomplcode in (");
                    }
                    this.addInt(completionCode.dbValue());
                    notFirst = true;
                } else {
                    nullRequested = true;
                }
            }
            if (notFirst) {
                this.append(")");
                if (nullRequested) {
                    this.append(" OR ");
                }
            }
            if (nullRequested) {
                this.append("cte.lastsess_highestpriocomplcode IS NULL");
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