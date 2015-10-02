package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.CommunicationTaskBreakdowns;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.sql.SqlFragment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Builds and executes the query that produces the data
 * for the {@link CommunicationTaskBreakdowns}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-25 (16:40)
 */
@LiteralSql
public class CommunicationTaskBreakdownSqlExecutor {

    private static final String BASE_SQL =
            "WITH alldata AS (\n" +
                    "  SELECT cte.id,\n" +
                    "         cte.nextexecutiontimestamp,\n" +
                    "         cte.comport,\n" +
                    "         cte.currentretrycount,\n" +
                    "         cte.lastsuccessfulcompletion,\n" +
                    "         cte.lastexecutionfailed,\n" +
                    "         cte.comtask,\n" +
                    "         cte.comschedule,\n" +
                    "         dev.devicetype,\n" +
                    "         CASE WHEN ct.id IS NULL\n" +
                    "              THEN 0\n" +
                    "              ELSE 1\n" +
                    "         END as thereisabusytask\n" +
                    "    FROM ddc_comtaskexec cte\n" +
                    "    JOIN ddc_device dev ON cte.device = dev.id\n" +
                    "    LEFT OUTER JOIN ddc_connectiontask ct ON cte.connectiontask = ct.id\n" +
                    "                                         AND ct.comserver is not null\n" +
                    "   WHERE cte.obsolete_date is null";
    private static final String SQL_REMAINDER =
            "),\n" +
                    "qry_grouped AS (\n" +
                    "  SELECT status,\n" +
                    "         comtask,\n" +
                    "         comschedule,\n" +
                    "         devicetype,\n" +
                    "         sum(q1_count)      as q1_count,\n" +
                    "         sum(q2_count)      as q2_count,\n" +
                    "         count(comschedule) as csched_count,\n" +
                    "         count(comtask)     as ctask_count,\n" +
                    "         count(devicetype)  as devtype_count\n" +
                    "    FROM ( SELECT comtask,\n" +
                    "                  comschedule,\n" +
                    "                  devicetype,\n" +
                    "                  1 as q1_count,\n" +
                    "                  CASE WHEN comschedule is null\n" +
                    "                       THEN 0\n" +
                    "                       ELSE 1\n" +
                    "                   END q2_count,\n" +
                    "                  CASE WHEN comport is not null\n" +
                    "                         OR (thereisabusytask = 1 AND nextexecutiontimestamp <= ?)\n" +
                    "                       THEN '" + ServerComTaskStatus.Busy.name() + "'\n" +
                    "                       WHEN comport is null\n" +
                    "                        AND nextexecutiontimestamp <= ?\n" +
                    "                        AND thereisabusytask = 0\n" +
                    "                       THEN '" + ServerComTaskStatus.Pending.name() + "'\n" +
                    "                       WHEN comport is null\n" +
                    "                        AND nextexecutiontimestamp > ?\n" +
                    "                        AND lastsuccessfulcompletion is null\n" +
                    "                        AND currentretrycount = 0\n" +
                    "                       THEN '" + ServerComTaskStatus.NeverCompleted.name() + "'\n" +
                    "                       WHEN comport is null\n" +
                    "                        AND nextexecutiontimestamp > ?\n" +
                    "                        AND currentretrycount > 0\n" +
                    "                       THEN '" + ServerComTaskStatus.Retrying.name() + "'\n" +
                    "                       WHEN nextexecutiontimestamp > ?\n" +
                    "                        AND lastsuccessfulcompletion is not null\n" +
                    "                        AND currentretrycount = 0\n" +
                    "                        AND lastExecutionfailed = 1\n" +
                    "                       THEN '" + ServerComTaskStatus.Failed.name() + "'\n" +
                    "                       WHEN comport is null\n" +
                    "                        AND nextexecutiontimestamp > ?\n" +
                    "                        AND lastsuccessfulcompletion is not null\n" +
                    "                        AND lastexecutionfailed = 0\n" +
                    "                       THEN '" + ServerComTaskStatus.Waiting.name() + "'\n" +
                    "                       WHEN comport is null\n" +
                    "                        AND nextexecutiontimestamp is null\n" +
                    "                       THEN 'OnHold'\n" +
                    "                   END AS status\n" +
                    "             FROM alldata)\n" +
                    "   GROUP BY status, comtask, comschedule, devicetype)\n" +
                    "SELECT '" + BreakdownType.None.name() + "', status, null as item, sum(q1_count) as count\n" +
                    "  FROM qry_grouped\n" +
                    " GROUP BY status\n" +
                    "UNION ALL\n" +
                    "SELECT '" + BreakdownType.ComSchedule.name() + "', status, comschedule, sum(q2_count) as count\n" +
                    "  FROM qry_grouped\n" +
                    " WHERE comschedule is not null\n" +
                    "   AND status <> '" + ServerComTaskStatus.OnHold.name() + "'\n" +
                    " GROUP BY status, comschedule\n" +
                    "UNION ALL\n" +
                    "SELECT '" + BreakdownType.ComTask.name() + "', status, comtask, sum(ctask_count) as count\n" +
                    "  FROM qry_grouped\n" +
                    " WHERE comschedule is  null\n" +
                    "   AND status <> '" + ServerComTaskStatus.OnHold.name() + "'\n" +
                    " GROUP BY status, comtask\n" +
                    "UNION ALL\n" +
                    "SELECT '" + BreakdownType.ComTask.name() + "', GR.status, ctincs.comtask, sum(csched_count) as count\n" +
                    "  FROM qry_grouped GR\n" +
                    "  LEFT OUTER JOIN sch_comschedule csch ON GR.comschedule = csch.id\n" +
                    "  LEFT OUTER JOIN sch_comtaskincomschedule ctincs ON csch.id = ctincs.comschedule\n" +
                    " WHERE GR.comschedule is not null\n" +
                    "   AND GR.status <> '" + ServerComTaskStatus.OnHold.name() + "'\n" +
                    " GROUP BY GR.status, ctincs.comtask\n" +
                    "UNION ALL\n" +
                    "SELECT '" + BreakdownType.DeviceType.name() + "', status, devicetype, sum(devtype_count) as count\n" +
                    "  FROM qry_grouped\n" +
                    " WHERE status <> '" + ServerComTaskStatus.OnHold.name() + "'\n" +
                    " GROUP BY status, devicetype\n" +
                    "ORDER BY 1, 2, 3";

    private static final int TYPE_COLUMN_NUMBER = 1;
    private static final int STATUS_COLUMN_NUMBER = TYPE_COLUMN_NUMBER + 1;
    private static final int TARGET_ID_COLUMN_NUMBER = STATUS_COLUMN_NUMBER + 1;
    private static final int COUNT_COLUMN_NUMBER = TARGET_ID_COLUMN_NUMBER + 1;
    private static final int NUMBER_OF_UTC_BINDS = 6;

    private final DataModel dataModel;
    private final Optional<EndDeviceGroup> deviceGroup;
    private final Optional<AmrSystem> amrSystem;
    private Optional<SqlFragment> deviceGroupFragment = Optional.empty();

    static CommunicationTaskBreakdownSqlExecutor systemWide(DataModel dataModel) {
        return new CommunicationTaskBreakdownSqlExecutor(dataModel, Optional.<EndDeviceGroup>empty(), Optional.<AmrSystem>empty());
    }

    static CommunicationTaskBreakdownSqlExecutor forGroup(EndDeviceGroup deviceGroup, AmrSystem mdcAmrSystem, DataModel dataModel) {
        return new CommunicationTaskBreakdownSqlExecutor(dataModel, Optional.of(deviceGroup), Optional.of(mdcAmrSystem));
    }

    private CommunicationTaskBreakdownSqlExecutor(DataModel dataModel, Optional<EndDeviceGroup> deviceGroup, Optional<AmrSystem> amrSystem) {
        super();
        this.dataModel = dataModel;
        this.deviceGroup = deviceGroup;
        this.amrSystem = amrSystem;
    }

    enum BreakdownType {
        None {
            @Override
            BreakdownResult parse(ResultSet row) throws SQLException {
                return BreakdownResult
                        .noBreakdown(
                                ServerComTaskStatus.valueOf(row.getString(STATUS_COLUMN_NUMBER)),
                                row.getLong(COUNT_COLUMN_NUMBER));
            }

            @Override
            public void addTo(BreakdownResult row, CommunicationTaskBreakdownsImpl breakdownResult) {
                breakdownResult.addOverallStatusCount(row);
            }
        },
        ComSchedule {
            @Override
            public void addTo(BreakdownResult row, CommunicationTaskBreakdownsImpl breakdownResult) {
                breakdownResult.addComScheduleStatusCount(row);
            }
        },

        ComTask {
            @Override
            public void addTo(BreakdownResult row, CommunicationTaskBreakdownsImpl breakdownResult) {
                breakdownResult.addComTaskStatusCount(row);
            }
        },

        DeviceType {
            @Override
            public void addTo(BreakdownResult row, CommunicationTaskBreakdownsImpl breakdownResult) {
                breakdownResult.addDeviceTypeStatusCount(row);
            }
        };

        static BreakdownResult resultFor(ResultSet row) throws SQLException {
            BreakdownType breakdownType = BreakdownType.valueOf(row.getString(TYPE_COLUMN_NUMBER));
            return breakdownType.parse(row);
        }

        BreakdownResult parse(ResultSet row) throws SQLException {
            return BreakdownResult.from(
                    this,
                    ServerComTaskStatus.valueOf(row.getString(STATUS_COLUMN_NUMBER)),
                    row.getLong(TARGET_ID_COLUMN_NUMBER),
                    row.getLong(COUNT_COLUMN_NUMBER));
        }

        public abstract void addTo(BreakdownResult row, CommunicationTaskBreakdownsImpl breakdownResult);

    }

    static final class BreakdownResult {
        BreakdownType type;
        ServerComTaskStatus status;
        Optional<Long> breakdownTargetId;
        long count;

        static BreakdownResult noBreakdown(ServerComTaskStatus status, long count) {
            BreakdownResult result = new BreakdownResult();
            result.type = BreakdownType.None;
            result.status = status;
            result.breakdownTargetId = Optional.empty();
            result.count = count;
            return result;
        }

        static BreakdownResult from(BreakdownType type, ServerComTaskStatus status, long targetId, long count) {
            BreakdownResult result = new BreakdownResult();
            result.type = type;
            result.status = status;
            result.breakdownTargetId = Optional.of(targetId);
            result.count = count;
            return result;
        }

        public void addTo(CommunicationTaskBreakdownsImpl breakdownResult) {
            this.type.addTo(this, breakdownResult);
        }
    }

    List<BreakdownResult> communicationTaskBreakdowns() {
        try (PreparedStatement statement = this.statement()) {
            return this.fetchcommunicationTaskBreakdowns(statement);
        }
        catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    private List<BreakdownResult> fetchcommunicationTaskBreakdowns(PreparedStatement statement) throws SQLException {
        List<BreakdownResult> counters = new ArrayList<>();
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                counters.add(BreakdownType.resultFor(resultSet));
            }
        }
        return counters;
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
        StringBuilder sqlBuilder = new StringBuilder(BASE_SQL);
        this.deviceGroup.ifPresent(deviceGroup -> this.appendDeviceGroupSql(deviceGroup, sqlBuilder));
        sqlBuilder.append(SQL_REMAINDER);
        return sqlBuilder.toString();
    }

    private void appendDeviceGroupSql(EndDeviceGroup deviceGroup, StringBuilder sqlBuilder) {
        SqlFragment fragment;
        sqlBuilder.append(" and cte.device in (");
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

    /**
     * Returns a QueryExecutor that supports building a subquery to match
     * that the ComTaskExecution's device is in a EndDeviceGroup.
     *
     * @return The QueryExecutor
     */
    private QueryExecutor<Device> deviceFromDeviceGroupQueryExecutor() {
        return this.dataModel.query(Device.class, DeviceConfiguration.class, DeviceType.class);
    }

    private void bind(PreparedStatement statement) throws SQLException {
        int bindPosition = 1;
        if (this.deviceGroupFragment.isPresent()) {
            bindPosition = this.deviceGroupFragment.get().bind(statement, bindPosition);
        }
        Clock clock = this.dataModel.getInstance(Clock.class);
        long now = clock.instant().getEpochSecond();
        for (int i = 0; i < NUMBER_OF_UTC_BINDS; i++) {
            statement.setLong(bindPosition++, now);
        }
    }

}