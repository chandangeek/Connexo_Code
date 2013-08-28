package com.elster.jupiter.messaging.impl;

import oracle.jdbc.aq.AQMessage;
import oracle.jdbc.aq.AQMessageProperties;

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Facade for advanced queuing classes.
 */
interface AQFacade {

    /**
     * @param properties
     * @return a newly created AQMessage with the given AQMessageProperties
     * @throws SQLException
     */
    AQMessage create(AQMessageProperties properties) throws SQLException;

    /**
     * @return a newly created AQMessageProperties object.
     * @throws SQLException
     */
    AQMessageProperties createAQMessageProperties() throws SQLException;

    /**
     * @param connection
     * @return a QueueConnection based on the given connection.
     * @throws JMSException
     */
    QueueConnection createQueueConnection(Connection connection) throws JMSException;
}
