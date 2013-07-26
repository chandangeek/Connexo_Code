package com.elster.jupiter.messaging;

import oracle.jdbc.aq.AQMessage;

import java.sql.SQLException;

public interface SubscriberSpec {

    DestinationSpec getDestination();

    String getName();

    AQMessage receive() throws SQLException;

    void cancel() throws SQLException;
}
