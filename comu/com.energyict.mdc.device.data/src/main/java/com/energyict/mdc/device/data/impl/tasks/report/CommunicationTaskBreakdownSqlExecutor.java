/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks.report;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.impl.tasks.DeviceStateSqlBuilder;
import com.energyict.mdc.device.data.impl.tasks.ServerComTaskStatus;
import com.energyict.mdc.device.data.tasks.CommunicationTaskBreakdowns;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

import java.time.Instant;
import java.util.EnumSet;
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

    private static final String DEVICE_STATE_ALIAS_NAME = "enddevices";

    static CommunicationTaskBreakdownSqlExecutor systemWide(DataModel dataModel) {
        return new CommunicationTaskBreakdownSqlExecutor(dataModel, Optional.empty(), Optional.empty());
    }

    static CommunicationTaskBreakdownSqlExecutor forGroup(EndDeviceGroup deviceGroup, AmrSystem mdcAmrSystem, DataModel dataModel) {
        return new CommunicationTaskBreakdownSqlExecutor(dataModel, Optional.of(deviceGroup), Optional.of(mdcAmrSystem));
    }

    private CommunicationTaskBreakdownSqlExecutor(DataModel dataModel, Optional<EndDeviceGroup> deviceGroup, Optional<AmrSystem> amrSystem) {
        super(dataModel, deviceGroup, amrSystem);
    }

    @Override
    protected SqlBuilder beforeDeviceGroupSql(Instant now) {
        SqlBuilder sqlBuilder = new SqlBuilder("with ");
        DeviceStateSqlBuilder
                .forExcludeStates(DEVICE_STATE_ALIAS_NAME, EnumSet.of(DefaultState.IN_STOCK, DefaultState.DECOMMISSIONED))
                .appendRestrictedStatesWithClause(sqlBuilder, now);
        sqlBuilder.append(", alldata AS (");
        sqlBuilder.append("  SELECT cte.id,");
        sqlBuilder.append("         cte.nextexecutiontimestamp,");
        sqlBuilder.append("         cte.lastexecutiontimestamp,");
        sqlBuilder.append("         cte.plannednextexecutiontimestamp,");
        sqlBuilder.append("         cte.discriminator,");
        sqlBuilder.append("         cte.nextexecutionspecs,");
        sqlBuilder.append("         cte.comport,");
        sqlBuilder.append("         cte.onhold,");
        sqlBuilder.append("         cte.currentretrycount,");
        sqlBuilder.append("         cte.lastsuccessfulcompletion,");
        sqlBuilder.append("         cte.lastexecutionfailed,");
        sqlBuilder.append("         cte.comtask,");
        sqlBuilder.append("         cte.comschedule,");
        sqlBuilder.append("         dev.devicetype,");
        sqlBuilder.append("         CASE WHEN ct.id IS NULL");
        sqlBuilder.append("              THEN 0");
        sqlBuilder.append("              ELSE 1");
        sqlBuilder.append("         END as thereisabusytask");
        sqlBuilder.append("    FROM " + TableSpecs.DDC_COMTASKEXEC.name() + " cte");
        sqlBuilder.append("    JOIN " + TableSpecs.DDC_DEVICE.name() + " dev ON cte.device = dev.id");
        sqlBuilder.append("    JOIN enddevices kd ON dev.meterid = kd.id");
        sqlBuilder.append("    LEFT OUTER JOIN " + TableSpecs.DDC_CONNECTIONTASK.name() + " ct ON cte.connectiontask = ct.id");
        sqlBuilder.append("                                                                   AND ct.comserver is not null");
        sqlBuilder.append("                                                                   AND ct.lastCommunicationStart > cte.nextExecutionTimestamp");
        sqlBuilder.append("   WHERE cte.obsolete_date is null");
        return sqlBuilder;
    }

    @Override
    protected SqlBuilder taskStatusSql(Instant now) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.append("),");
        sqlBuilder.append("qry_grouped AS (");
        sqlBuilder.append("  SELECT status,");
        sqlBuilder.append("         comtask,");
        sqlBuilder.append("         comschedule,");
        sqlBuilder.append("         devicetype,");
        sqlBuilder.append("         sum(q1_count)      as q1_count,");
        sqlBuilder.append("         sum(q2_count)      as q2_count,");
        sqlBuilder.append("         count(comschedule) as csched_count,");
        sqlBuilder.append("         count(comtask)     as ctask_count,");
        sqlBuilder.append("         count(devicetype)  as devtype_count");
        sqlBuilder.append("    FROM ( SELECT comtask,");
        sqlBuilder.append("                  comschedule,");
        sqlBuilder.append("                  devicetype,");
        sqlBuilder.append("                  1 as q1_count,");
        sqlBuilder.append("                  CASE WHEN comschedule is null");
        sqlBuilder.append("                       THEN 0");
        sqlBuilder.append("                       ELSE 1");
        sqlBuilder.append("                   END q2_count,");
        sqlBuilder.append("                  CASE WHEN onhold = 0");
        sqlBuilder.append("                        AND (   comport is not null");
        sqlBuilder.append("                             OR (thereisabusytask = 1 AND nextexecutiontimestamp <=");
        sqlBuilder.addLong(now.getEpochSecond());
        sqlBuilder.append("))");
        sqlBuilder.append("                       THEN '" + ServerComTaskStatus.Busy.name() + "'");
        sqlBuilder.append("                       WHEN onhold = 0");
        sqlBuilder.append("                        AND comport is null");
        sqlBuilder.append("                        AND thereisabusytask = 0");
        sqlBuilder.append("                        AND nextexecutiontimestamp <=");
        sqlBuilder.addLong(now.getEpochSecond());
        sqlBuilder.append("                       THEN '" + ServerComTaskStatus.Pending.name() + "'");
        sqlBuilder.append("                       WHEN onhold = 0");
        sqlBuilder.append("                        AND comport is null");
        sqlBuilder.append("                        AND currentretrycount = 0");
        sqlBuilder.append("                        AND lastsuccessfulcompletion is null");
        sqlBuilder.append("                        AND lastExecutionTimestamp is not null");
        sqlBuilder.append("                        AND (   nextExecutionTimestamp is null");
        sqlBuilder.append("                             OR nextExecutionTimestamp >");
        sqlBuilder.addLong(now.getEpochSecond());
        sqlBuilder.append(")");
        sqlBuilder.append("                       THEN '" + ServerComTaskStatus.NeverCompleted.name() + "'");
        sqlBuilder.append("                       WHEN onhold = 0");
        sqlBuilder.append("                        AND nextexecutiontimestamp >");
        sqlBuilder.addLong(now.getEpochSecond());
        sqlBuilder.append("                        AND comport is null");
        sqlBuilder.append("                        AND currentretrycount > 0");
        sqlBuilder.append("                       THEN '" + ServerComTaskStatus.Retrying.name() + "'");
        sqlBuilder.append("                       WHEN onhold = 0");
        sqlBuilder.append("                        AND nextexecutiontimestamp >");
        sqlBuilder.addLong(now.getEpochSecond());
        sqlBuilder.append("                        AND lastsuccessfulcompletion is not null");
        sqlBuilder.append("                        AND lastExecutionTimestamp > lastSuccessfulCompletion");
        sqlBuilder.append("                        AND lastExecutionfailed = 1");
        sqlBuilder.append("                        AND currentretrycount = 0");
        sqlBuilder.append("                       THEN '" + ServerComTaskStatus.Failed.name() + "'");
        sqlBuilder.append("                       WHEN onhold = 0");
        sqlBuilder.append("                        AND comport is null");
        sqlBuilder.append("                        AND lastexecutionfailed = 0");
        sqlBuilder.append("                        AND currentretrycount = 0");
        sqlBuilder.append("                        AND (   lastExecutionTimestamp is null");
        sqlBuilder.append("                             OR lastSuccessfulCompletion is not null)");
        sqlBuilder.append("                        AND ((");
        sqlBuilder.append("                                        plannednextexecutiontimestamp IS NULL");
        sqlBuilder.append("                                AND nextexecutiontimestamp IS NULL");
        sqlBuilder.append("                        )");
        sqlBuilder.append("                        OR ((discriminator = 2 OR (discriminator = 1 AND nextexecutionspecs IS NULL))");
        sqlBuilder.append("                        AND (nextexecutiontimestamp IS NULL OR nextexecutiontimestamp >");
        sqlBuilder.addLong(now.getEpochSecond());
        sqlBuilder.append(")");
        sqlBuilder.append("                        )");
        sqlBuilder.append("                        OR (nextexecutionspecs IS NOT NULL");
        sqlBuilder.append("                                AND (nextexecutiontimestamp IS NULL OR nextexecutiontimestamp >");
        sqlBuilder.addLong(now.getEpochSecond());
        sqlBuilder.append(")");
        sqlBuilder.append("                        )");
        sqlBuilder.append("                        )");
        sqlBuilder.append("                       THEN '" + ServerComTaskStatus.Waiting.name() + "'");
        sqlBuilder.append("                       WHEN onhold <> 0");
        sqlBuilder.append("                       THEN '" + ServerComTaskStatus.OnHold.name() + "'");
        sqlBuilder.append("                       ELSE");
//        sqlBuilder.append("                         to_char(id)");
        sqlBuilder.append("                         '" + ServerComTaskStatus.ProcessingError.name() + "'");

        sqlBuilder.append("                   END AS status");
        sqlBuilder.append("             FROM alldata)");
        sqlBuilder.append("   GROUP BY status, comtask, comschedule, devicetype)");
        sqlBuilder.append("SELECT '" + BreakdownType.None.name() + "', status, null as item, sum(q1_count) as count");
        sqlBuilder.append("  FROM qry_grouped");
        sqlBuilder.append(" GROUP BY status ");
        sqlBuilder.append("UNION ALL ");
        sqlBuilder.append("SELECT '" + BreakdownType.ComSchedule.name() + "', status, comschedule, sum(q2_count) as count");
        sqlBuilder.append("  FROM qry_grouped");
        sqlBuilder.append(" WHERE comschedule is not null");
        sqlBuilder.append("   AND status <> '" + ServerComTaskStatus.OnHold.name() + "'");
        sqlBuilder.append(" GROUP BY status, comschedule ");
        sqlBuilder.append("UNION ALL ");
        sqlBuilder.append("SELECT '" + BreakdownType.ComTask.name() + "', status, comtask, sum(ctask_count) as count");
        sqlBuilder.append("  FROM qry_grouped");
        sqlBuilder.append(" WHERE comschedule is  null");
        sqlBuilder.append("   AND status <> '" + ServerComTaskStatus.OnHold.name() + "'");
        sqlBuilder.append(" GROUP BY status, comtask ");
        sqlBuilder.append("UNION ALL ");
        sqlBuilder.append("SELECT '" + BreakdownType.ComTask.name() + "', GR.status, ctincs.comtask, sum(csched_count) as count");
        sqlBuilder.append("  FROM qry_grouped GR");
        sqlBuilder.append("  LEFT OUTER JOIN sch_comschedule csch ON GR.comschedule = csch.id");
        sqlBuilder.append("  LEFT OUTER JOIN sch_comtaskincomschedule ctincs ON csch.id = ctincs.comschedule");
        sqlBuilder.append(" WHERE GR.comschedule is not null");
        sqlBuilder.append("   AND GR.status <> '" + ServerComTaskStatus.OnHold.name() + "'");
        sqlBuilder.append(" GROUP BY GR.status, ctincs.comtask ");
        sqlBuilder.append("UNION ALL ");
        sqlBuilder.append("SELECT '" + BreakdownType.DeviceType.name() + "', status, devicetype, sum(devtype_count) as count");
        sqlBuilder.append("  FROM qry_grouped");
        sqlBuilder.append(" WHERE status <> '" + ServerComTaskStatus.OnHold.name() + "'");
        sqlBuilder.append(" GROUP BY status, devicetype ");
        sqlBuilder.append("ORDER BY 1, 2, 3");
        return sqlBuilder;
    }

    @Override
    protected String deviceContainerAliasName() {
        return "cte";
    }

}