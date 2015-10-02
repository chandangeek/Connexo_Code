package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.ClauseAwareSqlBuilder;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.device.data.tasks.TaskStatus;

import com.elster.jupiter.orm.QueryExecutor;

import java.time.Clock;

/**
 * Builds the SQL query thats counts {@link com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution}s
 * for a single {@link TaskStatus}, grouping them by the {@link com.energyict.mdc.scheduling.model.ComSchedule}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-03 (13:41)
 */
public class ComTaskExecutionDeviceTypeCounterSqlBuilder extends AbstractComTaskExecutionFilterSqlBuilder {

    private ServerComTaskStatus taskStatus;

    public ComTaskExecutionDeviceTypeCounterSqlBuilder(ServerComTaskStatus taskStatus, Clock clock, ComTaskExecutionFilterSpecification filterSpecification, QueryExecutor<Device> queryExecutor) {
        super(clock, filterSpecification, queryExecutor);
        this.taskStatus = taskStatus;
    }

    public void appendTo(ClauseAwareSqlBuilder sqlBuilder) {
        this.setActualBuilder(sqlBuilder);
        this.appendSelectClause();
        this.appendFromClause();
        this.appendWhereClause();
        this.appendGroupByClause();
    }

    private void appendSelectClause() {
        this.append("select '");
        this.append(this.taskStatus.getPublicStatus().name());
        this.append("', dev.devicetype, count(*)");
    }

    private void appendFromClause() {
        this.append(" from ");
        this.append(TableSpecs.DDC_COMTASKEXEC.name());
        this.append(" ");
        this.append(communicationTaskAliasName());
        this.append(" join ");
        this.append(TableSpecs.DDC_DEVICE.name());
        this.append(" dev on cte.device = dev.id");
    }

    private void appendWhereClause() {
        this.appendWhereClause(this.taskStatus);
    }

    private void appendGroupByClause() {
        this.append(" group by dev.devicetype");
    }

}