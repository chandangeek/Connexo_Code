package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.orm.QueryStringifier;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.util.List;

public class DashboardBreakdownSqlBuilder {

    private List<QueryEndDeviceGroup> queryEndDeviceGroupList;

    public DashboardBreakdownSqlBuilder(List<QueryEndDeviceGroup> queryEndDeviceGroupList) {
        this.queryEndDeviceGroupList = queryEndDeviceGroupList;
    }

    public SqlBuilder getDashboardComTaskDataBuilder() {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.append("INSERT into dashboard_comtask\n" +
                "       ( querytype, devicetype, mrid, lastsesshighestcompcode, heatmapcount,\n" +
                "         tasktype, status, comschedule, count )\n" +
                "with ");
        sqlBuilder.append(
                " alldata as (\n" +
                        "--\n" +
                        "  SELECT\n" +
                        "         cte.id,\n" +
                        "         cte.nextexecutiontimestamp,\n" +
                        "         cte.lastexecutiontimestamp,\n" +
                        "         cte.plannednextexecutiontimestamp,\n" +
                        "         cte.discriminator,\n" +
                        "         cte.nextexecutionspecs,\n" +
                        "         cte.comport,\n" +
                        "         cte.onhold,\n" +
                        "         cte.currentretrycount,\n" +
                        "         cte.lastsuccessfulcompletion,\n" +
                        "         cte.lastexecutionfailed,\n" +
                        "         cte.comtask,\n" +
                        "         cte.comschedule,\n" +
                        "         cte.device,\n" +
                        "         cte.lastsession,\n" +
                        "         cte.lastsess_highestpriocomplcode,\n" +
                        "         dev.devicetype,\n" +
                        "         CASE WHEN ct.id                IS NULL THEN 0 ELSE 1 END as thereisabusytask,\n" +
                        "         CASE WHEN hp.comtaskexecution = cte.id THEN 1 ELSE 0 END as isapriotask,\n" +
                        "         grdesc.mrid\n" +
                        "   --\n" +
                        "     FROM\tDDC_COMTASKEXEC cte\n" +
                        "   --\n" +
                        " LEFT JOIN MTG_ED_GROUP grdesc\n" +
                        " ON exists(select * from MTG_ENUM_ED_IN_GROUP where group_id = grdesc.id and enddevice_id = dev.meterid) or " +
                        " exists(select * from DYNAMIC_GROUP_DATA where group_id = grdesc.id and device_id = dev.id)" +
                        "  LEFT OUTER JOIN DDC_CONNECTIONTASK ct\n" +
                        "          ON  cte.connectiontask             = ct.id\n" +
                        "              AND ct.comPort                is not null\n" +
                        "              AND ct.lastCommunicationStart  > cte.nextExecutionTimestamp\n" +
                        "   --\n" +
                        "          LEFT JOIN DDC_HIPRIOCOMTASKEXEC hp\n" +
                        "          ON hp.comtaskexecution = cte.id\n" +
                        "   --\n" +
                        "    WHERE cte.obsolete_date is null\n" +
                        "      AND comschedule is not null      -- added by Jozsef\n" +
                        "),\n" +
                        "--\n" +
                        "alldatagrouped as (\n" +
                        "--\n" +
                        " SELECT status,\n" +
                        "        comtask,\n" +
                        "        comschedule,\n" +
                        "        devicetype,\n" +
                        "        mrid,\n" +
                        "        sum ( q1_count )      as q1_count,\n" +
                        "        sum ( q2_count )      as q2_count,\n" +
                        "        count ( comschedule ) as csched_count,\n" +
                        "        count ( comtask )     as ctask_count,\n" +
                        "        count ( devicetype )  as devtype_count\n" +
                        "   FROM\n" +
                        "        (\n" +
                        "          SELECT comtask,\n" +
                        "                 comschedule,\n" +
                        "                 devicetype,\n" +
                        "                 mrid,\n" +
                        "                 1 as q1_count,\n" +
                        "                 CASE WHEN comschedule is null\n" +
                        "                      THEN 0\n" +
                        "                      ELSE 1\n" +
                        "                  END q2_count,\n" +
                        "                 CASE\n" +
                        "                   --\n" +
                        "                      WHEN onhold = 0\n" +
                        "                           AND ( comport is not null OR  ( thereisabusytask = 1\n" +
                        "                                                           AND nextexecutiontimestamp <= 86400 * (SYSDATE - TO_DATE('1970/01/01', 'YYYY/MM/DD')) )\n" +
                        "                               )\n" +
                        "                      THEN 'Busy'\n" +
                        "                   --\n" +
                        "                      WHEN onhold = 0\n" +
                        "                           AND isapriotask = 0\n" +
                        "                           AND comport is null\n" +
                        "                           AND thereisabusytask = 0\n" +
                        "                           AND nextexecutiontimestamp <= 86400 * (SYSDATE - TO_DATE('1970/01/01', 'YYYY/MM/DD'))\n" +
                        "                      THEN 'Pending'\n" +
                        "                   --\n" +
                        "                      WHEN onhold = 0\n" +
                        "                           AND isapriotask = 1\n" +
                        "                           AND comport is null\n" +
                        "                           AND thereisabusytask = 0\n" +
                        "                           AND nextexecutiontimestamp <= 86400 * (SYSDATE - TO_DATE('1970/01/01', 'YYYY/MM/DD'))\n" +
                        "                      THEN 'PendingWithPriority'\n" +
                        "                   --\n" +
                        "                      WHEN onhold = 0\n" +
                        "                           AND comport is null\n" +
                        "                           AND currentretrycount = 0\n" +
                        "                           AND lastsuccessfulcompletion is null\n" +
                        "                           AND lastExecutionTimestamp is not null\n" +
                        "                           AND ( nextExecutionTimestamp is null\n" +
                        "                            OR  nextExecutionTimestamp > 86400 * (SYSDATE - TO_DATE('1970/01/01', 'YYYY/MM/DD')) )\n" +
                        "                      THEN 'NeverCompleted'\n" +
                        "                   --\n" +
                        "                      WHEN onhold = 0\n" +
                        "                           AND isapriotask = 0\n" +
                        "                           AND nextexecutiontimestamp > 86400 * (SYSDATE - TO_DATE('1970/01/01', 'YYYY/MM/DD'))\n" +
                        "                           AND comport is null\n" +
                        "                           AND currentretrycount > 0\n" +
                        "                      THEN 'Retrying'\n" +
                        "                   --\n" +
                        "                      WHEN onhold = 0\n" +
                        "                           AND isapriotask = 1\n" +
                        "                           AND nextexecutiontimestamp > 86400 * (SYSDATE - TO_DATE('1970/01/01', 'YYYY/MM/DD'))\n" +
                        "                           AND comport is null\n" +
                        "                           AND currentretrycount > 0\n" +
                        "                      THEN 'RetryingWithPriority'\n" +
                        "                   --\n" +
                        "                      WHEN onhold = 0\n" +
                        "                           AND nextexecutiontimestamp > 86400 * (SYSDATE - TO_DATE('1970/01/01', 'YYYY/MM/DD'))\n" +
                        "                           AND lastsuccessfulcompletion is not null\n" +
                        "                           AND lastExecutionTimestamp > lastSuccessfulCompletion\n" +
                        "                           AND lastExecutionfailed = 1\n" +
                        "                           AND currentretrycount = 0\n" +
                        "                      THEN 'Failed'\n" +
                        "                   --\n" +
                        "                      WHEN onhold = 0\n" +
                        "                           AND isapriotask = 0\n" +
                        "                           AND comport is null\n" +
                        "                           AND lastexecutionfailed = 0\n" +
                        "                           AND currentretrycount = 0\n" +
                        "                           AND ( lastExecutionTimestamp is null OR  lastSuccessfulCompletion is not null )\n" +
                        "                           AND ( ( plannednextexecutiontimestamp IS NULL AND nextexecutiontimestamp IS NULL )\n" +
                        "                                 OR  ( ( discriminator = 2\n" +
                        "                                          OR  ( discriminator = 1 AND nextexecutionspecs IS NULL )\n" +
                        "                                       )\n" +
                        "                                       AND ( nextexecutiontimestamp IS NULL OR  nextexecutiontimestamp > 86400 * (SYSDATE - TO_DATE('1970/01/01', 'YYYY/MM/DD')) )\n" +
                        "                                     )\n" +
                        "                                 OR  ( nextexecutionspecs IS NOT NULL  AND ( nextexecutiontimestamp IS NULL\n" +
                        "                                                                             OR  nextexecutiontimestamp > 86400 * (SYSDATE - TO_DATE('1970/01/01', 'YYYY/MM/DD')) )\n" +
                        "                                     )\n" +
                        "                               )\n" +
                        "                      THEN 'Waiting'\n" +
                        "                   --\n" +
                        "                      WHEN onhold = 0\n" +
                        "                           AND isapriotask = 1\n" +
                        "                           AND comport is null\n" +
                        "                           AND lastexecutionfailed = 0\n" +
                        "                           AND currentretrycount = 0\n" +
                        "                           AND ( lastExecutionTimestamp is null  OR  lastSuccessfulCompletion is not null )\n" +
                        "                           AND ( ( plannednextexecutiontimestamp IS NULL  AND nextexecutiontimestamp IS NULL )\n" +
                        "                                   OR  ( ( discriminator = 2 OR  ( discriminator = 1 AND nextexecutionspecs IS NULL ) )\n" +
                        "                                         AND ( nextexecutiontimestamp IS NULL  OR  nextexecutiontimestamp > 86400 * (SYSDATE - TO_DATE('1970/01/01', 'YYYY/MM/DD')) )\n" +
                        "                                       )\n" +
                        "                                   OR  ( nextexecutionspecs IS NOT NULL\n" +
                        "                                         AND ( nextexecutiontimestamp IS NULL OR  nextexecutiontimestamp > 86400 * (SYSDATE - TO_DATE('1970/01/01', 'YYYY/MM/DD')) )\n" +
                        "                                       )\n" +
                        "                               )\n" +
                        "                      THEN 'WaitingWithPriority'\n" +
                        "                   --\n" +
                        "                      WHEN onhold <> 0\n" +
                        "                      THEN 'OnHold'\n" +
                        "                   --\n" +
                        "                      ELSE 'ProcessingError'\n" +
                        "                   --\n" +
                        "                  END AS status\n" +
                        "            FROM alldata\n" +
                        "        )\n" +
                        "  GROUP BY status, comtask, comschedule, devicetype, mrid\n" +
                        ")\n" +
                        "--\n" +
                        "SELECT 'COMTASK_Q2' as QTYPE,\n" +
                        "       DEVICETYPE,\n" +
                        "       mrid,\n" +
                        "       lastsess_highestpriocomplcode,\n" +
                        "       count ( * ) as heatmapcount,\n" +
                        "       null,\n" +
                        "       null,\n" +
                        "       null,\n" +
                        "       null\n" +
                        "  FROM alldata\n" +
                        "-- WHERE\n" +
                        " -- rowtype2 = 1 and\n" +
                        "--   lastsession   is not null -- commented as per CXO-11938\n" +
                        " GROUP BY devicetype, mrid, lastsess_highestpriocomplcode\n" +
                        "--\n" +
                        "UNION ALL\n" +
                        "--\n" +
                        "SELECT 'COMTASK_Q1' as QTYPE,\n" +
                        "       DEVICETYPE,\n" +
                        "       mrid,\n" +
                        "       null,\n" +
                        "       null,\n" +
                        "       'None', status, null as item, sum ( q1_count ) as count\n" +
                        "  FROM alldatagrouped\n" +
                        " GROUP BY devicetype, mrid, status\n" +
                        "--\n" +
                        "UNION  ALL\n" +
                        "--\n" +
                        "SELECT 'COMTASK_Q1' as QTYPE,\n" +
                        "       DEVICETYPE,\n" +
                        "       mrid,\n" +
                        "       null,\n" +
                        "       null,\n" +
                        "       'ComSchedule', status, comschedule, sum ( q2_count ) as count\n" +
                        "  FROM alldatagrouped\n" +
                        " WHERE comschedule is not null\n" +
                        "   AND status <> 'OnHold'\n" +
                        " GROUP BY devicetype, mrid, status, comschedule\n" +
                        "--\n" +
                        "UNION  ALL\n" +
                        "--\n" +
                        "SELECT 'COMTASK_Q1' as QTYPE,\n" +
                        "       DEVICETYPE,\n" +
                        "       mrid,\n" +
                        "       null,\n" +
                        "       null,\n" +
                        "       'ComTask', status, comtask, sum ( ctask_count ) as count\n" +
                        "  FROM alldatagrouped\n" +
                        " WHERE comschedule is null\n" +
                        "   AND status <> 'OnHold'\n" +
                        " GROUP BY devicetype, mrid, status, comtask\n" +
                        "--\n" +
                        "UNION  ALL\n" +
                        "--\n" +
                        "SELECT 'COMTASK_Q1' as QTYPE,\n" +
                        "       DEVICETYPE,\n" +
                        "       gr.mrid,\n" +
                        "       null,\n" +
                        "       null,\n" +
                        "       'ComTask', GR.status, ctincs.comtask, sum ( csched_count ) as count\n" +
                        "  FROM alldatagrouped GR\n" +
                        "       LEFT OUTER JOIN sch_comschedule csch ON GR.comschedule = csch.id\n" +
                        "       LEFT OUTER JOIN sch_comtaskincomschedule ctincs ON csch.id = ctincs.comschedule\n" +
                        " WHERE GR.comschedule is not null\n" +
                        "   AND GR.status <> 'OnHold'\n" +
                        " GROUP BY devicetype, GR.status, gr.mrid, ctincs.comtask\n" +
                        "--\n" +
                        "UNION  ALL\n" +
                        "--\n" +
                        "SELECT 'COMTASK_Q1' as QTYPE,\n" +
                        "       DEVICETYPE,\n" +
                        "       mrid,\n" +
                        "       null,\n" +
                        "       null,\n" +
                        "       'DeviceType', status, devicetype, sum ( devtype_count ) as count\n" +
                        "  FROM alldatagrouped\n" +
                        " WHERE status <> 'OnHold'\n" +
                        " GROUP BY devicetype, mrid, status, devicetype");
        return sqlBuilder;
    }

    public SqlBuilder getDashboardConTaskDataBuilder() {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.append("CREATE GLOBAL TEMPORARY TABLE MV_CONNECTIONDATA on commit preserve rows\n" +
                "AS ");
        sqlBuilder.append("SELECT\n" +
                "       ct.lastsession,\n" +
                "       dev.devicetype,\n" +
                "       grdesc.mrid,\n" +
                "       failedTask.comSession failedTask_comSession,\n" +
                "       ct.connectiontypepluggableclass,\n" +
                "       ct.device,\n" +
                "       ct.comportpool,\n" +
                "       ct.lastSessionSuccessIndicator,\n" +
                "       CASE\n" +
                "            WHEN ctsFromCtes.connectiontask IS NOT NULL\n" +
                "                 OR ct.comport   IS NOT NULL\n" +
                "            THEN 'Busy'\n" +
                "            WHEN ( discriminator = '1' AND status > 0)\n" +
                "                 OR ( discriminator = '2' AND ( status > 0 OR nextExecutionTimestamp is null ) )\n" +
                "            THEN 'OnHold'\n" +
                "            WHEN nextexecutiontimestamp <= round((SYSDATE - date '1970-01-01')*24*60*60)\n" +
                "            THEN 'Pending'\n" +
                "            WHEN currentretrycount = 0\n" +
                "                 AND nextexecutiontimestamp > round((SYSDATE - date '1970-01-01')*24*60*60)\n" +
                "                 AND lastsuccessfulcommunicationend is null\n" +
                "            THEN 'NeverCompleted'\n" +
                "            WHEN currentretrycount > 0\n" +
                "                 AND nextexecutiontimestamp > round((SYSDATE - date '1970-01-01')*24*60*60)\n" +
                "            THEN 'Retrying'\n" +
                "            WHEN currentretrycount = 0\n" +
                "                 AND lastExecutionFailed = 1\n" +
                "                 AND nextexecutiontimestamp > round((SYSDATE - date '1970-01-01')*24*60*60)\n" +
                "                 AND lastsuccessfulcommunicationend is not null\n" +
                "            THEN 'Failed'\n" +
                "            WHEN currentretrycount = 0\n" +
                "                 AND lastExecutionFailed = 0\n" +
                "                 AND nextexecutiontimestamp > round((SYSDATE - date '1970-01-01')*24*60*60)\n" +
                "                 AND lastsuccessfulcommunicationend is not null\n" +
                "            THEN 'Waiting'\n" +
                "            ELSE 'ProcessingError'\n" +
                "        END taskStatus,\n" +
                "     --\n" +
                "       CASE WHEN ct.lastSessionSuccessIndicator = 0\n" +
                "                 AND failedTask.comsession IS NULL\n" +
                "            THEN 1\n" +
                "            ELSE 0\n" +
                "        END completeSucces,\n" +
                "     --\n" +
                "       CASE WHEN ct.lastSessionSuccessIndicator = 0\n" +
                "                 AND failedTask.comsession IS NOT NULL\n" +
                "            THEN 1\n" +
                "            ELSE 0\n" +
                "        END atLeastOneFailure,\n" +
                "     --\n" +
                "       CASE WHEN ct.lastSessionSuccessIndicator = 1\n" +
                "            THEN 1\n" +
                "            ELSE 0\n" +
                "        END failureSetupError,\n" +
                "     --\n" +
                "       CASE WHEN ct.lastSessionSuccessIndicator = 2\n" +
                "            THEN 1\n" +
                "            ELSE 0\n" +
                "        END failureBroken,\n" +
                "     --\n" +
                "       CASE WHEN ct.lastSessionSuccessIndicator = 3\n" +
                "            THEN 1\n" +
                "            ELSE 0\n" +
                "        END failureInterrupted,\n" +
                "     --\n" +
                "       CASE WHEN ct.lastSessionSuccessIndicator = 4\n" +
                "            THEN 1\n" +
                "            ELSE 0\n" +
                "        END failureNot_Executed,\n" +
                "     --\n" +
                "       CASE WHEN ct.status                  = 0\n" +
                "             AND ct.nextexecutiontimestamp is not null\n" +
                "            THEN 1\n" +
                "            ELSE 0\n" +
                "        END IS_MV_CONTASKBREAKDOWN,\n" +
                "     --\n" +
                "       CASE WHEN ct.status = 0\n" +
                "            THEN 1\n" +
                "            ELSE 0\n" +
                "        END IS_MV_CONNECTIONTYPEHEATMAP,\n" +
                "     --\n" +
                "       CASE WHEN ct.nextexecutiontimestamp is not null\n" +
                "             AND ct.lastsession            is not null\n" +
                "            THEN 1\n" +
                "            ELSE 0\n" +
                "        END IS_MV_CTLCSSUCINDCOUNT,\n" +
                "     --\n" +
                "       CASE WHEN ct.nextexecutiontimestamp          IS NOT NULL\n" +
                "                 AND ct.lastSessionSuccessIndicator  = 0\n" +
                "                 AND failedTask.comSession          IS NOT NULL\n" +
                "            THEN 1\n" +
                "            ELSE 0\n" +
                "        END IS_MV_CTLCSWITHATLSTONEFT\n" +
                "  FROM\n" +
                "  --\n" +
                "       DDC_CONNECTIONTASK ct\n" +
                "  --\n" +
                "       JOIN DDC_DEVICE    dev ON ct.device = dev.id\n" +
                "  --\n" +
                "  LEFT JOIN MTG_ED_GROUP grdesc\n" +
                " ON exists(select * from MTG_ENUM_ED_IN_GROUP where group_id = grdesc.id and enddevice_id = dev.meterid) or " +
                " exists(select * from DYNAMIC_GROUP_DATA where group_id = grdesc.id and device_id = dev.id) " +
                "  --\n" +
                "       LEFT JOIN\n" +
                "                 ( SELECT comsession\n" +
                "                     FROM DDC_COMTASKEXECSESSION\n" +
                "                    WHERE successindicator > 0\n" +
                "                    GROUP BY comsession\n" +
                "                 ) failedTask\n" +
                "       ON ct.lastSession = failedTask.comSession\n" +
                "  --\n" +
                "       LEFT OUTER JOIN (\n" +
                "                         SELECT connectiontask\n" +
                "                           FROM DDC_COMTASKEXEC\n" +
                "                          WHERE comschedule   is not null   -- added mail Jozsef\n" +
                "                            AND comport       is not null\n" +
                "                            AND obsolete_date is null\n" +
                "                          GROUP BY connectiontask\n" +
                "\t                      )\tctsFromCtes\n" +
                "       ON ct.id = ctsFromCtes.connectiontask\n" +
                "  WHERE ct.obsolete_date  is null");
        return sqlBuilder;
    }

    public String getDashboardConTaskBreakdownDataQuery() {
        String conTaskBreakdownQuery = "insert into DASHBOARD_CONTASKBREAKDOWN\n" +
                "       (grouperby, devicetype, mrid, item, taskstatus, count)\n" +
                "WITH\n" +
                "--\n" +
                "alldata as (\n" +
                "  select COMPORTPOOL, CONNECTIONTYPEPLUGGABLECLASS, DEVICETYPE, MRID, TASKSTATUS, count(*) counter\n" +
                "    from MV_CONNECTIONDATA\n" +
                "   where IS_MV_CONTASKBREAKDOWN = 1\n" +
                "   group by COMPORTPOOL, CONNECTIONTYPEPLUGGABLECLASS, DEVICETYPE, mrid, TASKSTATUS\n" +
                ")\n" +
                "--\n" +
                "SELECT 'None', devicetype, mrid, null as item, taskStatus, nvl ( sum ( counter ) , 0 )\n" +
                "  FROM alldata\n" +
                " GROUP BY devicetype, mrid, taskStatus\n" +
                "--\n" +
                "UNION  ALL\n" +
                "--\n" +
                "SELECT 'ComPortPool', devicetype, mrid, comportpool, taskStatus, nvl ( sum ( counter ) , 0 )\n" +
                "  FROM alldata\n" +
                " GROUP BY devicetype, mrid, taskStatus, comportpool\n" +
                "--\n" +
                "UNION  ALL\n" +
                "--\n" +
                "SELECT 'ConnectionType', devicetype, mrid, connectiontypepluggableclass, taskStatus, nvl ( sum ( counter ) , 0 )\n" +
                "  FROM alldata\n" +
                " GROUP BY devicetype, mrid, taskStatus, connectiontypepluggableclass\n" +
                "--\n" +
                "UNION  ALL\n" +
                "--\n" +
                "SELECT 'DeviceType', devicetype, mrid, devicetype, taskStatus, nvl ( sum ( counter ) , 0 )\n" +
                "  FROM alldata\n" +
                " GROUP BY devicetype, mrid, taskStatus";
        return conTaskBreakdownQuery;
    }

    public String getDashboardConTypeHeatMapDataQuery() {
        String ConTypeHeatMapDataQuery = "insert into DASHBOARD_CONTYPEHEATMAP\n" +
                "       ( connectiontypepluggableclass, devicetype, mrid, comportpool, completeSucces,\n" +
                "         atLeastOneFailure, failureSetupError, failureBroken, failureInterrupted,\n" +
                "         failureNot_Execute)\n" +
                "SELECT connectiontypepluggableclass,\n" +
                "       devicetype,\n" +
                "       mrid,\n" +
                "       comportpool,\n" +
                "       sum ( completeSucces ),\n" +
                "       sum ( atLeastOneFailure ),\n" +
                "       sum ( failureSetupError ) ,\n" +
                "       sum ( failureBroken ),\n" +
                "       sum ( failureInterrupted ),\n" +
                "       sum ( failureNot_Executed )\n" +
                "  FROM MV_CONNECTIONDATA\n" +
                " WHERE IS_MV_ConnectionTypeHeatMap = 1\n" +
                " GROUP BY connectiontypepluggableclass, devicetype, mrid, comportpool";

        return ConTypeHeatMapDataQuery;
    }

    public String getDashboardConTaskLastSessionSuccessIndicatorCountDataQuery() {
        String ConTaskLastSessionSuccessIndicatorCountDataQuery = "insert into DASHBOARD_CTLCSSUCINDCOUNT\n" +
                "       (devicetype, mrid, lastSessionSuccessIndicator, count)\n" +
                "SELECT devicetype,\n" +
                "       mrid,\n" +
                "       lastSessionSuccessIndicator,\n" +
                "       count ( * )\n" +
                "  FROM MV_CONNECTIONDATA\n" +
                " WHERE IS_MV_CTLCSSucIndCount = 1\n" +
                " GROUP BY devicetype, mrid, lastSessionSuccessIndicator";

        return ConTaskLastSessionSuccessIndicatorCountDataQuery;
    }

    public String getDashboardConTaskLastSessionWithAtLeastOneFailedTaskCountDataQuery() {
        String conTaskLastSessionWithAtLeastOneFailedTaskCountDataQuery = "insert into DASHBOARD_CTLCSWITHATLSTONEFT\n" +
                "       (devicetype, mrid, count)\n" +
                "SELECT devicetype,\n" +
                "       mrid,\n" +
                "       count ( * )\n" +
                "  FROM MV_CONNECTIONDATA\n" +
                " WHERE IS_MV_CTLCSWithAtLstOneFT  = 1\n" +
                "   AND lastsession is not null\n" +
                " GROUP BY devicetype, mrid";

        return conTaskLastSessionWithAtLeastOneFailedTaskCountDataQuery;
    }

    public SqlBuilder getDynamicGroupDataBuilder() {
        SqlBuilder builder = new SqlBuilder();
        builder.append("CREATE GLOBAL TEMPORARY TABLE DYNAMIC_GROUP_DATA on commit preserve rows AS ( ");
        String unionAll = "";
        for (QueryEndDeviceGroup group : queryEndDeviceGroupList) {
            builder.append(unionAll);
            builder.append("select " + group.getId());
            builder.append(" as group_id, id as device_id from (");
            builder.append(new QueryStringifier(group.toFragment()).getQuery());
            builder.closeBracket();
            unionAll = " union all ";
        }
        builder.closeBracket();
        return builder;
    }
}
