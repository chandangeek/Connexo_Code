/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.offline.services.messaging.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pubsub.Publisher;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Osgi Component class.
 */
@Component(name = "com.elster.jupiter.offline.messaging", service = {MessageService.class},
        property = {"name=" + MessageService.COMPONENTNAME})
public class OfflineMessageServiceImpl implements MessageService {

    private volatile Publisher publisher;
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile NlsService nlsService;

    public OfflineMessageServiceImpl() {
    }

    @Reference
    public final void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(MessageService.COMPONENTNAME, "Jupiter Messaging");
    }

    @Activate
    public final void activate() {
    }

    @Deactivate
    public final void deactivate() {
    }

    @Override
    public QueueTableSpec createQueueTableSpec(String name, String payloadType, boolean multiConsumer, boolean isPrioritized) {
        return null;
    }

    @Override
    public QueueTableSpec createQueueTableSpec(String name, String payloadType, boolean multiConsumer) {
        return null;
    }

    @Override
    public Optional<QueueTableSpec> getQueueTableSpec(String name) {
        return Optional.empty();
    }

    @Override
    public Optional<DestinationSpec> getDestinationSpec(String name) {
        return Optional.of(new DefaultDestinationSpecImpl(name));
    }

    public List<DestinationSpec> getDestinationSpecs(String queueTypeName) {
        return Collections.emptyList();
    }

    @Override
    public Optional<DestinationSpec> lockDestinationSpec(String name, long version) {
        return Optional.empty();
    }

    @Override
    public Optional<SubscriberSpec> getSubscriberSpec(String destinationSpecName, String name) {
        return Optional.empty();
    }

    @Override
     public List<SubscriberSpec> getSubscribers() {
        return Collections.emptyList();
    }

    @Override
    public List<SubscriberSpec> getNonSystemManagedSubscribers() {
        return Collections.emptyList();
    }

    @Override
    public List<DestinationSpec> findDestinationSpecs() {
        return Collections.emptyList();
    }

    @Reference
    public final void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(MessageService.COMPONENTNAME, Layer.DOMAIN);
    }
}
