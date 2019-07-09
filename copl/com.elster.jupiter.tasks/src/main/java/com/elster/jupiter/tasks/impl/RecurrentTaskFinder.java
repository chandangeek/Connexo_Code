/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.RecurrentTaskFilterSpecification;
import com.elster.jupiter.tasks.TaskFinder;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.sql.Fetcher;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@LiteralSql
public class RecurrentTaskFinder implements TaskFinder {

    private DataModel dataModel;
    private RecurrentTaskFilterSpecification filter;
    private Integer start;
    private Integer limit;

    public RecurrentTaskFinder(DataModel dataModel, RecurrentTaskFilterSpecification filter, Integer start, Integer limit) {
        this.dataModel = dataModel;
        this.filter = filter;
        this.start = start;
        this.limit = limit;
    }


    public List<RecurrentTask> find() {
        try (Connection connection = dataModel.getConnection(false)) {
            return findTasks(connection);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private List<RecurrentTask> findTasks(Connection connection) throws SQLException {
        DataMapper<RecurrentTaskImpl> mapper = dataModel.mapper(RecurrentTaskImpl.class);
        //SqlBuilder builder = mapper.builder("RT");
        SqlBuilder builder = new SqlBuilder();
        builder.append("select * from (select ID, APPLICATION, NAME, CRONSTRING, NEXTEXECUTION, PAYLOAD, DESTINATION, PRIORITY, LASTRUN, VERSIONCOUNT, CREATETIME, MODTIME, USERNAME, LOGLEVEL, QUEUE_TYPE_NAME, ROWNUM as rnum from (");
        builder.append("select RT.ID, RT.APPLICATION, RT.NAME, RT.CRONSTRING, RT.NEXTEXECUTION, RT.PAYLOAD, RT.DESTINATION, RT.PRIORITY, RT.LASTRUN, RT.VERSIONCOUNT, RT.CREATETIME, RT.MODTIME, RT.USERNAME, RT.LOGLEVEL, DS.QUEUE_TYPE_NAME, ROWNUM as rnum ");
        builder.append(" from TSK_RECURRENT_TASK RT ");
        builder.append(" inner join (select NAME, QUEUE_TYPE_NAME from MSG_DESTINATIONSPEC) DS on RT.DESTINATION = DS.NAME ");
        builder.append(" inner join ");
        builder.append("(select * from ");
        builder.append("(select x.*, ROWNUM rnum from ");
        builder.append("(with busy as (select TSK.ID as \"TSKID\", sign(nvl(OCC.STATUS, 1)) AS \"TSKSTATUS\", OCC.STARTDATE   from ( ");
        builder.append("(select * from TSK_RECURRENT_TASK WHERE NEXTEXECUTION IS NOT NULL) TSK ");
        builder.append("LEFT JOIN ");
        builder.append("(select * from TSK_TASK_OCCURRENCE where TSK_TASK_OCCURRENCE.ID in ");
        builder.append("(select max(OCC.ID) \"OCCID\" from TSK_TASK_OCCURRENCE OCC group by OCC.RECURRENTTASKID)) OCC ");
        builder.append("on TSK.ID=OCC.RECURRENTTASKID ");
        builder.append(") where OCC.STATUS=0), ");
        builder.append("notbusy as (select TSK.ID as \"TSKID\", sign(nvl(OCC.STATUS, 1)) AS \"TSKSTATUS\", TSK.NEXTEXECUTION  from ( ");
        builder.append("(select * from TSK_RECURRENT_TASK) TSK ");
        builder.append("LEFT JOIN");
        builder.append("(select * from TSK_TASK_OCCURRENCE where TSK_TASK_OCCURRENCE.ID in ");
        builder.append("(select max(OCC.ID) \"OCCID\" from TSK_TASK_OCCURRENCE OCC group by OCC.RECURRENTTASKID)) OCC ");
        builder.append("on TSK.ID=OCC.RECURRENTTASKID ");
        builder.append(") where ((OCC.STATUS is null) or (OCC.STATUS > 0))) ");
        builder.append("select * from (select * from busy ORDER BY STARTDATE ASC) ");
        builder.append("union all ");
        //builder.append("select * from (select * from planned ORDER BY NEXTEXECUTION ASC)) x where ROWNUM <=  ");
        //builder.addInt(start+limit+1);
        builder.append("select * from (select * from notbusy ORDER BY NEXTEXECUTION ASC)) x ");

        builder.append(" ) ");
        //builder.append("where rnum >= ");
        //builder.addInt(start+1);
        builder.append(") ");
        builder.append("on RT.ID=TSKID ");

        //add started between conditions
        boolean isStartedBetweenConditionPresent = (filter.startedOnFrom != null) || (filter.startedOnTo != null);
        if (isStartedBetweenConditionPresent) {
            builder.append("where exists (select * from TSK_TASK_OCCURRENCE where ");
            if (filter.startedOnFrom != null) {
                builder.append(" TSK_TASK_OCCURRENCE.STARTDATE >= ");
                builder.addLong(filter.startedOnFrom.toEpochMilli());
            }
            if ((filter.startedOnFrom != null) && (filter.startedOnTo != null)) {
                builder.append(" and ");
            }
            if (filter.startedOnTo != null) {
                builder.append(" TSK_TASK_OCCURRENCE.STARTDATE <= ");
                builder.addLong(filter.startedOnTo.toEpochMilli());
            }
            builder.append(") ");
        }

        //add next execution between conditions
        boolean isNextExecutionBetweenConditionPresent = (filter.nextExecutionFrom != null) || (filter.nextExecutionTo != null);
        if (isNextExecutionBetweenConditionPresent) {
            builder.append(isStartedBetweenConditionPresent ? " and ( " : " where ( ");
            if (filter.nextExecutionFrom != null) {
                builder.append(" NEXTEXECUTION >= ");
                builder.addLong(filter.nextExecutionFrom.toEpochMilli());
            }
            if ((filter.nextExecutionFrom != null) && (filter.nextExecutionTo != null)) {
                builder.append(" and ");
            }
            if (filter.nextExecutionTo != null) {
                builder.append(" NEXTEXECUTION <= ");
                builder.addLong(filter.nextExecutionTo.toEpochMilli());
            }
            builder.append(") ");
        }

        //add priority between conditions
        boolean isPriorityBetweenConditionPresent = (filter.priorityFrom != null) || (filter.priorityTo != null);
        if (isPriorityBetweenConditionPresent) {
            builder.append((isStartedBetweenConditionPresent || isNextExecutionBetweenConditionPresent) ? " and ( " : " where ( ");
            if (filter.priorityFrom != null) {
                builder.append(" PRIORITY >= ");
                builder.addInt(filter.priorityFrom);
            }
            if ((filter.priorityFrom != null) && (filter.priorityTo != null)) {
                builder.append(" and ");
            }
            if (filter.priorityTo != null) {
                builder.append(" PRIORITY <= ");
                builder.addInt(filter.priorityTo);
            }
            builder.append(") ");
        }

        //add queues filter conditions
        boolean isQueueFilterConditionPresent = (filter.queues != null) && !filter.queues.isEmpty();
        if (isQueueFilterConditionPresent) {
            builder.append((isStartedBetweenConditionPresent || isNextExecutionBetweenConditionPresent || isPriorityBetweenConditionPresent) ?
                            " and ( " : " where ( ");
            List<String> queues = new ArrayList();
            queues.addAll(filter.queues);
            builder.append("DESTINATION in ( ");
            for (int i = 0; i < queues.size(); i++) {
                builder.addObject(queues.get(i));
                builder.append((i < queues.size() - 1) ? " , " : "");
            }
            builder.append(")) ");
        }

        //add queue type filter conditions
        boolean isQueueTypeConditionPresent = (filter.queueTypes != null) && !filter.queueTypes.isEmpty();
        if (isQueueTypeConditionPresent) {
            builder.append((isStartedBetweenConditionPresent || isNextExecutionBetweenConditionPresent
                            || isPriorityBetweenConditionPresent || isQueueFilterConditionPresent) ?
                            " and ( " : " where ( ");

            List<String> queueTypes = new ArrayList();
            queueTypes.addAll(filter.queueTypes);
            builder.append("QUEUE_TYPE_NAME in (");
            for (int i = 0; i < queueTypes.size(); i++) {
                builder.addObject(queueTypes.get(i));
                builder.append((i < queueTypes.size() - 1) ? " , " : "");
            }
            builder.append(")) ");
        }

        //add application filter conditions
        boolean isApplicationFilterConditionPresent = (filter.applications != null) && !filter.applications.isEmpty();
        if (isApplicationFilterConditionPresent) {
            builder.append((isStartedBetweenConditionPresent || isNextExecutionBetweenConditionPresent
                            || isPriorityBetweenConditionPresent || isQueueFilterConditionPresent || isQueueTypeConditionPresent) ?
                           " and ( " : " where ( ");

            List<String> applications = new ArrayList();
            applications.addAll(filter.applications);
            builder.append("APPLICATION in (");
            for (int i = 0; i < applications.size(); i++) {
                builder.addObject(applications.get(i));
                builder.append((i < applications.size() - 1) ? " , " : "");
            }
            builder.append(")) ");
        }

        // add sorting conditions
        builder.append("order by ");
        if (!filter.sortingColumns.isEmpty()) {
            Order[] order = filter.sortingColumns.toArray(new Order[filter.sortingColumns.size()]);
            for (int i = 0; i < order.length; i++) {
                switch (order[i].getName()) {
                    case "nextRun":
                        builder.append(" TSKSTATUS, STARTDATE " + order[i].ordering());
                        builder.append((i < order.length - 1) ? " , " : "");
                        break;
                    case "queue":
                        builder.append(" DESTINATION " + order[i].ordering());
                        builder.append((i < order.length - 1) ? " , " : "");
                        break;
                    case "priority":
                        builder.append(" PRIORITY " + order[i].ordering());
                        builder.append((i < order.length - 1) ? " , " : "");
                        break;
                }
            }
        } else {
            builder.append("NAME ");
        }
        builder.append(")) ");

        // add pagging
        builder.append(" where rnum <=  ");
        builder.addInt(start + limit + 1);
        builder.append(" and rnum >= ");
        builder.addInt(start + 1);

        try (Fetcher<RecurrentTaskImpl> fetcher = mapper.fetcher(builder)) {
            return getRecurrentTasks(fetcher);
        }
    }

    private List<RecurrentTask> getRecurrentTasks(Fetcher<RecurrentTaskImpl> fetcher) throws SQLException {
        List<RecurrentTask> result = new ArrayList<>();
        for (RecurrentTaskImpl task : fetcher) {
            result.add(task);
        }
        return result;
    }
}

