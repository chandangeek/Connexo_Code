package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.tasks.RecurrentTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class DueTaskFetcher {

    Iterable<RecurrentTask> dueTasks() {
        try(Connection connection = Bus.getOrmClient().getConnection()) {
            return dueTasks(connection);
        } catch (SQLException e) {
        	e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private Iterable<RecurrentTask> dueTasks(Connection connection) throws SQLException {
        Date now = Bus.getClock().now();
        List<RecurrentTask> result = new ArrayList<>();
        try(PreparedStatement statement = connection.prepareStatement("select id, name, cronstring, nextexecution, payload, destination from TSK_RECURRENT_TASK where nextExecution < ?")) {
            statement.setLong(1, now.getTime());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                long id = resultSet.getLong(1);
                String name = resultSet.getString(2);
                String cronString = resultSet.getString(3);
                long nextExecutionLong = resultSet.getLong(4);
                Date nextExecution = resultSet.wasNull() ? null : new Date(nextExecutionLong);
                String payload = resultSet.getString(5);
                String destination = resultSet.getString(6);
                DestinationSpec destinationSpec = Bus.getMessageService().getDestinationSpec(destination).get();
                RecurrentTaskImpl recurrentTask = new RecurrentTaskImpl(name, Bus.getCronExpressionParser().parse(cronString), destinationSpec, payload);
                recurrentTask.setNextExecution(nextExecution);
                recurrentTask.setId(id);
                result.add(recurrentTask);
            }
        }
        return result;
    }

}
