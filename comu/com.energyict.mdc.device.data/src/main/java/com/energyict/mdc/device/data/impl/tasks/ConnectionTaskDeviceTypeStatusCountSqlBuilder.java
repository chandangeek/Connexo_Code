package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.device.data.impl.ClauseAwareSqlBuilder;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;

/**
 * Builds the SQL query thats counts {@link ConnectionTask}s
 * for a single {@link TaskStatus} grouped by the
 * {@link com.energyict.mdc.device.config.DeviceType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-06 (13:06)
 */
public class ConnectionTaskDeviceTypeStatusCountSqlBuilder extends AbstractConnectionTaskFilterSqlBuilder {

    private ServerConnectionTaskStatus taskStatus;

    public ConnectionTaskDeviceTypeStatusCountSqlBuilder(ServerConnectionTaskStatus taskStatus, Clock clock) {
        super(clock);
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
        this.append(TableSpecs.DDC_CONNECTIONTASK.name());
        this.append(" ");
        this.append(connectionTaskAliasName());
        this.append(" join ");
        this.append(TableSpecs.DDC_DEVICE.name());
        this.append(" dev on ");
        this.append(connectionTaskAliasName());
        this.append(".device = dev.id");
    }

    private void appendWhereClause() {
        this.appendWhereClause(this.taskStatus);
    }

    private void appendGroupByClause() {
        this.append(" group by dev.devicetype");
    }

}