/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventPropertyType;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.NoSuchTopicException;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EventServiceImplTest {

    private static final String TOPIC = "topic";
    private EventServiceImpl eventService;

    private EventTypeImpl eventType;

    private Clock clock = Clock.systemDefaultZone();

    @Mock
    private OrmService ormService;
    @Mock
    private DataModel dataModel;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Table table;
    @Mock
    private DataMapper<EventType> eventTypeFactory;
    @Mock
    private Publisher publisher;
    @Mock
    private LocalEvent localEvent;
    @Mock
    private DataMapper<EventPropertyType> eventTypePropertyFactory;
    @Mock
    private NlsService nlsService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private NlsMessageFormat nlsMessageFormat;
    @Mock
    private JsonService jsonService;
    @Mock
    private EventConfiguration eventConfig;
    @Mock
    private MessageService messageService;
    @Mock
    private BeanService beanService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DestinationSpec destination;

    @Before
    public void setUp() {

        when(dataModel.getInstance(EventTypeImpl.class)).thenAnswer(inv -> new EventTypeImpl(dataModel, clock, jsonService, eventConfig, messageService, beanService, thesaurus));

        eventType = EventTypeImpl.from(dataModel, TOPIC);

        when(ormService.newDataModel(anyString(), anyString())).thenReturn(dataModel);
        when(dataModel.addTable(anyString(),any())).thenReturn(table);
        when(eventTypeFactory.getOptional(TOPIC)).thenReturn(Optional.<EventType>of(eventType));
        when(dataModel.mapper(EventPropertyType.class)).thenReturn(eventTypePropertyFactory);
        when(dataModel.mapper(EventType.class)).thenReturn(eventTypeFactory);
        when(nlsService.getThesaurus(EventService.COMPONENTNAME, Layer.DOMAIN)).thenReturn(thesaurus);
        when(thesaurus.getFormat(any(MessageSeed.class))).thenReturn(nlsMessageFormat);
        when(nlsMessageFormat.format(anyVararg())).thenReturn("");
        when(messageService.getDestinationSpec(any())).thenReturn(Optional.of(destination));

        eventService = new EventServiceImpl();

        eventService.setOrmService(ormService);
        eventService.setPublisher(publisher);
        eventService.setNlsService(nlsService);
    }

    @After
    public void tearDown() {
    }

    @Test(expected = NoSuchTopicException.class)
    public void testPostEventForNotExistingTopic() {
        when(eventTypeFactory.getOptional(TOPIC)).thenReturn(Optional.empty());

        eventService.postEvent(TOPIC, "");
    }

    @Test
    public void testPostEventPublishesLocalEventToPublisher() {
        eventService.postEvent(TOPIC, "");

        ArgumentCaptor<LocalEvent> localEventCaptor = ArgumentCaptor.forClass(LocalEvent.class);
        verify(publisher).publish(localEventCaptor.capture(), anyVararg());

        LocalEvent capture = localEventCaptor.getValue();
        assertThat(capture.getSource()).isEqualTo("");
    }

    @Test
    public void testPublishToDestinationIfShouldPublish() {
        eventType.setPublish(true);

        eventService.postEvent(TOPIC, "");

        verify(destination).message(anyString());
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

        assertThat(eventTypeBuilder.create()).isEqualTo(eventType);
    }


}
