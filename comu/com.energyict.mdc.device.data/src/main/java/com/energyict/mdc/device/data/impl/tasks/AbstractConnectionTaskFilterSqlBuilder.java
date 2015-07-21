package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.QueryExecutor;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides code reuse opportunities to builds SQL queries that will
 * match {@link ConnectionTask}s against a {@link ConnectionTaskFilterSpecification}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (17:22)
 */
public abstract class AbstractConnectionTaskFilterSqlBuilder extends AbstractTaskFilterSqlBuilder {

    private final Set<ConnectionTypePluggableClass> connectionTypes;
    private final Set<ComPortPool> comPortPools;
    private final Set<DeviceType> deviceTypes;
    private final List<EndDeviceGroup> deviceGroups;
    private final QueryExecutor<Device> queryExecutor;
    private boolean appendLastComSessionJoinClause;
    private final Set<String> allowedDeviceStates;

    public AbstractConnectionTaskFilterSqlBuilder(Clock clock, List<EndDeviceGroup> deviceGroups, QueryExecutor<Device> queryExecutor) {
        super(clock);
        this.connectionTypes = new HashSet<>();
        this.comPortPools = new HashSet<>();
        this.deviceTypes = new HashSet<>();
        this.appendLastComSessionJoinClause = false;
        this.deviceGroups = new ArrayList<>(deviceGroups);
        this.queryExecutor = queryExecutor;
        this.allowedDeviceStates = Collections.emptySet();
    }

    public AbstractConnectionTaskFilterSqlBuilder(ConnectionTaskFilterSpecification filterSpecification, Clock clock, QueryExecutor<Device> deviceQueryExecutor) {
        super(clock);
        this.connectionTypes = new HashSet<>(filterSpecification.connectionTypes);
        this.comPortPools = new HashSet<>(filterSpecification.comPortPools);
        this.deviceTypes = new HashSet<>(filterSpecification.deviceTypes);
        this.appendLastComSessionJoinClause = filterSpecification.useLastComSession;
        this.deviceGroups = new ArrayList<>(filterSpecification.deviceGroups);
        this.queryExecutor = deviceQueryExecutor;
        this.allowedDeviceStates = Collections.emptySet();
    }

    protected void appendWhereClause(ServerConnectionTaskStatus taskStatus) {
        taskStatus.completeFindBySqlBuilder(this.getActualBuilder(), this.getClock(), connectionTaskAliasName());
        this.appendNonStatusWhereClauses();
    }

    protected void appendNonStatusWhereClauses() {
        this.appendWhereOrAnd();
        this.append(this.connectionTaskAliasName());
        this.append(".obsolete_date is null");
        this.appendConnectionTypeSql();
        this.appendComPortPoolSql();
        this.appendDeviceTypeSql();
        this.appendDeviceInStateSql();
    }

    protected void appendJoinedTables() {
        if (this.requiresLastComSessionClause()) {
            this.appendLastComSessionJoinClause();
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
            this.appendInClause("ct.comportpool", this.comPortPools);
            this.append(")");
        }
    }

    private void appendDeviceTypeSql() {
        this.appendDeviceTypeSql(this.connectionTaskAliasName(), this.deviceTypes);
    }

    protected void appendDeviceInGroupSql() {
        this.appendDeviceInGroupSql(this.deviceGroups, this.queryExecutor, "ct");
    }

    protected void appendDeviceInStateSql(){
        this.appendDeviceInStateSql(connectionTaskAliasName(), this.allowedDeviceStates);
    }

    protected boolean requiresLastComSessionClause() {
        return this.appendLastComSessionJoinClause;
    }

    protected void requiresLastComSessionClause(boolean flag) {
        this.appendLastComSessionJoinClause = flag;
    }

    protected void appendLastComSessionJoinClause() {
        this.appendLastComSessionJoinClause(this.connectionTaskAliasName());
    }

    private void appendLastComSessionJoinClause(String connectionTaskTableName) {
        this.append(" join ");
        this.append(TableSpecs.DDC_COMSESSION.name());
        this.append(" cs on ");
        this.append(connectionTaskTableName);
        this.append(".lastsession = cs.id");
    }

}