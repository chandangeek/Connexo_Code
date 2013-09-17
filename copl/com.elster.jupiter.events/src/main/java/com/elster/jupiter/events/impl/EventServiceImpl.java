package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.NoSuchTopicException;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.cache.CacheService;
import com.elster.jupiter.orm.cache.ComponentCache;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Clock;
import com.google.common.base.Optional;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.event.EventAdmin;

@Component(name = "com.elster.jupiter.events", service = {InstallService.class, EventService.class}, property = "name=" + Bus.COMPONENTNAME, immediate=true)
public class EventServiceImpl implements EventService, InstallService, ServiceLocator {

    private volatile ComponentCache componentCache;
    private volatile Clock clock;
    private volatile OrmClient ormClient;
    private volatile EventAdmin eventAdmin;
    private volatile Publisher publisher;
    private volatile BeanService beanService;
    private volatile MessageService messageService;
    private volatile JsonService jsonService;

    private LocalEventDispatcher localEventDispatcher = new LocalEventDispatcher();

    @Override
    public void install() {
        new InstallerImpl().install();
    }

    public ComponentCache getComponentCache() {
        return componentCache;
    }

    @Override
    public Clock getClock() {
        return clock;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setComponentCache(CacheService cacheService) {
        this.componentCache = cacheService.createComponentCache(ormClient.getDataModel());
    }

    @Override
    public OrmClient getOrmClient() {
        return ormClient;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(Bus.COMPONENTNAME, "Events");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
        this.ormClient = new OrmClientImpl(dataModel);
    }

    @Reference(name = "ZCacheService")
    public void setCacheService(CacheService cacheService) {
        this.componentCache = cacheService.createComponentCache(ormClient.getDataModel());
    }

    @Override
    public EventAdmin getEventAdmin() {
        return eventAdmin;
    }

    @Reference
    public void setEventAdmin(EventAdmin eventAdmin) {
        this.eventAdmin = eventAdmin;
    }

    @Override
    public Publisher getPublisher() {
        return publisher;
    }

    @Reference
    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public BeanService getBeanService() {
        return beanService;
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

    @Override
    public JsonService getJsonService() {
        return jsonService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Override
    public EventService getEventService() {
        return this;
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
        Bus.setServiceLocator(this);
        localEventDispatcher.register(context);
    }

    @Deactivate
    public void deactivate() {
        Bus.setServiceLocator(this);
    }

    @Override
    public void postEvent(String topic, Object source) {
        Optional<EventType> found = getOrmClient().getEventTypeFactory().get(topic);
        if (!found.isPresent()) {
            throw new NoSuchTopicException(topic);
        }
        EventType eventType = found.get();
        LocalEvent localEvent = eventType.create(source);
        getPublisher().publish(localEvent); // synchronous call, may throw an exception to prevent transaction commit should be prior to further propagating the event.
        getEventAdmin().postEvent(localEvent.toOsgiEvent());
        if (eventType.shouldPublish()) {
            localEvent.publish();
        }
    }

    @Override
    public EventTypeBuilder buildEventTypeWithTopic(String topic) {
        return new EventTypeBuilderImpl(topic);
    }

    @Override
    public EventConfiguration getEventConfiguration() {
        return new EventConfiguration() {
            @Override
            public String getEventDestinationName() {
                return JUPITER_EVENTS;
            }
        };
    }
}
