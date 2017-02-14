/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.sql.Fetcher;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.time.ScheduleExpressionParser;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

class DueTaskFetcher {

    private static final int ID_INDEX = 1;
    private static final int NAME_INDEX = 2;
    private static final int CRON_INDEX = 3;
    private static final int NEXT_EXECUTION_INDEX = 4;
    private static final int PAYLOAD_INDEX = 5;
    private static final int DESTINATION_INDEX = 6;

    private final DataModel dataModel;
    private final Clock clock;
    private final MessageService messageService;
    private final ScheduleExpressionParser scheduleExpressionParser;

    DueTaskFetcher(DataModel dataModel, MessageService messageService, ScheduleExpressionParser scheduleExpressionParser, Clock clock) {
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.scheduleExpressionParser = scheduleExpressionParser;
        this.clock = clock;
    }

    Iterable<RecurrentTaskImpl> dueTasks() {
        try (Connection connection = dataModel.getConnection(false)) {
            return dueTasks(connection);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private Iterable<RecurrentTaskImpl> dueTasks(Connection connection) throws SQLException {
        Instant now = clock.instant();
        DataMapper<RecurrentTaskImpl> mapper = dataModel.mapper(RecurrentTaskImpl.class);
        SqlBuilder builder = mapper.builder("a");
        builder.append(" where nextExecution < ");
        builder.addLong(now.toEpochMilli());
        builder.append(" for update skip locked");
        try(Fetcher<RecurrentTaskImpl> fetcher = mapper.fetcher(builder)) {
        	return getRecurrentTasks(fetcher);
        }
    }

    private List<RecurrentTaskImpl> getRecurrentTasks(Fetcher<RecurrentTaskImpl> fetcher) throws SQLException {
        List<RecurrentTaskImpl> result = new ArrayList<>();
        for (RecurrentTaskImpl task : fetcher) {
            result.add(task);
        }
        return result;
    }

}
