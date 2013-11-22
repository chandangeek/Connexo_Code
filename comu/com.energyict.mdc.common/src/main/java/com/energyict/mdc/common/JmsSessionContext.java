package com.energyict.mdc.common;

import oracle.jms.AQjmsQueueConnectionFactory;
import oracle.jms.AQjmsSession;

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.Session;
import java.sql.SQLException;

public class JmsSessionContext {

    private AQjmsSession session;
    private QueueConnection connection = null;

    public AQjmsSession getSession() throws BusinessException, SQLException {
        if (session == null) {
            try {
                java.sql.Connection dbConnection = Environment.DEFAULT.get().getUnwrappedConnection();
                connection = AQjmsQueueConnectionFactory.createQueueConnection(dbConnection);
                connection.start();
                try {
                    session = (AQjmsSession) connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
                } catch (NullPointerException ex) {
                    // when a user a has no sufficient rights, Oracle throws a NullPointerException. Sweet.
                    throw new ApplicationException("The dbuser has no rights to create a JMS session. Please contact your system administrator.", ex);
                }
                if (session == null) {
                    if (connection != null) {
                        try {
                            connection.close();
                        } catch (JMSException e) {
                            // ignore
                        } finally {
                            connection = null;
                        }
                    }
                    throw new DatabaseException(new SQLException("Could not create AQjmsSession"));
                }
            } catch (JMSException e) {
                throw new MessageException("jmsSessionContext.sessionCouldNotBeCreated","jmsSession could not be created",e);
            }
        }
        return session;
    }

    public void close() {
        try {
            try {
                if (connection != null) {
                    connection.close();
                }
            } finally {
                connection = null;
                try {
                    if (session != null) {
                        session.close();
                    }
                } finally {
                    session = null;
                }
            }
        } catch (JMSException e) {
            throw new ApplicationException(e);
        }
    }

}
