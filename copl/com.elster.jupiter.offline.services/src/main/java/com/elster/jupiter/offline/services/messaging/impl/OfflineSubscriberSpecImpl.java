/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.offline.services.messaging.impl;

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
import com.elster.jupiter.util.conditions.Condition;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.function.Predicate;
import java.util.logging.Logger;

/**
 * SubscriberSpec implementation.
 */
public class OfflineSubscriberSpecImpl implements SubscriberSpec {

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
    private Condition filterCondition;

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

    private final Collection<Connection> cancellableConnections = Collections.synchronizedSet(new HashSet<Connection>());

    private final DataModel dataModel;
    private final NlsService nlsService;
    private static final Logger LOGGER = Logger.getLogger(SubscriberSpec.class.getName());

    @Inject
    OfflineSubscriberSpecImpl(DataModel dataModel, NlsService nlsService) {
        this.dataModel = dataModel;
        this.nlsService = nlsService;
    }

    static OfflineSubscriberSpecImpl from(DataModel dataModel, DestinationSpec destinationSpec, String nameKey, String component, Layer layer, String filter, boolean systemManaged) {
        return dataModel.getInstance(OfflineSubscriberSpecImpl.class).init(destinationSpec, nameKey, component, layer, systemManaged, filter);
    }

    OfflineSubscriberSpecImpl init(DestinationSpec destination, String nameKey, String component, Layer layer, boolean systemManaged, Condition filter) {
        this.filterCondition = filter;
        init(destination, nameKey, component, layer, systemManaged, toString(filter));
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

    static OfflineSubscriberSpecImpl from(DataModel dataModel, DestinationSpec destinationSpec, String nameKey, String component, Layer layer) {
        return from(dataModel, destinationSpec, nameKey, component, layer, false, null);
    }

    static OfflineSubscriberSpecImpl from(DataModel dataModel, DestinationSpec destinationSpec, String nameKey, String component, Layer layer, boolean systemManaged, Condition filter) {
        return dataModel.getInstance(OfflineSubscriberSpecImpl.class).init(destinationSpec, nameKey, component, layer, systemManaged, filter);
    }

    OfflineSubscriberSpecImpl init(DestinationSpec destination, String nameKey, String component, Layer layer, boolean systemManaged, String filter) {
        this.destination.set(destination);
        this.name = nameKey;
        this.nlsComponent = component;
        this.nlsLayer = layer;
        this.systemManaged = systemManaged;
        this.filter = filter;
        return this;
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
        return null;
    }

    @Override
    public Message receive(Predicate<Message> validationFunction) {
        return null;
    }

    @Override
    public boolean isSystemManaged() {
        return systemManaged;
    }

    private Connection getConnection() throws SQLException {
        return dataModel.getConnection(false);
    }

    @Override
    public void cancel() {
        synchronized (cancellableConnections) {
            for (Connection cancellableConnection : new ArrayList<>(cancellableConnections)) {
                try {
                    cancellableConnection.close();
                    cancellableConnections.remove(cancellableConnection);
                } catch (SQLException e) {
                    throw new UnderlyingSQLFailedException(e);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "OfflineSubscriberSpecImpl{" +
                "name='" + name + '\'' +
                ", destinationName='" + destination.get().getName() + '\'' +
                '}';
    }

    void subscribe() {
        if (getDestination().isQueue()) {
            return;
        }
    }

    public void unSubscribe() {
        if (getDestination().isQueue()) {
            return;
        }
    }

    @Override
    public Layer getNlsLayer() {
        return nlsLayer;
    }

    @Override
    public String getNlsComponent() {
        return nlsComponent;
    }

    @Override
    public Condition getFilterCondition() {
        return filterCondition;
    }

    @Override
    public String getFilter() {
        return filter;
    }
}

