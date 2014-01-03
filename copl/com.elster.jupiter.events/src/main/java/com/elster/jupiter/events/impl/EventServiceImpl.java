package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.NoSuchTopicException;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Clock;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.event.EventAdmin;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Component(name = "com.elster.jupiter.events", service = {InstallService.class, EventService.class}, property = "name=" + "EVT", immediate=true)
public class EventServiceImpl implements EventService, InstallService {

    private volatile Clock clock;
    private final AtomicReference<EventAdmin> eventAdmin = new AtomicReference<>();
    private volatile Publisher publisher;
    private volatile BeanService beanService;
    private volatile MessageService messageService;
    private volatile JsonService jsonService;
    private volatile DataModel dataModel;
    private final EventConfiguration eventConfiguration = new DefaultEventConfiguration();

    private LocalEventDispatcher localEventDispatcher = new LocalEventDispatcher();

    public EventServiceImpl() {
    }

    @Inject
    public EventServiceImpl(Clock clock, JsonService jsonService, Publisher publisher, BeanService beanService, OrmService ormService, MessageService messageService, BundleContext bundleContext, EventAdmin eventAdmin) {
        setClock(clock);
        setEventAdmin(eventAdmin);
        setPublisher(publisher);
        setBeanService(beanService);
        setJsonService(jsonService);
        setMessageService(messageService);
        setOrmService(ormService);
        activate(bundleContext);
        install();
    }

    @Override
    public void install() {
        new InstallerImpl(dataModel, messageService).install();
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel("EVT", "Events");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Reference(cardinality=ReferenceCardinality.OPTIONAL,policy=ReferencePolicy.DYNAMIC)
    public void setEventAdmin(EventAdmin eventAdmin) {
        this.eventAdmin.set(eventAdmin);
    }
    
    public void unsetEventAdmin(EventAdmin eventAdmin) {
    	this.eventAdmin.compareAndSet(eventAdmin, null);
    }

    @Reference
    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    @Reference
    public void setBeanService(BeanService beanService) {
        this.beanService = beanService;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addTopicHandler(TopicHandler topicHandler) {
        localEventDispatcher.addSubscription(topicHandler);
    }

    public void removeTopicHandler(TopicHandler topicHandler) {
        localEventDispatcher.removeSubscription(topicHandler);
    }

    @Activate
    public void activate(BundleContext context) {
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
            throw new NoSuchTopicException(topic);
        }
        EventType eventType = found.get();
        LocalEvent localEvent = eventType.create(source);
        publisher.publish(localEvent); // synchronous call, may throw an exception to prevent transaction commit should be prior to further propagating the event.
        EventAdmin eventAdmin = this.eventAdmin.get();
        if (eventAdmin != null) {
        	eventAdmin.postEvent(localEvent.toOsgiEvent());
        }
        if (eventType.shouldPublish()) {
            localEvent.publish();
        }
    }

    @Override
    public EventTypeBuilder buildEventTypeWithTopic(String topic) {
        return new EventTypeBuilderImpl(dataModel, clock, jsonService, eventConfiguration, messageService, beanService, topic);
    }

    @Override
    public List<EventType> getEventTypes() {
        return eventTypeFactory().find();
    }
	
	@Override
    public Optional<EventType> getEventType(String topic) {
        return eventTypeFactory().getOptional(topic);
    }

    private DataMapper<EventType> eventTypeFactory() {
        return dataModel.mapper(EventType.class);
    }


}
