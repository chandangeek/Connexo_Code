CREATE OR REPLACE PROCEDURE connection_task_status AUTHID CURRENT_USER
AS
BEGIN
execute immediate 'DROP TABLE MV_CONNECTIONDATA';

EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF;

execute immediate 'CREATE GLOBAL TEMPORARY TABLE MV_CONNECTIONDATA on commit preserve rows
AS
SELECT
       ct.lastsession,
       dev.devicetype,
       grdesc.mrid,
       failedTask.comSession failedTask_comSession,
       ct.connectiontypepluggableclass,
       ct.device,
       ct.comportpool,
       ct.lastSessionSuccessIndicator,
       CASE
            WHEN ctsFromCtes.connectiontask IS NOT NULL
                 OR ct.comport   IS NOT NULL
            THEN ''Busy''
            WHEN ( discriminator = ''1'' AND status > 0)
                 OR ( discriminator = ''2'' AND ( status > 0 OR nextExecutionTimestamp is null ) )
            THEN ''OnHold''
            WHEN nextexecutiontimestamp <= round((SYSDATE - date ''1970-01-01'')*24*60*60)
            THEN ''Pending''
            WHEN currentretrycount = 0
                 AND nextexecutiontimestamp > round((SYSDATE - date ''1970-01-01'')*24*60*60)
                 AND lastsuccessfulcommunicationend is null
            THEN ''NeverCompleted''
            WHEN currentretrycount > 0
                 AND nextexecutiontimestamp > round((SYSDATE - date ''1970-01-01'')*24*60*60)
            THEN ''Retrying''
            WHEN currentretrycount = 0
                 AND lastExecutionFailed = 1
                 AND nextexecutiontimestamp > round((SYSDATE - date ''1970-01-01'')*24*60*60)
                 AND lastsuccessfulcommunicationend is not null
            THEN ''Failed''
            WHEN currentretrycount = 0
                 AND lastExecutionFailed = 0
                 AND nextexecutiontimestamp > round((SYSDATE - date ''1970-01-01'')*24*60*60)
                 AND lastsuccessfulcommunicationend is not null
            THEN ''Waiting''
            ELSE ''ProcessingError''
        END taskStatus,
     --
       CASE WHEN ct.lastSessionSuccessIndicator = 0
                 AND failedTask.comsession IS NULL
            THEN 1
            ELSE 0
        END completeSucces,
     --
       CASE WHEN ct.lastSessionSuccessIndicator = 0
                 AND failedTask.comsession IS NOT NULL
            THEN 1
            ELSE 0
        END atLeastOneFailure,
     --
       CASE WHEN ct.lastSessionSuccessIndicator = 1
            THEN 1
            ELSE 0
        END failureSetupError,
     --
       CASE WHEN ct.lastSessionSuccessIndicator = 2
            THEN 1
            ELSE 0
        END failureBroken,
     --
       CASE WHEN ct.lastSessionSuccessIndicator = 3
            THEN 1
            ELSE 0
        END failureInterrupted,
     --
       CASE WHEN ct.lastSessionSuccessIndicator = 4
            THEN 1
            ELSE 0
        END failureNot_Executed,
     --
       CASE WHEN ct.status                  = 0
             AND ct.nextexecutiontimestamp is not null
            THEN 1
            ELSE 0
        END IS_MV_CONTASKBREAKDOWN,
     --
       CASE WHEN ct.status = 0
            THEN 1
            ELSE 0
        END IS_MV_CONNECTIONTYPEHEATMAP,
     --
       CASE WHEN ct.nextexecutiontimestamp is not null
             AND ct.lastsession            is not null
            THEN 1
            ELSE 0
        END IS_MV_CTLCSSUCINDCOUNT,
     --
       CASE WHEN ct.nextexecutiontimestamp          IS NOT NULL
                 AND ct.lastSessionSuccessIndicator  = 0
                 AND failedTask.comSession          IS NOT NULL
            THEN 1
            ELSE 0
        END IS_MV_CTLCSWITHATLSTONEFT
  FROM
  --
       DDC_CONNECTIONTASK ct
  --
       JOIN DDC_DEVICE    dev ON ct.device = dev.id
  --
       LEFT JOIN MTG_ENUM_ED_IN_GROUP gr
       ON gr.enddevice_id = dev.meterid
  --
       LEFT JOIN MTG_ED_GROUP  grdesc
       ON grdesc.id = gr.group_id
  --
       LEFT JOIN
                 ( SELECT comsession
                     FROM DDC_COMTASKEXECSESSION
                    WHERE successindicator > 0
                    GROUP BY comsession
                 ) failedTask
       ON ct.lastSession = failedTask.comSession
  --
       LEFT OUTER JOIN (
                         SELECT connectiontask
                           FROM DDC_COMTASKEXEC
                          WHERE comschedule   is not null   -- added mail Jozsef
                            AND comport       is not null
                            AND obsolete_date is null
                          GROUP BY connectiontask
	                      )	ctsFromCtes
       ON ct.id = ctsFromCtes.connectiontask
  WHERE ct.obsolete_date  is null';

--
execute immediate 'delete DASHBOARD_CONTASKBREAKDOWN';
execute immediate 'insert into DASHBOARD_CONTASKBREAKDOWN
       (grouperby, devicetype, mrid, item, taskstatus, count)
WITH
--
alldata as (
  select COMPORTPOOL, CONNECTIONTYPEPLUGGABLECLASS, DEVICETYPE, MRID, TASKSTATUS, count(*) counter
    from MV_CONNECTIONDATA
   where IS_MV_CONTASKBREAKDOWN = 1
   group by COMPORTPOOL, CONNECTIONTYPEPLUGGABLECLASS, DEVICETYPE, mrid, TASKSTATUS
)
--
SELECT ''None'', devicetype, mrid, null as item, taskStatus, nvl ( sum ( counter ) , 0 )
  FROM alldata
 GROUP BY devicetype, mrid, taskStatus
--
UNION  ALL
--
SELECT ''ComPortPool'', devicetype, mrid, comportpool, taskStatus, nvl ( sum ( counter ) , 0 )
  FROM alldata
 GROUP BY devicetype, mrid, taskStatus, comportpool
--
UNION  ALL
--
SELECT ''ConnectionType'', devicetype, mrid, connectiontypepluggableclass, taskStatus, nvl ( sum ( counter ) , 0 )
  FROM alldata
 GROUP BY devicetype, mrid, taskStatus, connectiontypepluggableclass
--
UNION  ALL
--
SELECT ''DeviceType'', devicetype, mrid, devicetype, taskStatus, nvl ( sum ( counter ) , 0 )
  FROM alldata
 GROUP BY devicetype, mrid, taskStatus, devicetype';

--
execute immediate 'delete DASHBOARD_CONTYPEHEATMAP';
execute immediate 'insert into DASHBOARD_CONTYPEHEATMAP
       ( connectiontypepluggableclass, devicetype, mrid, comportpool, completeSucces,
         atLeastOneFailure, failureSetupError, failureBroken, failureInterrupted,
         failureNot_Execute)
SELECT connectiontypepluggableclass,
       devicetype,
       mrid,
       comportpool,
       sum ( completeSucces ),
       sum ( atLeastOneFailure ),
       sum ( failureSetupError ) ,
       sum ( failureBroken ),
       sum ( failureInterrupted ),
       sum ( failureNot_Executed )
  FROM MV_CONNECTIONDATA
 WHERE IS_MV_ConnectionTypeHeatMap = 1
 GROUP BY connectiontypepluggableclass, devicetype, mrid, comportpool';

--
execute immediate 'delete DASHBOARD_CTLCSSUCINDCOUNT';
execute immediate 'insert into DASHBOARD_CTLCSSUCINDCOUNT
       (devicetype, mrid, lastSessionSuccessIndicator, count)
SELECT devicetype,
       mrid,
       lastSessionSuccessIndicator,
       count ( * )
  FROM MV_CONNECTIONDATA
 WHERE IS_MV_CTLCSSucIndCount = 1
 GROUP BY devicetype, mrid, lastSessionSuccessIndicator';

--
execute immediate 'delete DASHBOARD_CTLCSWITHATLSTONEFT';
execute immediate 'insert into DASHBOARD_CTLCSWITHATLSTONEFT
       (devicetype, mrid, count)
SELECT devicetype,
       mrid,
       count ( * )
  FROM MV_CONNECTIONDATA
 WHERE IS_MV_CTLCSWithAtLstOneFT  = 1
   AND lastsession is not null
 GROUP BY devicetype, mrid';
--
execute immediate 'TRUNCATE TABLE MV_CONNECTIONDATA';
--
execute immediate 'DROP TABLE MV_CONNECTIONDATA';
--
commit;
END;
