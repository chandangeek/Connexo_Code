/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.oracle.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.messaging.*;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import oracle.AQ.AQException;
import oracle.AQ.AQQueueTable;
import oracle.AQ.AQQueueTableProperty;
import oracle.jdbc.OracleConnection;
import oracle.jms.AQjmsSession;

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.Session;
import javax.validation.constraints.Size;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;


public class QueueTableSpecImpl implements QueueTableSpec {

    // persistent fields
    @Size(max = 24, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.QUEUE_NAME_TOO_LONG + "}")
    private String name;
    private String payloadType;
    private boolean multiConsumer;
    private boolean active;
    private boolean isPrioritized;

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    private final DataModel dataModel;
    private final AQFacade aqFacade;
    private transient boolean fromDB = true;
    private final Thesaurus thesaurus;

    @Inject
    QueueTableSpecImpl(DataModel dataModel, AQFacade aqFacade, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.aqFacade = aqFacade;
        this.thesaurus = thesaurus;
    }

    QueueTableSpecImpl init(String name, String payloadType, boolean multiConsumer, boolean isPrioritized) {
        this.name = name;
        this.payloadType = payloadType;
        this.multiConsumer = multiConsumer;
        this.isPrioritized = isPrioritized;
        this.fromDB = false;
        return this;
    }

    static QueueTableSpecImpl from(DataModel dataModel, String name, String payloadType, boolean multiConsumer) {
        return dataModel.getInstance(QueueTableSpecImpl.class).init(name, payloadType, multiConsumer, false);
    }

    static QueueTableSpecImpl from(DataModel dataModel, String name, String payloadType, boolean multiConsumer, boolean isPrioritized) {
        return dataModel.getInstance(QueueTableSpecImpl.class).init(name, payloadType, multiConsumer, isPrioritized);
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
        aqFacade.activateAq(connection, this);
    }

    private void doActivateJms() {
        try (Connection connection = getConnection()) {
            tryActivateJms(connection);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        } catch (AQException e) {
            throw new UnderlyingAqException(thesaurus, e);
        } catch (JMSException e) {
            throw new UnderlyingJmsException(thesaurus, e);
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

    @Override
    public void delete() {
        if (isActive()) {
            deactivate();
        }
        dataModel.mapper(QueueTableSpec.class).remove(this);
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
    public boolean isPrioritized() {
        return isPrioritized;
    }

    @Override
    public String getPayloadType() {
        return payloadType;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    private DestinationSpec createDestinationSpec(String name, int retryDelay, int retries, boolean buffered, boolean isDefault, String queueTypeName, boolean isExtraQueueCreationEnabled, boolean isPrioritized) {
        DestinationSpecImpl spec = DestinationSpecImpl.from(dataModel, this, name, retryDelay, retries, buffered, isDefault, queueTypeName, isExtraQueueCreationEnabled, isPrioritized);
        spec.save();
        return spec;
    }

    @Override
    public DestinationSpec createDestinationSpec(String name, int retryDelay, int retries, boolean isDefault, String queueTypeName, boolean isExtraQueueCreationEnabled, boolean isPrioritized) {
        return createDestinationSpec(name, retryDelay, retries, false, isDefault, queueTypeName, isExtraQueueCreationEnabled, isPrioritized);
    }

    @Override
    public DestinationSpec createBufferedDestinationSpec(String name, int retryDelay, int retries, boolean isDefault, String queueTypeName, boolean isExtraQueueCreationEnabled) {
        return createDestinationSpec(name, retryDelay, retries, true, isDefault, queueTypeName, isExtraQueueCreationEnabled,false);
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
            Save.UPDATE.save(dataModel, this);
        } else {
            Save.CREATE.save(dataModel, this);
            fromDB = true;
        }
    }

}
