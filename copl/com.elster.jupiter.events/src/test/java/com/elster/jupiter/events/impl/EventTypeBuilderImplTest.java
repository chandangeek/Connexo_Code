package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Clock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EventTypeBuilderImplTest {

    private static final String TOPIC = "topic";
    private static final String CATEGORY = "category";
    private static final String COMPONENT = "CMP";
    private static final String NAME = "name";
    private static final String SCOPE = "scope";

    private EventTypeBuilder eventTypeBuilder;

    @Mock
    private DataModel dataModel;
    @Mock
    private Clock clock;
    @Mock
    private JsonService jsonService;
    @Mock
    private EventConfiguration eventConfiguration;
    @Mock
    private MessageService messageService;
    @Mock
    private BeanService beanService;
    @Mock
    private Thesaurus thesaurus;

    @Before
    public void setUp() {
        when(dataModel.getInstance(EventTypeImpl.class)).thenReturn(new EventTypeImpl(dataModel, clock, jsonService, eventConfiguration, messageService, beanService, thesaurus));
        eventTypeBuilder = new EventTypeBuilderImpl(dataModel, clock, jsonService, eventConfiguration, messageService, beanService, TOPIC)
                .category(CATEGORY)
                .component(COMPONENT)
                .name(NAME)
                .scope(SCOPE);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testCreatedEventTypeHasCorrectTopic() {
        assertThat(eventTypeBuilder.create().getTopic()).isEqualTo(TOPIC);
    }

    @Test
    public void testCreatedEventTypeHasCorrectCategory() {
        assertThat(eventTypeBuilder.create().getCategory()).isEqualTo(CATEGORY);
    }

    @Test
    public void testCreatedEventTypeHasCorrectName() {
        assertThat(eventTypeBuilder.create().getName()).isEqualTo(NAME);
    }

    @Test
    public void testCreatedEventTypeHasCorrectScope() {
        assertThat(eventTypeBuilder.create().getScope()).isEqualTo(SCOPE);
    }

    @Test
    public void testCreatedEventTypeHasCorrectPublishIfTrue() {

        EventType eventType = eventTypeBuilder
                .shouldPublish()
                .create();

        assertThat(eventType.shouldPublish()).isEqualTo(true);
    }

    @Test
    public void testCreatedEventTypeHasCorrectPublishIfFalse() {

        EventType eventType = eventTypeBuilder
                .shouldNotPublish()
                .create();

        assertThat(eventType.shouldPublish()).isEqualTo(false);
    }

    @Test
    public void testCreatedEventTypeHasCorrectPropertiesNone() {

        EventType eventType = eventTypeBuilder
                .shouldNotPublish()
                .create();

        assertThat(eventType.getPropertyTypes()).isEmpty();
    }

    @Test
    public void testCreatedEventTypeHasCorrectPropertiesOne() {

        EventType eventType = eventTypeBuilder
                .withProperty("property", ValueType.STRING, "owner.age")
                .create();

        assertThat(eventType.getPropertyTypes()).hasSize(1);
    }


}
