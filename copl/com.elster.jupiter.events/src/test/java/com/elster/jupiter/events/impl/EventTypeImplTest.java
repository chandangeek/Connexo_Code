package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventPropertyType;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.cache.TypeCache;
import com.elster.jupiter.util.time.Clock;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Date;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EventTypeImplTest {

    private static final String TOPIC = "topic";
    private static final String COMPONENT = "CMP";
    private static final String SCOPE = "scope";
    private static final String CATEGORY = "category";
    private static final String NAME = "name";
    private static final String ACCESS_PATH = "owner.name";
    private static final String PROPERTY_NAME = "propertyName";
    private static final Date NOW = new DateTime(2013, 9, 14, 18, 45, 12).toDate();
    private static final String SOURCE = "source";

    @Mock
    private OrmClient ormClient;
    @Mock
    private ServiceLocator serviceLocator;
    @Mock
    private TypeCache<EventType> eventTypeFactory;
    @Mock
    private DataMapper<EventPropertyType> eventTypePropertyFactory;
    @Mock
    private Clock clock;

    @Before
    public void setUp() {
        when(serviceLocator.getOrmClient()).thenReturn(ormClient);
        when(ormClient.getEventTypeFactory()).thenReturn(eventTypeFactory);
        when(ormClient.getEventTypePropertyFactory()).thenReturn(eventTypePropertyFactory);
        when(serviceLocator.getClock()).thenReturn(clock);
        when(clock.now()).thenReturn(NOW);

        Bus.setServiceLocator(serviceLocator);
    }

    @After
    public void tearDown() {
        Bus.setServiceLocator(null);
    }

    @Test
    public void testGetTopic() {
        EventTypeImpl eventType = new EventTypeImpl(TOPIC);

        assertThat(eventType.getTopic()).isEqualTo(TOPIC);
    }

    @Test
    public void testSetTopic() {
        EventTypeImpl eventType = new EventTypeImpl("");

        eventType.setTopic(TOPIC);

        assertThat(eventType.getTopic()).isEqualTo(TOPIC);
    }

    @Test
    public void testComponent() {
        EventTypeImpl eventType = new EventTypeImpl(TOPIC);

        eventType.setComponent(COMPONENT);

        assertThat(eventType.getComponent()).isEqualTo(COMPONENT);
    }

    @Test
    public void testScope() {
        EventTypeImpl eventType = new EventTypeImpl(TOPIC);

        eventType.setScope(SCOPE);

        assertThat(eventType.getScope()).isEqualTo(SCOPE);
    }

    @Test
    public void testCategory() {
        EventTypeImpl eventType = new EventTypeImpl(TOPIC);

        eventType.setCategory(CATEGORY);

        assertThat(eventType.getCategory()).isEqualTo(CATEGORY);
    }

    @Test
    public void testName() {
        EventTypeImpl eventType = new EventTypeImpl(TOPIC);

        eventType.setName(NAME);

        assertThat(eventType.getName()).isEqualTo(NAME);
    }

    @Test
    public void testShouldPublishTrue() {
        EventTypeImpl eventType = new EventTypeImpl(TOPIC);

        eventType.setPublish(true);

        assertThat(eventType.shouldPublish()).isTrue();
    }

    @Test
    public void testShouldPublishFalse() {
        EventTypeImpl eventType = new EventTypeImpl(TOPIC);

        eventType.setPublish(false);

        assertThat(eventType.shouldPublish()).isFalse();
    }

    @Test
    public void testGetPropertyTypesEmpty() {
        EventTypeImpl eventType = new EventTypeImpl(TOPIC);

        assertThat(eventType.getPropertyTypes()).isEmpty();
    }

    @Test
    public void testAddPropertyYieldsPropertyWithCorrectName() {
        EventTypeImpl eventType = new EventTypeImpl(TOPIC);

        EventPropertyType eventPropertyType = eventType.addProperty(PROPERTY_NAME, ValueType.STRING, ACCESS_PATH);

        assertThat(eventPropertyType.getName()).isEqualTo(PROPERTY_NAME);
    }

    @Test
    public void testAddPropertyYieldsPropertyWithCorrectValueType() {
        EventTypeImpl eventType = new EventTypeImpl(TOPIC);

        EventPropertyType eventPropertyType = eventType.addProperty(PROPERTY_NAME, ValueType.STRING, ACCESS_PATH);

        assertThat(eventPropertyType.getValueType()).isEqualTo(ValueType.STRING);
    }

    @Test
    public void testAddPropertyYieldsPropertyWithCorrectAccessPath() {
        EventTypeImpl eventType = new EventTypeImpl(TOPIC);

        EventPropertyType eventPropertyType = eventType.addProperty(PROPERTY_NAME, ValueType.STRING, ACCESS_PATH);

        assertThat(eventPropertyType.getAccessPath()).isEqualTo(ACCESS_PATH);
    }

    @Test
    public void testAddPropertyIsAdded() {
        EventTypeImpl eventType = new EventTypeImpl(TOPIC);

        EventPropertyType eventPropertyType = eventType.addProperty(PROPERTY_NAME, ValueType.STRING, ACCESS_PATH);

        assertThat(eventType.getPropertyTypes()).hasSize(1)
                .contains(eventPropertyType);
    }

    @Test
    public void testSaveWithoutProperties() {
        EventTypeImpl eventType = new EventTypeImpl(TOPIC);

        eventType.save();

        verify(eventTypeFactory).persist(eventType);
    }

    @Test
    public void testSaveWithProperties() {
        EventTypeImpl eventType = new EventTypeImpl(TOPIC);
        EventPropertyType eventPropertyType = eventType.addProperty(PROPERTY_NAME, ValueType.STRING, ACCESS_PATH);

        eventType.save();

        verify(eventTypeFactory).persist(eventType);
        verify(eventTypePropertyFactory).persist(eventPropertyType);
    }

    @Test
    public void testUpdateRemovingProperty() {
        EventTypeImpl eventType = new EventTypeImpl(TOPIC);
        EventPropertyType eventPropertyType = eventType.addProperty(PROPERTY_NAME, ValueType.STRING, ACCESS_PATH);
        eventType.save();
        when(eventTypePropertyFactory.find("eventType", eventType)).thenReturn(Arrays.asList(eventPropertyType));

        eventType.removePropertyType(eventPropertyType);
        eventType.save();

        verify(eventTypeFactory).update(eventType);
        verify(eventTypePropertyFactory).remove(eventPropertyType);
    }

    @Test
    public void testUpdateAddingProperty() {
        EventTypeImpl eventType = new EventTypeImpl(TOPIC);
        EventPropertyType eventPropertyType = eventType.addProperty(PROPERTY_NAME, ValueType.STRING, ACCESS_PATH);
        eventType.save();
        when(eventTypePropertyFactory.find("eventType", eventType)).thenReturn(Arrays.asList(eventPropertyType));

        eventPropertyType = eventType.addProperty("newProperty", ValueType.STRING, ACCESS_PATH);
        eventType.save();

        verify(eventTypeFactory).update(eventType);
        verify(eventTypePropertyFactory).persist(eventPropertyType);
    }

    @Test
    public void testCreate() {
        EventTypeImpl eventType = new EventTypeImpl(TOPIC);
        eventType.addProperty(PROPERTY_NAME, ValueType.STRING, ACCESS_PATH);

        LocalEvent localEvent = eventType.create(SOURCE);

        assertThat(localEvent.getDateTime()).isEqualTo(NOW);
        assertThat(localEvent.getType()).isEqualTo(eventType);
        assertThat(localEvent.getSource()).isEqualTo(SOURCE);
    }

}
