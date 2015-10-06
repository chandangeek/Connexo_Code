package com.energyict.mdc.device.data.impl.tasks.report;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.tasks.DeviceStateSqlBuilder;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
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
 * It is assumed that the complete SQL comes in 3 parts.
 * The second part tests against the current state of the related device.
 * This part will bind the current system time in millis. It also requires
 * that the SQL context contains an element that has a foreign key
 * to a device. The name of the column that holds this foreign key should be "device".
 * The first part contains sufficient SQL constructs to provide the aforementioded
 * foreign key to the device table {@link com.energyict.mdc.device.data.impl.TableSpecs#DDC_DEVICE}.
 * This first part does not require any binding.
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
    private Optional<SqlFragment> deviceGroupFragment = Optional.empty();
    private int numberOfUtcMilliBinds;

    AbstractBreakdownSqlExecutor(DataModel dataModel, Optional<EndDeviceGroup> deviceGroup, Optional<AmrSystem> amrSystem) {
        super();
        this.dataModel = dataModel;
        this.deviceGroup = deviceGroup;
        this.amrSystem = amrSystem;
    }

    List<BreakdownResult> breakdowns() {
        try (PreparedStatement statement = this.statement()) {
            return this.fetchBreakdowns(statement);
        }
        catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    private PreparedStatement statement() throws SQLException {
        return this.statement(this.dataModel.getConnection(true));
    }

    private PreparedStatement statement(Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(this.sql());
        boolean failed = true;
        try {
            this.bind(statement);
            failed = false;
        }
        finally {
            if (failed) {
                statement.close();
            }
        }
        return statement;
    }

    private String sql() {
        StringBuilder sqlBuilder = new StringBuilder(this.beforeDeviceStateSql());
        this.numberOfUtcMilliBinds =
                DeviceStateSqlBuilder
                        .forDefaultExcludedStates(this.deviceContainerAliasName())
                        .appendRestrictedStatesCondition(sqlBuilder);
        this.deviceGroup.ifPresent(deviceGroup -> this.appendDeviceGroupSql(deviceGroup, sqlBuilder));
        sqlBuilder.append(this.taskStatusSql());
        return sqlBuilder.toString();
    }

    /**
     * Returns the part of the SQL before the test against
     * the current state of the related device.
     * For more details read the class comment.
     *
     * @return The first part of the SQL
     */
    protected abstract String beforeDeviceStateSql();

    /**
     * Binds the {@link #beforeDeviceStateSql()}, starting at the specified
     * start position and returns the next bind position.
     * In other words, increments the start position with the
     * number of binds that were done. Therefore, if no bindings
     * are required, simply return the start position
     *
     * @param statement The PreparedStatement
     * @param now The current system time
     * @param startPosition The position on which the first bind should be done
     * @return The position of the next bind
     * @throws SQLException Thrown by the PreparedStatement when binding values
     */
    protected abstract int bindBeforeDeviceStateSql(PreparedStatement statement, Instant now, int startPosition)  throws SQLException;

    /**
     * Returns the part of the SQL that produces the TaskStatus clauses.
     * For more details read the class comment.
     *
     * @return The last part of the SQL
     */
    protected abstract String taskStatusSql();

    /**
     * Binds the {@link #taskStatusSql()}, starting at the specified
     * start position and returns the next bind position.
     * In other words, increments the start position with the
     * number of binds that were done. Therefore, if no bindings
     * are required, simply return the start position
     *
     * @param statement The PreparedStatement
     * @param now The current system time
     * @param startPosition The position on which the first bind should be done
     * @return The position of the next bind
     * @throws SQLException Thrown by the PreparedStatement when binding values
     */
    protected abstract int bindTaskStatusSql(PreparedStatement statement, Instant now, int startPosition) throws SQLException;

    protected abstract String deviceContainerAliasName();

    private void appendDeviceGroupSql(EndDeviceGroup deviceGroup, StringBuilder sqlBuilder) {
        SqlFragment fragment;
        sqlBuilder.append(" and ");
        sqlBuilder.append(this.deviceContainerAliasName());
        sqlBuilder.append(".device in (");
        if (deviceGroup instanceof QueryEndDeviceGroup) {
            QueryExecutor<Device> queryExecutor = this.deviceFromDeviceGroupQueryExecutor();
            fragment = queryExecutor.asFragment(((QueryEndDeviceGroup) deviceGroup).getCondition(), "id");
        }
        else {
            fragment = ((EnumeratedEndDeviceGroup) deviceGroup).getAmrIdSubQuery(this.amrSystem.get()).toFragment();
        }
        sqlBuilder.append(fragment.getText());
        sqlBuilder.append(")");
        this.deviceGroupFragment = Optional.of(fragment);
    }

    private void bind(PreparedStatement statement) throws SQLException {
        Instant instant = this.dataModel.getInstance(Clock.class).instant();
        int bindPosition = this.bindBeforeDeviceStateSql(statement, instant, 1);
        if (this.deviceGroupFragment.isPresent()) {
            bindPosition = this.deviceGroupFragment.get().bind(statement, bindPosition);
        }
        long nowInMillis = instant.toEpochMilli();
        for (int i = 0; i < this.numberOfUtcMilliBinds; i++) {
            statement.setLong(bindPosition++, nowInMillis);
        }
        this.bindTaskStatusSql(statement, instant, bindPosition);
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

    /**
     * Returns a QueryExecutor that supports building a subquery to match
     * that the ComTaskExecution's device is in a EndDeviceGroup.
     *
     * @return The QueryExecutor
     */
    private QueryExecutor<Device> deviceFromDeviceGroupQueryExecutor() {
        return this.dataModel.query(Device.class, DeviceConfiguration.class, DeviceType.class);
    }

}