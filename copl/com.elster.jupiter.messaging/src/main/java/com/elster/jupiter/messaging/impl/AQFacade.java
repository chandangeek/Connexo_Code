package com.elster.jupiter.messaging.impl;

import oracle.jdbc.aq.AQMessage;
import oracle.jdbc.aq.AQMessageProperties;

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import java.sql.Connection;
import java.sql.SQLException;

interface AQFacade {

    AQMessage create(AQMessageProperties properties) throws SQLException;

    AQMessageProperties createAQMessageProperties() throws SQLException;

    QueueConnection createQueueConnection(Connection oracleConnection) throws JMSException;
}
