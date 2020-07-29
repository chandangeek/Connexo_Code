/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.RecurrentTaskFilterSpecification;
import com.elster.jupiter.tasks.TaskFinder;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.sql.Fetcher;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

    public RecurrentTaskFinder(DataModel dataModel, RecurrentTaskFilterSpecification filter) {
        this(dataModel, filter, null, null);
    }


    public List<RecurrentTask> find() {
        try (Connection connection = dataModel.getConnection(false)) {
            return getRecurrentTasks(dataModel.mapper(RecurrentTaskImpl.class).fetcher(getTasksSqlBuilder(false)));
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    @Override
    public long count() {
        try (Connection connection = dataModel.getConnection(false)) {
            try (PreparedStatement statement = getTasksSqlBuilder(true).prepare(connection)) {
                try (ResultSet rs = statement.executeQuery()) {
                    return rs.next() ? rs.getLong(1) : 0;
                }
            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private SqlBuilder getTasksSqlBuilder(boolean count) {
        SqlBuilder builder = new SqlBuilder();
        if (count) {
            builder.append("select count(*)");
        } else {
            builder.append("select *");
        }
        builder.append(" from (select ID, APPLICATION, NAME, CRONSTRING, NEXTEXECUTION, PAYLOAD, DESTINATION, PRIORITY, LASTRUN, VERSIONCOUNT, CREATETIME, MODTIME, USERNAME, LOGLEVEL, SUSPENDUNTIL, QUEUE_TYPE_NAME, ROWNUM as rnum from (");
        builder.append("select RT.ID, RT.APPLICATION, RT.NAME, RT.CRONSTRING, RT.NEXTEXECUTION, RT.PAYLOAD, RT.DESTINATION, RT.PRIORITY, RT.LASTRUN, RT.VERSIONCOUNT, RT.CREATETIME, RT.MODTIME, RT.USERNAME, RT.LOGLEVEL, RT.SUSPENDUNTIL, DS.QUEUE_TYPE_NAME, ROWNUM as rnum ");
        builder.append(" from TSK_RECURRENT_TASK RT ");
        builder.append(" inner join (select NAME, QUEUE_TYPE_NAME from MSG_DESTINATIONSPEC) DS on RT.DESTINATION = DS.NAME ");
        builder.append(" inner join ");
        builder.append("(select * from ");
        builder.append("(select x.*, ROWNUM rnum from ");
        builder.append("(with busy as (select TSK.ID as \"TSKID\", sign(nvl(OCC.STATUS,1)) AS \"TSKSTATUS\", OCC.STARTDATE   from ( ");
        builder.append("(select * from TSK_RECURRENT_TASK WHERE NEXTEXECUTION IS NOT NULL) TSK ");
        builder.append("LEFT JOIN ");
        builder.append("(select * from TSK_TASK_OCCURRENCE where TSK_TASK_OCCURRENCE.ID in ");
        builder.append("(select max(OCC.ID) \"OCCID\" from TSK_TASK_OCCURRENCE OCC group by OCC.RECURRENTTASKID)) OCC ");
        builder.append("on TSK.ID=OCC.RECURRENTTASKID ");
        builder.append(") where OCC.STATUS=0), ");
        builder.append("notbusy as (select TSK.ID as \"TSKID\", sign(nvl(OCC.STATUS,1)) AS \"TSKSTATUS\", TSK.NEXTEXECUTION  from ( ");
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

        boolean isFirstCondition = true;
        //add started between conditions
        if ((filter.startedOnFrom != null) || (filter.startedOnTo != null)) {
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
            isFirstCondition = false;
        }

        //add next execution between conditions
        if ((filter.nextExecutionFrom != null) || (filter.nextExecutionTo != null)) {
            builder.append(isFirstCondition ? " where ( " : " and ( ");
            isFirstCondition = false;
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
        if ((filter.priority != null) && (filter.priority.operator != null)) {
            builder.append(isFirstCondition ? " where ( " : " and ( ");
            isFirstCondition = false;
            switch (filter.priority.operator) {
                case EQUAL:
                    builder.append(" PRIORITY = ");
                    builder.addLong(filter.priority.lowerBound);
                    break;
                case LESS_THAN:
                    builder.append(" PRIORITY < ");
                    builder.addLong(filter.priority.upperBound);
                    break;
                case GREATER_THAN:
                    builder.append(" PRIORITY > ");
                    builder.addLong(filter.priority.lowerBound);
                    break;
                case BETWEEN:
                    // range should be open for consistency of the FE component behavior
                    builder.append(" PRIORITY BETWEEN ");
                    builder.addLong(filter.priority.lowerBound + 1);
                    builder.append(" AND ");
                    builder.addLong(filter.priority.upperBound - 1);
                    break;
            }
            builder.append(") ");
        }

        //add queues filter conditions
        if ((filter.queues != null) && (!filter.queues.isEmpty())) {
            builder.append(isFirstCondition ? " where ( " : " and ( ");
            isFirstCondition = false;

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
        if ((filter.queueTypes != null) && (!filter.queueTypes.isEmpty())) {
            builder.append(isFirstCondition ? " where ( " : " and ( ");
            isFirstCondition = false;

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
        if ((filter.applications != null) && (!filter.applications.isEmpty())) {
            builder.append(isFirstCondition ? " where ( " : " and ( ");
            isFirstCondition = false;

            List<String> applications = new ArrayList();
            applications.addAll(filter.applications);
            builder.append("APPLICATION in (");
            for (int i = 0; i < applications.size(); i++) {
                builder.addObject(applications.get(i));
                builder.append((i < applications.size() - 1) ? " , " : "");
            }
            builder.append(")) ");
        }

        if ((filter.suspended != null) && (!filter.suspended.isEmpty())) {

            builder.append(isFirstCondition ? " where ( " : " and ( ");

            List<String> suspended = new ArrayList();
            suspended.addAll(filter.suspended);
            for (int i = 0; i < suspended.size(); i++) {
                if ("y".equalsIgnoreCase(suspended.get(i))) {
                    builder.append("(SUSPENDUNTIL IS NOT NULL)");
                } else if ("n".equalsIgnoreCase(suspended.get(i))) {
                    builder.append("(SUSPENDUNTIL IS NULL)");
                }

                if (i < suspended.size() - 1) {
                    builder.append(" or ");
                }
            }
            builder.append(") ");
        }

        // add sorting conditions
        builder.append("order by ");
        if (filter.sortingColumns!=null && !filter.sortingColumns.isEmpty()) {
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
        if (start != null && limit != null) {
            builder.append(" where rnum <=  ");
            builder.addInt(start + limit + 1);
            builder.append(" and rnum >= ");
            builder.addInt(start + 1);
        }
        return builder;
    }

    private List<RecurrentTask> getRecurrentTasks(Fetcher<RecurrentTaskImpl> fetcher) throws SQLException {
        List<RecurrentTask> result = new ArrayList<>();
        for (RecurrentTaskImpl task : fetcher) {
            result.add(task);
        }
        return result;
    }
}

