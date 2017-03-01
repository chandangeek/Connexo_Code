/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.QueryExecutor;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.ClauseAwareSqlBuilder;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.impl.tasks.report.AbstractTaskFilterSqlBuilder;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.energyict.mdc.device.data.impl.tasks.DeviceStateSqlBuilder.DEVICE_STATE_ALIAS_NAME;

/**
 * Provides code reuse opportunities to builds SQL queries that will
 * match {@link ConnectionTask}s against a {@link ConnectionTaskFilterSpecification}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (17:22)
 */
abstract class AbstractConnectionTaskFilterSqlBuilder extends AbstractTaskFilterSqlBuilder {

    private final Set<ConnectionTypePluggableClass> connectionTypes;
    private final Set<ComPortPool> comPortPools;
    private final Set<DeviceType> deviceTypes;
    private final Set<EndDeviceStage> restricedDeviceStages;
    private final List<EndDeviceGroup> deviceGroups;
    private final QueryExecutor<Device> queryExecutor;
    private boolean appendLastComSessionJoinClause;

    AbstractConnectionTaskFilterSqlBuilder(ConnectionTaskFilterSpecification filterSpecification, Clock clock, QueryExecutor<Device> deviceQueryExecutor) {
        super(clock);
        this.connectionTypes = new HashSet<>(filterSpecification.connectionTypes);
        this.comPortPools = new HashSet<>(filterSpecification.comPortPools);
        this.deviceTypes = new HashSet<>(filterSpecification.deviceTypes);
        this.restricedDeviceStages = EndDeviceStage.fromNames(filterSpecification.restrictedDeviceStages);
        this.appendLastComSessionJoinClause = filterSpecification.useLastComSession;
        this.deviceGroups = new ArrayList<>(filterSpecification.deviceGroups);
        this.queryExecutor = deviceQueryExecutor;
    }

    ClauseAwareSqlBuilder newActualBuilderForRestrictedStates() {
        ClauseAwareSqlBuilder actualBuilder = ClauseAwareSqlBuilder
                .withExcludedStages(
                        DEVICE_STATE_ALIAS_NAME,
                        this.restricedDeviceStages,
                        this.getClock().instant());
        this.setActualBuilder(actualBuilder);
        return actualBuilder;
    }

    private void appendDeviceStateJoinClauses() {
        this.appendDeviceStateJoinClauses(connectionTaskAliasName());
    }

    private void appendDeviceStateJoinClauses(String deviceContainerAliasName) {
        this.append(" join ");
        this.append(TableSpecs.DDC_DEVICE.name());
        this.append(" dev on ");
        this.append(deviceContainerAliasName);
        this.append(".device = dev.id ");
        this.append(" join ");
        this.append(DeviceStageSqlBuilder.DEVICE_STAGE_ALIAS_NAME);
        this.append(" kd on dev.meterid = kd.id ");
    }

    void appendWhereClause(ServerConnectionTaskStatus taskStatus) {
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
    }

    void appendJoinedTables() {
        this.appendDeviceStateJoinClauses();
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
        this.appendDeviceTypeSql(this.deviceTypes);
    }

    void appendDeviceInGroupSql() {
        this.appendDeviceInGroupSql(this.deviceGroups, "ct");
    }

    protected boolean requiresLastComSessionClause() {
        return this.appendLastComSessionJoinClause;
    }

    void requiresLastComSessionClause(boolean flag) {
        this.appendLastComSessionJoinClause = flag;
    }

    private void appendLastComSessionJoinClause() {
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