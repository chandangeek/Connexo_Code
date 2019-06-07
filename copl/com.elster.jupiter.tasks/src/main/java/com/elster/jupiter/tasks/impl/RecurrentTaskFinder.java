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
        builder.append("select * from (select ID, APPLICATION, NAME, CRONSTRING, NEXTEXECUTION, PAYLOAD, DESTINATION, LASTRUN, VERSIONCOUNT, CREATETIME, MODTIME, USERNAME, LOGLEVEL, QUEUE_TYPE_NAME, ROWNUM as rnum from (");
        builder.append("select RT.ID, RT.APPLICATION, RT.NAME, RT.CRONSTRING, RT.NEXTEXECUTION, RT.PAYLOAD, RT.DESTINATION, RT.LASTRUN, RT.VERSIONCOUNT, RT.CREATETIME, RT.MODTIME, RT.USERNAME, RT.LOGLEVEL, DS.QUEUE_TYPE_NAME, ROWNUM as rnum ");
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

        //add started bewteen conditions
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
        }

        //add queues filter conditions
        if ((filter.queues != null) && (!filter.queues.isEmpty())) {
            if ((filter.startedOnFrom == null) && (filter.startedOnTo == null)) {
                builder.append(" where ( ");
            } else {
                builder.append(" and ( ");
            }
            List<String> queues = new ArrayList();
            queues.addAll(filter.queues);
            for (int i = 0; i < queues.size(); i++) {
                builder.append("DESTINATION= ");
                builder.addObject(queues.get(i));
                if (i < queues.size() - 1) {
                    builder.append(" or ");
                }
            }
            builder.append(") ");
        }

        //add types filter conditions
        if ((filter.types != null) && (!filter.types.isEmpty())) {
            if ((filter.startedOnFrom == null) && (filter.startedOnTo == null)
                    && ((filter.queues == null) || filter.queues.isEmpty())) {
                builder.append(" where ( ");
            } else {
                builder.append(" and ( ");
            }
            List<String> types = new ArrayList();
            types.addAll(filter.types);
            for (int i = 0; i < types.size(); i++) {
                builder.append("QUEUE_TYPE_NAME= ");
                builder.addObject(types.get(i));
                if (i < types.size() - 1) {
                    builder.append(" or ");
                }
            }
            builder.append(") ");
        }

        //add application filter conditions
        if ((filter.applications != null) && (!filter.applications.isEmpty())) {
            if ((filter.startedOnFrom == null) && (filter.startedOnTo == null)
                    && ((filter.queues == null) || filter.queues.isEmpty())
                    && ((filter.types == null) || filter.types.isEmpty())) {
                builder.append(" where ( ");
            } else {
                builder.append(" and ( ");
            }
            List<String> applications = new ArrayList();
            applications.addAll(filter.applications);
            for (int i = 0; i < applications.size(); i++) {
                builder.append("APPLICATION= ");
                builder.addObject(applications.get(i));
                if (i < applications.size() - 1) {
                    builder.append(" or ");
                }
            }
            builder.append(") ");
        }

        builder.append("order by TSKSTATUS, STARTDATE ");
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

