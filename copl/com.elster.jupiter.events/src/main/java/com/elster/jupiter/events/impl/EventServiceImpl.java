package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.MessageSeeds;
import com.elster.jupiter.events.NoSuchTopicException;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.json.JsonService;
import com.google.inject.AbstractModule;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component(
        name = "com.elster.jupiter.events",
        service = {InstallService.class, EventService.class, TranslationKeyProvider.class},
        property = "name=" + EventService.COMPONENTNAME,
        immediate = true)
public class EventServiceImpl implements EventService, InstallService, TranslationKeyProvider {

    private volatile Clock clock;
    private volatile Publisher publisher;
    private volatile BeanService beanService;
    private volatile MessageService messageService;
    private volatile JsonService jsonService;
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private final EventConfiguration eventConfiguration = new DefaultEventConfiguration();

    private LocalEventDispatcher localEventDispatcher = new LocalEventDispatcher();

    public EventServiceImpl() {
    }

    @Inject
    public EventServiceImpl(Clock clock, JsonService jsonService, Publisher publisher, BeanService beanService, OrmService ormService, MessageService messageService, BundleContext bundleContext, NlsService nlsService) {
        setClock(clock);
        setPublisher(publisher);
        setBeanService(beanService);
        setJsonService(jsonService);
        setMessageService(messageService);
        setOrmService(ormService);
        setNlsService(nlsService);
        activate(bundleContext);
        if (!dataModel.isInstalled()) {
            install();
        }
        publisher.addSubscriber(this.localEventDispatcher);
    }

    @Override
    public final void install() {
        new InstallerImpl(dataModel, messageService).install();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "MSG", "NLS");
    }

    @Reference
    public final void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public final void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel("EVT", "Events");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Reference
    public final void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    @Reference
    public final void setBeanService(BeanService beanService) {
        this.beanService = beanService;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    @Reference
    public final void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public final void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public final void addTopicHandler(TopicHandler topicHandler) {
        localEventDispatcher.addSubscription(topicHandler);
    }

    public void removeTopicHandler(TopicHandler topicHandler) {
        localEventDispatcher.removeSubscription(topicHandler);
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(EventService.COMPONENTNAME, Layer.DOMAIN);
    }

    @Activate
    public final void activate(BundleContext context) {
        localEventDispatcher.register(context);
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Clock.class).toInstance(clock);
                bind(Publisher.class).toInstance(publisher);
                bind(BeanService.class).toInstance(beanService);
                bind(MessageService.class).toInstance(messageService);
                bind(JsonService.class).toInstance(jsonService);
                bind(EventConfiguration.class).toInstance(eventConfiguration);
                bind(DataModel.class).toInstance(dataModel);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
            }
        });
    }

    @Deactivate
    public void deactivate() {
    }

    @Override
    public void postEvent(String topic, Object source) {
        Optional<EventType> found = dataModel.mapper(EventType.class).getOptional(topic);
        if (!found.isPresent()) {
            throw new NoSuchTopicException(thesaurus, topic);
        }
        EventType eventType = found.get();
        LocalEvent localEvent = eventType.create(source);
        publisher.publish(localEvent); // synchronous call, may throw an exception to prevent transaction commit should be prior to further propagating the event.
        if (eventType.shouldPublish()) {
            localEvent.publish();
        }
    }

    @Override
    public EventTypeBuilder buildEventTypeWithTopic(String topic) {
        return new EventTypeBuilderImpl(dataModel, topic);
    }

    @Override
    public List<EventType> getEventTypes() {
        return eventTypeFactory().find();
    }

    @Override
    public List<EventType> getEventTypesForComponent(String component) {
        return eventTypeFactory().find("component", component);
    }

    @Override
    public Optional<EventType> getEventType(String topic) {
        return eventTypeFactory().getOptional(topic);
    }

    private DataMapper<EventType> eventTypeFactory() {
        return dataModel.mapper(EventType.class);
    }


    @Override
    public String getComponentName() {
        return EventService.COMPONENTNAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(MessageSeeds.values());
    }
}
