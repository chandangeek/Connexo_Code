package com.energyict.mdc.device.data.impl.tasks.report;

import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.impl.tasks.ServerComTaskStatus;
import com.energyict.mdc.device.data.tasks.CommunicationTaskBreakdowns;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;

/**
 * Builds and executes the query that produces the data
 * for the {@link CommunicationTaskBreakdowns}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-25 (16:40)
 */
@LiteralSql
class CommunicationTaskBreakdownSqlExecutor extends AbstractBreakdownSqlExecutor {

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
                    "    FROM " + TableSpecs.DDC_COMTASKEXEC.name() + " cte\n" +
                    "    JOIN " + TableSpecs.DDC_DEVICE.name() + " dev ON cte.device = dev.id\n" +
                    "    LEFT OUTER JOIN " + TableSpecs.DDC_CONNECTIONTASK.name() + " ct ON cte.connectiontask = ct.id\n" +
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

    private static final int NUMBER_OF_UTC_SECONDS_BINDS = 6;

    static CommunicationTaskBreakdownSqlExecutor systemWide(DataModel dataModel) {
        return new CommunicationTaskBreakdownSqlExecutor(dataModel, Optional.<EndDeviceGroup>empty(), Optional.<AmrSystem>empty());
    }

    static CommunicationTaskBreakdownSqlExecutor forGroup(EndDeviceGroup deviceGroup, AmrSystem mdcAmrSystem, DataModel dataModel) {
        return new CommunicationTaskBreakdownSqlExecutor(dataModel, Optional.of(deviceGroup), Optional.of(mdcAmrSystem));
    }

    private CommunicationTaskBreakdownSqlExecutor(DataModel dataModel, Optional<EndDeviceGroup> deviceGroup, Optional<AmrSystem> amrSystem) {
        super(dataModel, deviceGroup, amrSystem);
    }

    @Override
    protected String beforeDeviceStateSql() {
        return BASE_SQL;
    }

    @Override
    protected int bindBeforeDeviceStateSql(PreparedStatement statement, Instant now, int startPosition) throws SQLException {
        return startPosition;
    }

    @Override
    protected String taskStatusSql() {
        return SQL_REMAINDER;
    }

    @Override
    protected int bindTaskStatusSql(PreparedStatement statement, Instant now, int startPosition) throws SQLException {
        int bindPosition = startPosition;
        for (int i = 0; i < NUMBER_OF_UTC_SECONDS_BINDS; i++) {
            statement.setLong(bindPosition++, now.getEpochSecond());
        }
        return bindPosition;
    }

    @Override
    protected String deviceContainerAliasName() {
        return "cte";
    }

}