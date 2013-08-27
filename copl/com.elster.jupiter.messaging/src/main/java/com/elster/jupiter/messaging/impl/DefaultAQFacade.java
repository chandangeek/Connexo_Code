package com.elster.jupiter.messaging.impl;

import oracle.jdbc.aq.AQFactory;
import oracle.jdbc.aq.AQMessage;
import oracle.jdbc.aq.AQMessageProperties;
import oracle.jms.AQjmsQueueConnectionFactory;

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import java.sql.Connection;
import java.sql.SQLException;

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
    public QueueConnection createQueueConnection(Connection oracleConnection) throws JMSException {
        return AQjmsQueueConnectionFactory.createQueueConnection(oracleConnection);
    }
}
