package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.impl.ClauseAwareSqlBuilder;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;
import com.energyict.mdc.device.data.tasks.TaskStatus;

import com.elster.jupiter.util.time.Clock;

import java.util.HashSet;
import java.util.Set;

/**
 * Builds the SQL query thats counts {@link ConnectionTask}s
 * that match a {@link ConnectionTaskFilterSpecification} for a single {@link TaskStatus}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-06 (13:06)
 */
public class ComTaskExecutionFilterMatchCounterSqlBuilder extends AbstractComTaskExecutionFilterSqlBuilder {

    private ServerComTaskStatus taskStatus;

    public ComTaskExecutionFilterMatchCounterSqlBuilder(ServerComTaskStatus taskStatus, ComTaskExecutionFilterSpecification filter,  Clock clock) {
        super(clock, filter);
        this.taskStatus = taskStatus;
    }

    public void appendTo(ClauseAwareSqlBuilder sqlBuilder) {
        this.setActualBuilder(sqlBuilder);
        this.appendSelectClause();
        this.appendFromClause();
        this.appendWhereClause();
    }

    private void appendSelectClause() {
        this.append("select '");
        this.append(this.taskStatus.getPublicStatus().name());
        this.append("', count(*)");
    }

    private void appendFromClause() {
        this.append(" from ");
        this.append(TableSpecs.DDC_COMTASKEXEC.name());
        this.append(" ");
        this.append(communicationTaskAliasName());
    }

    private void appendWhereClause() {
        this.appendWhereClause(this.taskStatus);
    }

}