CREATE OR REPLACE PROCEDURE communication_task_status
AS
  B1 number := 86400 * (sysdate - to_date('1970/01/01', 'YYYY/MM/DD'));
BEGIN
execute immediate 'delete dashboard_comtask';
INSERT into dashboard_comtask
       ( querytype, devicetype, mrid, lastsesshighestcompcode, heatmapcount,
         tasktype, status, comschedule, count )
--
WITH
--
alldata as (
--
  SELECT
         cte.id,
         cte.nextexecutiontimestamp,
         cte.lastexecutiontimestamp,
         cte.plannednextexecutiontimestamp,
         cte.discriminator,
         cte.nextexecutionspecs,
         cte.comport,
         cte.onhold,
         cte.currentretrycount,
         cte.lastsuccessfulcompletion,
         cte.lastexecutionfailed,
         cte.comtask,
         cte.comschedule,
         cte.device,
         cte.lastsession,
         cte.lastsess_highestpriocomplcode,
         dev.devicetype,
         CASE WHEN ct.id                IS NULL THEN 0 ELSE 1 END as thereisabusytask,
         CASE WHEN hp.comtaskexecution = cte.id THEN 1 ELSE 0 END as isapriotask,
         grdesc.mrid
   --
     FROM	DDC_COMTASKEXEC cte
   --
          JOIN DDC_DEVICE dev	ON cte.device = dev.id
   --
          LEFT JOIN MTG_ENUM_ED_IN_GROUP gr
          ON gr.enddevice_id = dev.meterid
   --
          LEFT JOIN MTG_ED_GROUP  grdesc
          ON grdesc.id = gr.group_id
   --
          LEFT OUTER JOIN DDC_CONNECTIONTASK ct
          ON  cte.connectiontask             = ct.id
              AND ct.comPort                is not null
              AND ct.lastCommunicationStart  > cte.nextExecutionTimestamp
   --
          LEFT JOIN DDC_HIPRIOCOMTASKEXEC hp
          ON hp.comtaskexecution = cte.id
   --
    WHERE cte.obsolete_date is null
      AND comschedule is not null      -- added by Jozsef
),
--
alldatagrouped as (
--
 SELECT status,
        comtask,
        comschedule,
        devicetype,
        mrid,
        sum ( q1_count )      as q1_count,
        sum ( q2_count )      as q2_count,
        count ( comschedule ) as csched_count,
        count ( comtask )     as ctask_count,
        count ( devicetype )  as devtype_count
   FROM
        (
          SELECT comtask,
                 comschedule,
                 devicetype,
                 mrid,
                 1 as q1_count,
                 CASE WHEN comschedule is null
                      THEN 0
                      ELSE 1
                  END q2_count,
                 CASE
                   --
                      WHEN onhold = 0
                           AND ( comport is not null OR  ( thereisabusytask = 1
                                                           AND nextexecutiontimestamp <= B1 )
                               )
                      THEN 'Busy'
                   --
                      WHEN onhold = 0
                           AND isapriotask = 0
                           AND comport is null
                           AND thereisabusytask = 0
                           AND nextexecutiontimestamp <= B1
                      THEN 'Pending'
                   --
                      WHEN onhold = 0
                           AND isapriotask = 1
                           AND comport is null
                           AND thereisabusytask = 0
                           AND nextexecutiontimestamp <= B1
                      THEN 'PendingWithPriority'
                   --
                      WHEN onhold = 0
                           AND comport is null
                           AND currentretrycount = 0
                           AND lastsuccessfulcompletion is null
                           AND lastExecutionTimestamp is not null
                           AND ( nextExecutionTimestamp is null
                            OR  nextExecutionTimestamp > B1 )
                      THEN 'NeverCompleted'
                   --
                      WHEN onhold = 0
                           AND isapriotask = 0
                           AND nextexecutiontimestamp > B1
                           AND comport is null
                           AND currentretrycount > 0
                      THEN 'Retrying'
                   --
                      WHEN onhold = 0
                           AND isapriotask = 1
                           AND nextexecutiontimestamp > B1
                           AND comport is null
                           AND currentretrycount > 0
                      THEN 'RetryingWithPriority'
                   --
                      WHEN onhold = 0
                           AND nextexecutiontimestamp > B1
                           AND lastsuccessfulcompletion is not null
                           AND lastExecutionTimestamp > lastSuccessfulCompletion
                           AND lastExecutionfailed = 1
                           AND currentretrycount = 0
                      THEN 'Failed'
                   --
                      WHEN onhold = 0
                           AND isapriotask = 0
                           AND comport is null
                           AND lastexecutionfailed = 0
                           AND currentretrycount = 0
                           AND ( lastExecutionTimestamp is null OR  lastSuccessfulCompletion is not null )
                           AND ( ( plannednextexecutiontimestamp IS NULL AND nextexecutiontimestamp IS NULL )
                                 OR  ( ( discriminator = 2
                                          OR  ( discriminator = 1 AND nextexecutionspecs IS NULL )
                                       )
                                       AND ( nextexecutiontimestamp IS NULL OR  nextexecutiontimestamp > B1 )
                                     )
                                 OR  ( nextexecutionspecs IS NOT NULL  AND ( nextexecutiontimestamp IS NULL
                                                                             OR  nextexecutiontimestamp > B1 )
                                     )
                               )
                      THEN 'Waiting'
                   --
                      WHEN onhold = 0
                           AND isapriotask = 1
                           AND comport is null
                           AND lastexecutionfailed = 0
                           AND currentretrycount = 0
                           AND ( lastExecutionTimestamp is null  OR  lastSuccessfulCompletion is not null )
                           AND ( ( plannednextexecutiontimestamp IS NULL  AND nextexecutiontimestamp IS NULL )
                                   OR  ( ( discriminator = 2 OR  ( discriminator = 1 AND nextexecutionspecs IS NULL ) )
                                         AND ( nextexecutiontimestamp IS NULL  OR  nextexecutiontimestamp > B1 )
                                       )
                                   OR  ( nextexecutionspecs IS NOT NULL
                                         AND ( nextexecutiontimestamp IS NULL OR  nextexecutiontimestamp > B1 )
                                       )
                               )
                      THEN 'WaitingWithPriority'
                   --
                      WHEN onhold <> 0
                      THEN 'OnHold'
                   --
                      ELSE 'ProcessingError'
                   --
                  END AS status
            FROM alldata
        )
  GROUP BY status, comtask, comschedule, devicetype, mrid
)
--
SELECT 'COMTASK_Q2' as QTYPE,
       DEVICETYPE,
       mrid,
       lastsess_highestpriocomplcode,
       count ( * ) as heatmapcount,
       null,
       null,
       null,
       null
  FROM alldata
 WHERE
 -- rowtype2 = 1 and
   lastsession   is not null
 GROUP BY devicetype, mrid, lastsess_highestpriocomplcode
--
UNION ALL
--
SELECT 'COMTASK_Q1' as QTYPE,
       DEVICETYPE,
       mrid,
       null,
       null,
       'None', status, null as item, sum ( q1_count ) as count
  FROM alldatagrouped
 GROUP BY devicetype, mrid, status
--
UNION  ALL
--
SELECT 'COMTASK_Q1' as QTYPE,
       DEVICETYPE,
       mrid,
       null,
       null,
       'ComSchedule', status, comschedule, sum ( q2_count ) as count
  FROM alldatagrouped
 WHERE comschedule is not null
   AND status <> 'OnHold'
 GROUP BY devicetype, mrid, status, comschedule
--
UNION  ALL
--
SELECT 'COMTASK_Q1' as QTYPE,
       DEVICETYPE,
       mrid,
       null,
       null,
       'ComTask', status, comtask, sum ( ctask_count ) as count
  FROM alldatagrouped
 WHERE comschedule is null
   AND status <> 'OnHold'
 GROUP BY devicetype, mrid, status, comtask
--
UNION  ALL
--
SELECT 'COMTASK_Q1' as QTYPE,
       DEVICETYPE,
       gr.mrid,
       null,
       null,
       'ComTask', GR.status, ctincs.comtask, sum ( csched_count ) as count
  FROM alldatagrouped GR
       LEFT OUTER JOIN sch_comschedule csch ON GR.comschedule = csch.id
       LEFT OUTER JOIN sch_comtaskincomschedule ctincs ON csch.id = ctincs.comschedule
 WHERE GR.comschedule is not null
   AND GR.status <> 'OnHold'
 GROUP BY devicetype, GR.status, gr.mrid, ctincs.comtask
--
UNION  ALL
--
SELECT 'COMTASK_Q1' as QTYPE,
       DEVICETYPE,
       mrid,
       null,
       null,
       'DeviceType', status, devicetype, sum ( devtype_count ) as count
  FROM alldatagrouped
 WHERE status <> 'OnHold'
 GROUP BY devicetype, mrid, status, devicetype;
 commit;
END;
