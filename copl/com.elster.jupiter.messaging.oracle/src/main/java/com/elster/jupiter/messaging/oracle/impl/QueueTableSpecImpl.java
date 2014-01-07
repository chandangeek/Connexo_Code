package com.elster.jupiter.messaging.oracle.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.UnderlyingAqException;
import com.elster.jupiter.messaging.UnderlyingJmsException;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.time.UtcInstant;
import oracle.AQ.AQException;
import oracle.AQ.AQQueueTable;
import oracle.AQ.AQQueueTableProperty;
import oracle.jdbc.OracleConnection;
import oracle.jms.AQjmsSession;

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.Session;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class QueueTableSpecImpl implements QueueTableSpec {

    // persistent fields
    private String name;
    private String payloadType;
    private boolean multiConsumer;
    private boolean active;

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private UtcInstant createTime;
    @SuppressWarnings("unused")
    private UtcInstant modTime;
    @SuppressWarnings("unused")
    private String userName;

    private final DataModel dataModel;
    private final AQFacade aqFacade;
    private transient boolean fromDB = true;

    @SuppressWarnings("unused")
    @Inject
    QueueTableSpecImpl(DataModel dataModel, AQFacade aqFacade) {
        this.dataModel = dataModel;
        this.aqFacade = aqFacade;
    }

    QueueTableSpecImpl init(String name, String payloadType, boolean multiConsumer) {
        this.name = name;
        this.payloadType = payloadType;
        this.multiConsumer = multiConsumer;
        this.fromDB = false;
        return this;
    }

    static QueueTableSpecImpl from(DataModel dataModel, String name, String payloadType, boolean multiConsumer) {
        return dataModel.getInstance(QueueTableSpecImpl.class).init(name, payloadType, multiConsumer);
    }

    @Override
    public void activate() {
        if (isActive()) {
            return;
        }
        if (isJms()) {
            doActivateJms();
        } else {
            doActivateAq();
        }
        active = true;
        dataModel.mapper(QueueTableSpec.class).update(this, "active");
    }

    private void doActivateAq() {
        try (Connection connection = getConnection()) {
            tryActivateAq(connection);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private Connection getConnection() throws SQLException {
        return dataModel.getConnection(false);
    }

    private void tryActivateAq(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(createSql())) {
            statement.setString(1, name);
            statement.setString(2, payloadType);
            statement.execute();
        }
    }

    private void doActivateJms() {
        try (Connection connection = getConnection()) {
            tryActivateJms(connection);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        } catch (AQException e) {
            throw new UnderlyingAqException(e);
        } catch (JMSException e) {
            throw new UnderlyingJmsException(e);
        }
    }

    private void tryActivateJms(Connection connection) throws SQLException, JMSException, AQException {
        if (!connection.isWrapperFor(OracleConnection.class)) {
            return;
        }
        OracleConnection oraConnection = connection.unwrap(OracleConnection.class);
        QueueConnection queueConnection = aqFacade.createQueueConnection(oraConnection);
        try {
            queueConnection.start();
            AQjmsSession session = (AQjmsSession) queueConnection.createSession(true, Session.AUTO_ACKNOWLEDGE);
            AQQueueTableProperty properties = new AQQueueTableProperty(payloadType);
            properties.setMultiConsumer(multiConsumer);
            session.createQueueTable("kore", name, properties);
        } finally {
            queueConnection.close();
        }
    }

    @Override
    public void deactivate() {
        if (!isActive()) {
            return;
        }
        try {
            doDeactivate();
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
        active = false;
        dataModel.mapper(QueueTableSpec.class).update(this, "active");
    }

    private String createSql() {
        return
                "begin dbms_aqadm.create_queue_table(queue_table => ?, queue_payload_type => ? , multiple_consumers => " +
                        (multiConsumer ? "TRUE" : "FALSE") +
                        "); end;";
    }

    private String dropSql() {
        return "begin dbms_aqadm.drop_queue_table(?); end;";


    }

    private void doDeactivate() throws SQLException {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(dropSql())) {
                statement.setString(1, name);
                statement.execute();
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isMultiConsumer() {
        return multiConsumer;
    }

    @Override
    public String getPayloadType() {
        return payloadType;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public DestinationSpec createDestinationSpec(String name, int retryDelay) {
        DestinationSpecImpl spec = DestinationSpecImpl.from(dataModel, this, name, retryDelay);
        spec.save();
        spec.activate();
        return spec;
    }

    @Override
    public boolean isJms() {
        return payloadType.toUpperCase().startsWith("SYS.AQ$_JMS_");
    }

    public AQQueueTable getAqQueueTable(AQjmsSession session) throws JMSException {
        return session.getQueueTable("kore", name);
    }

    @Override
    public void save() {
        if (fromDB) {
            dataModel.mapper(QueueTableSpec.class).update(this);
        } else {
            dataModel.mapper(QueueTableSpec.class).persist(this);
            fromDB = true;
        }
    }
}
