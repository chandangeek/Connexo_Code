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
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.pubsub.Publisher;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Osgi Component class.
 */
@Component(name = "com.elster.jupiter.messaging", service = {MessageService.class, InstallService.class},
        property = {"name=" + MessageService.COMPONENTNAME})
public class MessageServiceImpl implements MessageService, InstallService {

    private final DefaultAQFacade defaultAQMessageFactory = new DefaultAQFacade();
    private volatile Publisher publisher;
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;

    public MessageServiceImpl() {
    }

    @Inject
    MessageServiceImpl(OrmService ormService, Publisher publisher, NlsService nlsService) {
        setOrmService(ormService);
        setPublisher(publisher);
        setNlsService(nlsService);
        activate();
        dataModel.install(true, true);
    }

    @Reference
    public final void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(MessageService.COMPONENTNAME, "Jupiter Messaging");
        for (TableSpecs each : TableSpecs.values()) {
            each.addTo(dataModel);
        }
    }

    @Activate
    public final void activate() {
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(AQFacade.class).toInstance(defaultAQMessageFactory);
                bind(Publisher.class).toInstance(publisher);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
            }
        });
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
    public void install() {
        new InstallerImpl(dataModel).install(this);
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM");
    }

    @Override
    public Optional<QueueTableSpec> getQueueTableSpec(String name) {
        // check if dataModel is already installed because this method get potentially called before the initAll is run
        return dataModel.isInstalled() ? dataModel.mapper(QueueTableSpec.class).getOptional(name) : Optional.<QueueTableSpec>empty();
    }

    @Override
    public Optional<DestinationSpec> getDestinationSpec(String name) {
        // check if dataModel is already installed because this method get potentially called before the initAll is run
        return dataModel.isInstalled() ? dataModel.mapper(DestinationSpec.class).getOptional(name) : Optional.<DestinationSpec>empty();
    }

    @Override
    public Optional<SubscriberSpec> getSubscriberSpec(String destinationSpecName, String name) {
        // check if dataModel is already installed because this method get potentially called before the initAll is run
        return dataModel.isInstalled() ? dataModel.mapper(SubscriberSpec.class).getOptional(destinationSpecName, name) : Optional.<SubscriberSpec>empty();
    }

    @Override
     public List<SubscriberSpec> getSubscribers() {
        return dataModel.mapper(SubscriberSpec.class).find();
    }

    @Override
    public List<SubscriberSpec> getNonSystemManagedSubscribers() {
        return dataModel.mapper(SubscriberSpec.class).find("systemManaged", false);
    }

    @Reference
    public final void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(MessageService.COMPONENTNAME, Layer.DOMAIN);
    }

    DataModel getDataModel() {
        return dataModel;
    }
}
