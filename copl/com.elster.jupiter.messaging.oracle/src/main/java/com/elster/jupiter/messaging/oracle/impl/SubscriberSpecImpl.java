/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.oracle.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.conditions.Condition;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.aq.AQDequeueOptions;
import oracle.jdbc.aq.AQMessage;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * SubscriberSpec implementation.
 */
public class SubscriberSpecImpl implements SubscriberSpec {

    /**
     * Receive will wait this long before checking whether to continue or not.
     */
    private static final Duration DEFAULT_WAIT = Duration.ofSeconds(60);
    private String name;
    @Size(max = 3)
    private String nlsComponent;
    @Size(max = 10)
    private Layer nlsLayer;
    @Size(max = 4000)
    private String filter;

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private boolean systemManaged;

    private final Reference<DestinationSpec> destination = ValueReference.absent();

    private final Collection<OracleConnection> cancellableConnections = Collections.synchronizedSet(new HashSet<OracleConnection>());

    private final DataModel dataModel;
    private final NlsService nlsService;

    @Inject
    SubscriberSpecImpl(DataModel dataModel, NlsService nlsService) {
        this.dataModel = dataModel;
        this.nlsService = nlsService;
    }

    SubscriberSpecImpl init(DestinationSpec destination, String nameKey, String component, Layer layer, boolean systemManaged, Condition filter) {
        this.destination.set(destination);
        this.name = nameKey;
        this.nlsComponent = component;
        this.nlsLayer = layer;
        this.systemManaged = systemManaged;
        this.filter = toString(filter);
        return this;
    }

    private String toString(Condition filter) {
        if (filter != null) {
            ConditionVisitor visitor = new ConditionVisitor();
            filter.visit(visitor);
            return visitor.toString();
        }
        return null;
    }

    static SubscriberSpecImpl from(DataModel dataModel, DestinationSpec destinationSpec, String nameKey, String component, Layer layer) {
        return from(dataModel, destinationSpec, nameKey, component, layer, false, null);
    }

    static SubscriberSpecImpl from(DataModel dataModel, DestinationSpec destinationSpec, String nameKey, String component, Layer layer, boolean systemManaged, Condition filter) {
        return dataModel.getInstance(SubscriberSpecImpl.class).init(destinationSpec, nameKey, component, layer, systemManaged, filter);
    }

    @Override
    public DestinationSpec getDestination() {
        return destination.get();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        if (!this.systemManaged) {
            return this.getThesaurus().getString(this.getName(), this.getName());
        } else {
            return this.getName();
        }
    }

    private Thesaurus getThesaurus() {
        return this.nlsService.getThesaurus(this.nlsComponent, this.nlsLayer);
    }

    @Override
    public Message receive() {
        try {
            return tryReceive();
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    @Override
    public boolean isSystemManaged() {
        return systemManaged;
    }

    private Message tryReceive() throws SQLException {
        OracleConnection cancellableConnection = null;
        try (Connection connection = getConnection()) {
            cancellableConnection = connection.unwrap(OracleConnection.class);
            cancellableConnections.add(cancellableConnection);
            AQMessage aqMessage = null;
            try {
                while (aqMessage == null) {
                    aqMessage = dequeueMessage(cancellableConnection);
                }
                return new MessageImpl(aqMessage);
            } catch (SQLTimeoutException e) {
                /*
                      The connection has been canceled.
                      We'll ignore this exception, since we have a way to recover from it (i.e. stop waiting as requested).
                 */
            }
        } finally {
            if (cancellableConnection != null) {
                cancellableConnections.remove(cancellableConnection);
            }
        }
        return null;
    }

    private Connection getConnection() throws SQLException {
        return dataModel.getConnection(false);
    }

    private AQMessage dequeueMessage(OracleConnection cancellableConnection) throws SQLException {
        return cancellableConnection.dequeue(destination.get().getName(), basicOptions(), getDestination().getPayloadType());
    }

    @Override
    public void cancel() {
        synchronized (cancellableConnections) {
            for (OracleConnection cancellableConnection : new ArrayList<>(cancellableConnections)) {
                try {
                    cancellableConnection.cancel();
                    cancellableConnections.remove(cancellableConnection);
                } catch (SQLException e) {
                    throw new UnderlyingSQLFailedException(e);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "SubscriberSpecImpl{" +
                "name='" + name + '\'' +
                ", destinationName='" + destination.get().getName() + '\'' +
                '}';
    }

    private AQDequeueOptions basicOptions() throws SQLException {
        AQDequeueOptions options = new AQDequeueOptions();
        options.setWait((int) DEFAULT_WAIT.getSeconds());
        if (getDestination().isTopic()) {
            options.setConsumerName(name);
            options.setNavigation(AQDequeueOptions.NavigationOption.FIRST_MESSAGE);
        }
        if (getDestination().isBuffered()) {
            options.setVisibility(AQDequeueOptions.VisibilityOption.IMMEDIATE);
            options.setDeliveryFilter(AQDequeueOptions.DeliveryFilter.BUFFERED);
        }
        return options;
    }

    /**
     * @return the message if one is immediately available, null otherwise
     */
    AQMessage receiveNow() {
        try (Connection connection = getConnection()) {
            OracleConnection oraConnection = connection.unwrap(OracleConnection.class);
            return oraConnection.dequeue(destination.get().getName(), optionsNoWait(), getDestination().getPayloadType());
        } catch (SQLTimeoutException e) {
            return null;
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private AQDequeueOptions optionsNoWait() throws SQLException {
        AQDequeueOptions options = basicOptions();
        options.setWait(0);
        return options;
    }

    void subscribe() {
        if (getDestination().isQueue()) {
            return;
        }
        try {
            doSubscribe();
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    void doSubscribe() throws SQLException {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(subscribeSql())) {
                statement.setString(1, name);
                statement.setString(2, destination.get().getName());
                statement.setString(3, Checks.is(filter).emptyOrOnlyWhiteSpace() ? null : filter);
                statement.execute();
            }
        }
    }

    public void unSubscribe() {
        try {
            doUnSubscribe(name);
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    void doUnSubscribe(String subscriberName) throws SQLException {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(unSubscribeSql())) {
                statement.setString(1, subscriberName);
                statement.setString(2, destination.get().getName());
                statement.execute();
            }
        }
    }

    private String subscribeSql() {
        return
                "declare subscriber sys.aq$_agent;  " +
                        "begin subscriber := sys.aq$_agent(?,null,null); " +
                        "dbms_aqadm.add_subscriber(?,subscriber, ?); end;";


    }

    private String unSubscribeSql() {
        return
                "declare subscriber sys.aq$_agent;  " +
                        "begin subscriber := sys.aq$_agent(?,null,null); " +
                        "dbms_aqadm.remove_subscriber(?,subscriber); end;";
    }

}

