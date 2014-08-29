package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.impl.ClauseAwareSqlBuilder;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;

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

    private static final String COM_TASK_EXECUTION_SESSION_ALIAS_NAME = "ctes";
    private static final String HIGHEST_PRIORITY_COMPLETION_CODE_ALIAS_NAME = "highestPrioCompletionCode";

    private Set<ServerComTaskStatus> taskStatuses;
    private Set<CompletionCode> completionCodes;
    public Interval lastSessionStart = null;
    public Interval lastSessionEnd = null;

    public ComTaskExecutionFilterSqlBuilder(ComTaskExecutionFilterSpecification filterSpecification, Clock clock) {
        super(clock, filterSpecification);
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

    public SqlBuilder build(DataMapper<ComTaskExecution> dataMapper, int pageStart, int pageSize) {
        SqlBuilder sqlBuilder = dataMapper.builder(null);   // Does not generate an alias
        this.setActualBuilder(new ClauseAwareSqlBuilder(sqlBuilder));
        this.appendJoinedTables();
        String sqlStartClause = sqlBuilder.getText();
        Iterator<ServerComTaskStatus> statusIterator = this.taskStatuses.iterator();
        while (statusIterator.hasNext()) {
            this.appendWhereClause(statusIterator.next());
            if (statusIterator.hasNext()) {
                this.unionAll();
                this.append(sqlStartClause);
                this.appendJoinedTables();
            }
        }
        if (this.taskStatuses.isEmpty()) {
            this.appendNonStatusWhereClauses();
        }
        this.appendWhereOrAnd();
        this.append("obsolete_date is null");
        this.append(" order by lastexecutiontimestamp desc");
        return sqlBuilder.asPageBuilder(pageStart, pageStart + pageSize - 1);
    }

    @Override
    protected void appendWhereClause(ServerComTaskStatus taskStatus) {
        super.appendWhereClause(taskStatus);
    }

    @Override
    protected void appendNonStatusWhereClauses() {
        super.appendNonStatusWhereClauses();
        this.appendCompletionCodeClause();
        this.appendLastSessionIntervalWhereClause();
    }

    protected void appendJoinedTables() {
        if (this.requiresCompletionCodeClause()) {
            this.appendLastSessionJoinClauseForComTaskExecution();
        }
    }

    private void appendLastSessionJoinClauseForComTaskExecution () {
        this.append(", (select comtaskexec, MAX(highestPrioCompletionCode) KEEP (DENSE_RANK LAST ORDER BY startdate) ");
        this.append(HIGHEST_PRIORITY_COMPLETION_CODE_ALIAS_NAME);
        this.append(" from ");
        this.append(TableSpecs.DDC_COMTASKEXECSESSION.name());
        this.append(" group by comtaskexec) ");
        this.append(COM_TASK_EXECUTION_SESSION_ALIAS_NAME);
        this.appendWhereOrAnd();
        this.append(" id = ");
        this.append(COM_TASK_EXECUTION_SESSION_ALIAS_NAME);
        this.append(".comtaskexec");
    }

    private void appendCompletionCodeClause () {
        if (this.requiresCompletionCodeClause()) {
            this.appendWhereOrAnd();
            this.append(COM_TASK_EXECUTION_SESSION_ALIAS_NAME);
            this.append(".");
            this.append(HIGHEST_PRIORITY_COMPLETION_CODE_ALIAS_NAME);
            this.append(" in (");
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

    private boolean requiresCompletionCodeClause () {
        return !this.completionCodes.isEmpty();
    }

    private void appendLastSessionIntervalWhereClause() {
        if (   !this.isNull(this.lastSessionStart)
            || !this.isNull(this.lastSessionEnd)) {
            this.appendWhereOrAnd();
            this.append(" exists (select * from ");
            this.append(TableSpecs.DDC_COMSESSION.name());
            this.append(" cs, ");
            this.append(TableSpecs.DDC_COMTASKEXECSESSION.name());
            this.append(" ctes where ctes.comtaskexec = ");
            this.append(TableSpecs.DDC_COMTASKEXEC.name());
            this.append(".id and ctes.comsession = cs.id ");
            if (!this.isNull(this.lastSessionStart)) {
                this.appendWhereOrAnd();
                this.appendIntervalWhereClause("cs", "startdate", this.lastSessionStart);
            }
            if (!this.isNull(this.lastSessionEnd)) {
                this.append("and (ctes.successindicator =");
                this.addInt(ComTaskExecutionSession.SuccessIndicator.Success.ordinal());
                this.appendWhereOrAnd();
                this.appendIntervalWhereClause("cs", "stopdate", this.lastSessionEnd);
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