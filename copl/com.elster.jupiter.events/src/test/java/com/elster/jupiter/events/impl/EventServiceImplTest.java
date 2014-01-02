package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventPropertyType;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.NoSuchTopicException;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.pubsub.Publisher;
import com.google.common.base.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EventServiceImplTest {

    private static final String TOPIC = "topic";
    private EventServiceImpl eventService;

    @Mock
    private OrmService ormService;
    @Mock
    private DataModel dataModel;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Table table;
    @Mock
    private DataMapper<EventType> eventTypeFactory;
    @Mock
    private EventType eventType;
    @Mock
    private Publisher publisher;
    @Mock
    private LocalEvent localEvent;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private DataMapper<EventPropertyType> eventTypePropertyFactory;

    @Before
    public void setUp() {

        when(ormService.newDataModel(anyString(), anyString())).thenReturn(dataModel);
        when(dataModel.addTable(anyString(),any(Class.class))).thenReturn(table);
        when(eventTypeFactory.getOptional(TOPIC)).thenReturn(Optional.of(eventType));
        when(eventType.create("")).thenReturn(localEvent);
        when(eventType.shouldPublish()).thenReturn(false);
        when(dataModel.mapper(EventPropertyType.class)).thenReturn(eventTypePropertyFactory);
        when(dataModel.mapper(EventType.class)).thenReturn(eventTypeFactory);

        eventService = new EventServiceImpl();

        eventService.setOrmService(ormService);
        eventService.setPublisher(publisher);
        eventService.setEventAdmin(eventAdmin);
    }

    @After
    public void tearDown() {
    }

    @Test(expected = NoSuchTopicException.class)
    public void testPostEventForNotExistingTopic() {
        when(eventTypeFactory.getOptional(TOPIC)).thenReturn(Optional.<EventType>absent());

        eventService.postEvent(TOPIC, "");
    }

    @Test
    public void testPostEventPublishesLocalEventToPublisher() {
        eventService.postEvent(TOPIC, "");

        verify(publisher).publish(localEvent);
    }

    @Test
    public void testPostEventPublishesOsgiEvent() {
        Event osgiEvent = mock(Event.class);
        when(localEvent.toOsgiEvent()).thenReturn(osgiEvent);

        eventService.postEvent(TOPIC, "");

        verify(eventAdmin).postEvent(osgiEvent);
    }

    @Test
    public void testPublishToDestinationIfShouldPublish() {
        when(eventType.shouldPublish()).thenReturn(true);

        eventService.postEvent(TOPIC, "");

        verify(localEvent).publish();
    }

    @Test
    public void testDoesNotPublishToDestinationIfShouldNotPublish() {
        eventService.postEvent(TOPIC, "");

        verify(localEvent, never()).publish();
    }

    /**
     * implementation specific : but avoids retesting the behavior of the return type implementation
     */
    @Test
    public void testBuildEventTypeWithTopic() {
        EventTypeBuilder eventTypeBuilder = eventService.buildEventTypeWithTopic(TOPIC);

        assertThat(eventTypeBuilder).isInstanceOf(EventTypeBuilderImpl.class);
    }

    @Test
    public void testBuildEventTypeWithTopicHasCorrectTopic() {
        EventTypeBuilder eventTypeBuilder = eventService.buildEventTypeWithTopic(TOPIC);

        assertThat(eventTypeBuilder.create().getTopic()).isEqualTo(TOPIC);
    }


}
