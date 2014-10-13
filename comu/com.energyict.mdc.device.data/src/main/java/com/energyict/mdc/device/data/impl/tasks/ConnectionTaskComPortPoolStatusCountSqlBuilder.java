package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.util.time.Clock;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.ClauseAwareSqlBuilder;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;

/**
 * Builds the SQL query thats counts {@link ConnectionTask}s
 * for a single {@link TaskStatus} grouped by the
 * {@link com.energyict.mdc.engine.model.ComPortPool}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-06 (13:06)
 */
public class ConnectionTaskComPortPoolStatusCountSqlBuilder extends AbstractConnectionTaskFilterSqlBuilder {

    private ServerConnectionTaskStatus taskStatus;

    public ConnectionTaskComPortPoolStatusCountSqlBuilder(ServerConnectionTaskStatus taskStatus, Clock clock, QueryEndDeviceGroup deviceGroup, QueryExecutor<Device> deviceQueryExecutor) {
        super(clock, deviceGroup, deviceQueryExecutor);
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
        this.append("', comportpool, count(*)");
    }

    private void appendFromClause() {
        this.append(" from ");
        this.append(TableSpecs.DDC_CONNECTIONTASK.name());
        this.append(" ");
        this.append(connectionTaskAliasName());
    }

    private void appendWhereClause() {
        this.appendWhereClause(this.taskStatus);
        this.appendDeviceSql();
    }

    private void appendGroupByClause() {
        this.append(" group by comportpool");
    }

}