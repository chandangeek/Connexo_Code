package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventPropertyType;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
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
    private ServiceLocator serviceLocator;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OrmClient ormClient;

    @Before
    public void setUp() {
        when(ormClient.getEventTypePropertyFactory().find(eq("eventType"), any(EventType.class))).thenReturn(Collections.<EventPropertyType>emptyList());
        when(serviceLocator.getOrmClient()).thenReturn(ormClient);

        eventTypeBuilder = new EventTypeBuilderImpl(TOPIC)
                .category(CATEGORY)
                .component(COMPONENT)
                .name(NAME)
                .scope(SCOPE);

        Bus.setServiceLocator(serviceLocator);
    }

    @After
    public void tearDown() {
        Bus.setServiceLocator(null);
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
