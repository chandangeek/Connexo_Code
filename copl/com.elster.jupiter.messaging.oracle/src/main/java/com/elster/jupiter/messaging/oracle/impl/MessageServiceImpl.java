/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.oracle.impl;

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
import com.elster.jupiter.upgrade.UpgradeService;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.orm.Version.version;
import static com.elster.jupiter.upgrade.InstallIdentifier.identifier;

/**
 * Osgi Component class.
 */
@Component(name = "com.elster.jupiter.messaging", service = {MessageService.class},
        property = {"name=" + MessageService.COMPONENTNAME})
public class MessageServiceImpl implements MessageService {

    private volatile AQFacade defaultAQMessageFactory;
    private volatile Publisher publisher;
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile NlsService nlsService;
    private volatile UpgradeService upgradeService;

    public MessageServiceImpl() {
    }

    @Inject
    MessageServiceImpl(OrmService ormService, Publisher publisher, NlsService nlsService, UpgradeService upgradeService, AQFacade aqFacade) {
        setOrmService(ormService);
        setPublisher(publisher);
        setNlsService(nlsService);
        setUpgradeService(upgradeService);
        this.defaultAQMessageFactory = aqFacade;
        activate();
    }

    @Reference
    public final void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(MessageService.COMPONENTNAME, "Jupiter Messaging");
        for (TableSpecs each : TableSpecs.values()) {
            each.addTo(dataModel);
        }
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Activate
    public final void activate() {
        if (defaultAQMessageFactory == null) {
            defaultAQMessageFactory = new DefaultAQFacade();
        }
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(AQFacade.class).toInstance(defaultAQMessageFactory);
                bind(Publisher.class).toInstance(publisher);
                bind(NlsService.class).toInstance(nlsService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(MessageService.class).toInstance(MessageServiceImpl.this);
            }
        });
        upgradeService.register(identifier("Pulse", COMPONENTNAME), dataModel, InstallerImpl.class, ImmutableMap.of(
                version(10, 2), UpgraderV10_2.class
        ));
    }

    @Deactivate
    public final void deactivate() {
    }

    @Override
    public QueueTableSpec createQueueTableSpec(String name, String payloadType, boolean multiConsumer) {
        QueueTableSpecImpl result = QueueTableSpecImpl.from(dataModel, name, payloadType, multiConsumer);
        result.save();
        result.activate();
        return result;
    }

    @Override
    public Optional<QueueTableSpec> getQueueTableSpec(String name) {
        // check if dataModel is already installed because this method get potentially called before the initAll is run
        return dataModel.mapper(QueueTableSpec.class).getOptional(name);
    }

    @Override
    public Optional<DestinationSpec> getDestinationSpec(String name) {
        // check if dataModel is already installed because this method get potentially called before the initAll is run
        return dataModel.mapper(DestinationSpec.class).getOptional(name);
    }

    @Override
    public Optional<DestinationSpec> lockDestinationSpec(String name, long version) {
        return dataModel.mapper(DestinationSpec.class).lockObjectIfVersion(version, name);
    }

    @Override
    public Optional<SubscriberSpec> getSubscriberSpec(String destinationSpecName, String name) {
        // check if dataModel is already installed because this method get potentially called before the initAll is run
        return dataModel.mapper(SubscriberSpec.class).getOptional(destinationSpecName, name);
    }

    @Override
     public List<SubscriberSpec> getSubscribers() {
        return dataModel.mapper(SubscriberSpec.class).find();
    }

    @Override
    public List<SubscriberSpec> getNonSystemManagedSubscribers() {
        return dataModel.mapper(SubscriberSpec.class).find("systemManaged", false);
    }

    @Override
    public List<DestinationSpec> findDestinationSpecs() {
        return dataModel.mapper(DestinationSpec.class).find();
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

    DataModel getDataModel() {
        return dataModel;
    }

    void setAqFacade(AQFacade aqFacade) {
        this.defaultAQMessageFactory = aqFacade;
    }
}
