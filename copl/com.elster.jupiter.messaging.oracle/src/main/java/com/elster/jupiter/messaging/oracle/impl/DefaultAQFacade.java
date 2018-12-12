/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.oracle.impl;

import com.elster.jupiter.messaging.QueueTableSpec;

import oracle.jdbc.aq.AQFactory;
import oracle.jdbc.aq.AQMessage;
import oracle.jdbc.aq.AQMessageProperties;
import oracle.jms.AQjmsQueueConnectionFactory;

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Default implementation that simply hides the static calls to AQFactory and AQjmsQueueConnectionFactory behind the AQFacade interface.
 */
public class DefaultAQFacade implements AQFacade {

    @Override
    public AQMessage create(AQMessageProperties properties) throws SQLException {
        return AQFactory.createAQMessage(properties);
    }

    @Override
    public AQMessageProperties createAQMessageProperties() throws SQLException {
        return AQFactory.createAQMessageProperties();
    }

    @Override
    public QueueConnection createQueueConnection(Connection connection) throws JMSException {
        return AQjmsQueueConnectionFactory.createQueueConnection(connection);
    }

    @Override
    public void activateAq(Connection connection, QueueTableSpec queueTableSpec) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(createSql(queueTableSpec))) {
            statement.setString(1, queueTableSpec.getName());
            statement.setString(2, queueTableSpec.getPayloadType());
            statement.execute();
        }
    }

    private String createSql(QueueTableSpec queueTableSpec) {
        return
                "begin dbms_aqadm.create_queue_table(queue_table => ?, queue_payload_type => ? , multiple_consumers => " +
                        (queueTableSpec.isMultiConsumer() ? "TRUE" : "FALSE") +
                        "); end;";
    }


}
