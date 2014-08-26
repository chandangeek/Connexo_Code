package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import com.elster.jupiter.util.time.Clock;

import java.util.HashSet;
import java.util.Set;

/**
 * Provides code reuse opportunities to builds SQL queries that will
 * match {@link ConnectionTask}s against a {@link ConnectionTaskFilterSpecification}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (17:22)
 */
public abstract class AbstractConnectionTaskFilterSqlBuilder extends AbstractTaskFilterSqlBuilder {

    private static final String SUCCESS_INDICATOR_ALIAS_NAME = "successindicator";

    private Set<ConnectionTypePluggableClass> connectionTypes;
    private Set<ComPortPool> comPortPools;
    private Set<DeviceType> deviceTypes;
    private boolean appendLastComSessionJoinClause;

    public AbstractConnectionTaskFilterSqlBuilder(ConnectionTaskFilterSpecification filterSpecification, Clock clock) {
        super(clock);
        this.connectionTypes = new HashSet<>(filterSpecification.connectionTypes);
        this.comPortPools = new HashSet<>(filterSpecification.comPortPools);
        this.deviceTypes = new HashSet<>(filterSpecification.deviceTypes);
        this.appendLastComSessionJoinClause = filterSpecification.useLastComSession;
    }

    protected void appendWhereClause(ServerConnectionTaskStatus taskStatus) {
        taskStatus.completeFindBySqlBuilder(this.getActualBuilder(), this.getClock());
        this.appendConnectionTypeSql();
        this.appendComPortPoolSql();
        this.appendDeviceTypeSql();
    }

    protected void appendJoinedTables() {
        if (this.requiresLastComSessionClause()) {
            this.appendLastComSessionJoinClause(this.connectionTaskTableName());
        }
    }

    private void appendConnectionTypeSql() {
        if (!this.connectionTypes.isEmpty()) {
            this.appendWhereOrAnd();
            this.appendInClause("connectiontypepluggableClass", this.connectionTypes);
        }
    }

    private void appendComPortPoolSql() {
        if (!this.comPortPools.isEmpty()) {
            this.appendWhereOrAnd();
            this.append(" (");
            this.appendInClause(this.connectionTaskTableName() + ".comportpool", this.comPortPools);
            this.append(")");
        }
    }

    private void appendDeviceTypeSql() {
        this.appendDeviceTypeSql(this.connectionTaskTableName(), this.deviceTypes);
    }

    private boolean requiresLastComSessionClause() {
        return this.appendLastComSessionJoinClause;
    }

    protected void requiresLastComSessionClause(boolean flag) {
        this.appendLastComSessionJoinClause = flag;
    }

    private void appendLastComSessionJoinClause(String connectionTaskTableName) {
        this.appendLastComSessionJoinClauseForConnectionTask(
                SUCCESS_INDICATOR_ALIAS_NAME,
                connectionTaskTableName);
    }

    private void appendLastComSessionJoinClauseForConnectionTask(String successIndicatorAliasName, String connectionTaskTableName) {
        this.append(", (select connectiontask, MAX(successindicator) KEEP (DENSE_RANK LAST ORDER BY ");
        this.append(TableSpecs.DDC_COMSESSION.name());
        this.append(".startdate) ");
        this.append(successIndicatorAliasName);
        this.append(" from ");
        this.append(TableSpecs.DDC_COMSESSION.name());
        this.append(" group by connectiontask) cs");
        this.appendWhereOrAnd();
        this.append(connectionTaskTableName);
        this.append(".id = cs.connectiontask");
    }

}