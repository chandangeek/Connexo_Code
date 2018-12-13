/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks.report;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Provides code reuse opportunities for components that generate and execute SQL
 * to count instances of XXX, broken down by various properties of the XXX class.
 * It is assumed that the complete SQL comes in 2 or 3 parts.
 * Three parts are only necessary if the devices that are linked to the XXX class
 * are required to be part of a group. Otherwise, 2 parts are sufficient.
 * The first part includes a tests against the current state of the related device.
 * This part will bind the current system time in millis.
 * The second part is optional an contains the conditions of the device group.
 * The last part produces the {@link com.energyict.mdc.device.data.tasks.TaskStatus}
 * SQL clauses and will bind the current system time in seconds.
 * The current system time is extracted from the clock that is
 * provided by the DataModel at construction time.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-10-05 (16:45)
 */
abstract class AbstractBreakdownSqlExecutor {

    static final int TYPE_COLUMN_NUMBER = 1;
    static final int STATUS_COLUMN_NUMBER = TYPE_COLUMN_NUMBER + 1;
    static final int TARGET_ID_COLUMN_NUMBER = STATUS_COLUMN_NUMBER + 1;
    static final int COUNT_COLUMN_NUMBER = TARGET_ID_COLUMN_NUMBER + 1;

    private final DataModel dataModel;
    private final Optional<EndDeviceGroup> deviceGroup;
    private final Optional<AmrSystem> amrSystem;
    private SqlFragment deviceGroupFragment = null;

    AbstractBreakdownSqlExecutor(DataModel dataModel, Optional<EndDeviceGroup> deviceGroup, Optional<AmrSystem> amrSystem) {
        super();
        this.dataModel = dataModel;
        this.deviceGroup = deviceGroup;
        this.amrSystem = amrSystem;
    }

    List<BreakdownResult> breakdowns() {
        try (Connection connection = this.dataModel.getConnection(true);
             PreparedStatement statement = this.statement(connection)) {
            return this.fetchBreakdowns(statement);
        }
        catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    private PreparedStatement statement(Connection connection) throws SQLException {
        return this.sql().prepare(connection);
    }

    private SqlBuilder sql() {
        Instant now = this.dataModel.getInstance(Clock.class).instant();
        SqlBuilder sqlBuilder = this.beforeDeviceGroupSql(now);
        this.deviceGroup.ifPresent(deviceGroup -> this.appendDeviceGroupSql(deviceGroup, sqlBuilder));
        sqlBuilder.add(this.taskStatusSql(now));
        return sqlBuilder;
    }

    /**
     * Returns a SqlBuilder that contains the part of the SQL before
     * the conditions of the the related device group.
     * This will not be called if the related device
     * is not required to be part of a device group.
     * For more details read the class comment.
     *
     * @param now The current time
     * @return The first part of the SQL
     */
    protected abstract SqlBuilder beforeDeviceGroupSql(Instant now);

    /**
     * Returns the part of the SQL that produces the TaskStatus clauses.
     * For more details read the class comment.
     *
     * @param now The current time
     * @return The last part of the SQL
     */
    protected abstract SqlBuilder taskStatusSql(Instant now);

    protected abstract String deviceContainerAliasName();

    private void appendDeviceGroupSql(EndDeviceGroup deviceGroup, SqlBuilder sqlBuilder) {
        SqlFragment fragment;
        sqlBuilder.append(" and ");
        sqlBuilder.append(this.deviceContainerAliasName());
        sqlBuilder.append(".device in (");
        if (deviceGroup instanceof QueryEndDeviceGroup) {
            fragment = ((QueryEndDeviceGroup) deviceGroup).toFragment();
        }
        else {
            fragment = ((EnumeratedEndDeviceGroup) deviceGroup).getAmrIdSubQuery(this.amrSystem.get()).toFragment();
        }
        sqlBuilder.add(fragment);
        sqlBuilder.append(")");
        this.deviceGroupFragment = fragment;
    }

    private List<BreakdownResult> fetchBreakdowns(PreparedStatement statement) throws SQLException {
        List<BreakdownResult> breakdownResults = new ArrayList<>();
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                breakdownResults.add(BreakdownType.resultFor(resultSet));
            }
        }
        return breakdownResults;
    }

}