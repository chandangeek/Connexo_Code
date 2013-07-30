package com.elster.jupiter.messaging;


import java.sql.SQLException;

public interface SubscriberSpec {

    DestinationSpec getDestination();

    String getName();

    Message receive() throws SQLException;

    void cancel() throws SQLException;
}
