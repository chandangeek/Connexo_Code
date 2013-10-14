package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.tasks.RecurrentTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class DueTaskFetcher {

    private static final int ID_INDEX = 1;
    private static final int NAME_INDEX = 2;
    private static final int CRON_INDEX = 3;
    private static final int NEXT_EXECUTION_INDEX = 4;
    private static final int PAYLOAD_INDEX = 5;
    private static final int DESTINATION_INDEX = 6;

    Iterable<RecurrentTask> dueTasks() {
        try (Connection connection = Bus.getOrmClient().getConnection()) {
            return dueTasks(connection);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private Iterable<RecurrentTask> dueTasks(Connection connection) throws SQLException {
        Date now = Bus.getClock().now();
        try (PreparedStatement statement = connection.prepareStatement("select id, name, cronstring, nextexecution, payload, destination from TSK_RECURRENT_TASK where nextExecution < ?")) {
            statement.setLong(1, now.getTime());
            try (ResultSet resultSet = statement.executeQuery()) {
                return getRecurrentTasks(resultSet);
            }
        }
    }

    private List<RecurrentTask> getRecurrentTasks(ResultSet resultSet) throws SQLException {
        List<RecurrentTask> result = new ArrayList<>();
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
        Date nextExecution = resultSet.wasNull() ? null : new Date(nextExecutionLong);
        String payload = resultSet.getString(PAYLOAD_INDEX);
        String destination = resultSet.getString(DESTINATION_INDEX);
        DestinationSpec destinationSpec = Bus.getMessageService().getDestinationSpec(destination).get();
        RecurrentTaskImpl recurrentTask = new RecurrentTaskImpl(name, Bus.getCronExpressionParser().parse(cronString), destinationSpec, payload);
        recurrentTask.setNextExecution(nextExecution);
        recurrentTask.setId(id);
        return recurrentTask;
    }

}
