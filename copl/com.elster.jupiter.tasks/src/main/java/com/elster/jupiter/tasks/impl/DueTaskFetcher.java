package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.elster.jupiter.util.time.ScheduleExpressionParser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
        try (PreparedStatement statement = connection.prepareStatement("select id, name, cronstring, nextexecution, payload, destination from TSK_RECURRENT_TASK where nextExecution < ?")) {
            statement.setLong(1, now.toEpochMilli());
            try (ResultSet resultSet = statement.executeQuery()) {
                return getRecurrentTasks(resultSet);
            }
        }
    }

    private List<RecurrentTaskImpl> getRecurrentTasks(ResultSet resultSet) throws SQLException {
        List<RecurrentTaskImpl> result = new ArrayList<>();
        while (resultSet.next()) {
            result.add(getRecurrentTask(resultSet));
        }
        return result;
    }

    private RecurrentTaskImpl getRecurrentTask(ResultSet resultSet) throws SQLException {
        long id = resultSet.getLong(ID_INDEX);
        String name = resultSet.getString(NAME_INDEX);
        String cronString = resultSet.getString(CRON_INDEX);
        long nextExecutionLong = resultSet.getLong(NEXT_EXECUTION_INDEX);
        Instant nextExecution = resultSet.wasNull() ? null : Instant.ofEpochMilli(nextExecutionLong);
        String payload = resultSet.getString(PAYLOAD_INDEX);
        String destination = resultSet.getString(DESTINATION_INDEX);
        DestinationSpec destinationSpec = messageService.getDestinationSpec(destination).get();
        ScheduleExpression scheduleExpression = scheduleExpressionParser.parse(cronString).orElseThrow(IllegalArgumentException::new);
        RecurrentTaskImpl recurrentTask = RecurrentTaskImpl.from(dataModel, name, scheduleExpression, destinationSpec, payload);
        recurrentTask.setNextExecution(nextExecution);
        recurrentTask.setId(id);
        return recurrentTask;
    }

}
