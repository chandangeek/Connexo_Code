package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;

import com.elster.jupiter.util.time.Clock;

import java.util.EnumSet;
import java.util.HashSet;
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

    private Set<CompletionCode> completionCodes;
    private Set<ComSchedule> comSchedules;
    private Set<ComTask> comTasks;
    private Set<DeviceType> deviceTypes;

    public ComTaskExecutionFilterSqlBuilder(ComTaskExecutionFilterSpecification filterSpecification, Clock clock) {
        super(clock, filterSpecification);
        this.completionCodes = EnumSet.noneOf(CompletionCode.class);
        this.completionCodes.addAll(filterSpecification.latestResults);
        this.comSchedules = new HashSet<>(filterSpecification.comSchedules);
        this.comTasks = new HashSet<>(filterSpecification.comTasks);
        this.deviceTypes = new HashSet<>(filterSpecification.deviceTypes);
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

    private void appendComTaskClause () {
        if (this.requiresComTaskClause()) {
            this.appendWhereOrAnd();
            this.appendInClause("comtask", this.comTasks);
        }
    }

    private boolean requiresComTaskClause () {
        return !this.comTasks.isEmpty();
    }

    private void appendComScheduleSql() {
        if (!this.comSchedules.isEmpty()) {
            this.appendWhereOrAnd();
            this.append(" (discriminator = ");
            this.addString(ComTaskExecutionImpl.SCHEDULED_COM_TASK_EXECUTION_DISCRIMINATOR);
            this.append(" and ");
            this.appendInClause("comschedule", this.comSchedules);
            this.append(")");
        }
    }

    private void appendDeviceTypeSql() {
        if (!this.deviceTypes.isEmpty()) {
            this.appendWhereOrAnd();
            this.append(" (");
            this.append(this.connectionTaskTableName());
            this.append(".device in (select id from ");
            this.append(TableSpecs.DDC_DEVICE.name());
            this.append(" where ");
            this.appendInClause("devicetype", this.deviceTypes);
            this.append("))");
        }
    }

}