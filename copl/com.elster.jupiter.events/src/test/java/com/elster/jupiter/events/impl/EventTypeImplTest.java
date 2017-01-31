/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.events.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.events.EventPropertyType;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.json.JsonService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EventTypeImplTest extends EqualsContractTest {

    private static final String TOPIC = "topic";
    private static final String COMPONENT = "CMP";
    private static final String SCOPE = "scope";
    private static final String CATEGORY = "category";
    private static final String NAME = "name";
    private static final String ACCESS_PATH = "owner.name";
    private static final String PROPERTY_NAME = "propertyName";
    private static final Instant NOW = ZonedDateTime.of(2013, 9, 14, 18, 45, 12, 0, ZoneId.systemDefault()).toInstant();
    private static final String SOURCE = "source";

    private EventType instanceA;

    @Mock
    private DataMapper<EventType> eventTypeFactory;
    @Mock
    private DataMapper<EventPropertyType> eventTypePropertyFactory;
    @Mock
    private Clock clock;
    @Mock
    private DataModel dataModel;
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
    @Override
    public void equalsContractSetUp() {
        when(clock.instant()).thenReturn(NOW);
        when(dataModel.mapper(EventType.class)).thenReturn(eventTypeFactory);
        when(dataModel.getInstance(EventTypeImpl.class)).thenAnswer(invocation -> new EventTypeImpl(dataModel, clock, jsonService, eventConfiguration, messageService, beanService, thesaurus));
        super.equalsContractSetUp();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetTopic() {
        EventTypeImpl eventType = EventTypeImpl.from(dataModel, TOPIC);

        assertThat(eventType.getTopic()).isEqualTo(TOPIC);
    }

    @Test
    public void testSetTopic() {
        EventTypeImpl eventType = EventTypeImpl.from(dataModel, "");

        eventType.setTopic(TOPIC);

        assertThat(eventType.getTopic()).isEqualTo(TOPIC);
    }

    @Test
    public void testComponent() {
        EventTypeImpl eventType = EventTypeImpl.from(dataModel, TOPIC);

        eventType.setComponent(COMPONENT);

        assertThat(eventType.getComponent()).isEqualTo(COMPONENT);
    }

    @Test
    public void testScope() {
        EventTypeImpl eventType = EventTypeImpl.from(dataModel, TOPIC);

        eventType.setScope(SCOPE);

        assertThat(eventType.getScope()).isEqualTo(SCOPE);
    }

    @Test
    public void testCategory() {
        EventTypeImpl eventType = EventTypeImpl.from(dataModel, TOPIC);

        eventType.setCategory(CATEGORY);

        assertThat(eventType.getCategory()).isEqualTo(CATEGORY);
    }

    @Test
    public void testName() {
        EventTypeImpl eventType = EventTypeImpl.from(dataModel, TOPIC);

        eventType.setName(NAME);

        assertThat(eventType.getName()).isEqualTo(NAME);
    }

    @Test
    public void testShouldPublishTrue() {
        EventTypeImpl eventType = EventTypeImpl.from(dataModel, TOPIC);

        eventType.setPublish(true);

        assertThat(eventType.shouldPublish()).isTrue();
    }

    @Test
    public void testShouldPublishFalse() {
        EventTypeImpl eventType = EventTypeImpl.from(dataModel, TOPIC);

        eventType.setPublish(false);

        assertThat(eventType.shouldPublish()).isFalse();
    }

    @Test
    public void testGetPropertyTypesEmpty() {
        EventTypeImpl eventType = EventTypeImpl.from(dataModel, TOPIC);

        assertThat(eventType.getPropertyTypes()).isEmpty();
    }

    @Test
    public void testAddPropertyYieldsPropertyWithCorrectName() {
        EventTypeImpl eventType = EventTypeImpl.from(dataModel, TOPIC);

        EventPropertyType eventPropertyType = eventType.addProperty(PROPERTY_NAME, ValueType.STRING, ACCESS_PATH);

        assertThat(eventPropertyType.getName()).isEqualTo(PROPERTY_NAME);
    }

    @Test
    public void testAddPropertyYieldsPropertyWithCorrectValueType() {
        EventTypeImpl eventType = EventTypeImpl.from(dataModel, TOPIC);

        EventPropertyType eventPropertyType = eventType.addProperty(PROPERTY_NAME, ValueType.STRING, ACCESS_PATH);

        assertThat(eventPropertyType.getValueType()).isEqualTo(ValueType.STRING);
    }

    @Test
    public void testAddPropertyYieldsPropertyWithCorrectAccessPath() {
        EventTypeImpl eventType = EventTypeImpl.from(dataModel, TOPIC);

        EventPropertyType eventPropertyType = eventType.addProperty(PROPERTY_NAME, ValueType.STRING, ACCESS_PATH);

        assertThat(eventPropertyType.getAccessPath()).isEqualTo(ACCESS_PATH);
    }

    @Test
    public void testAddPropertyIsAdded() {
        EventTypeImpl eventType = EventTypeImpl.from(dataModel, TOPIC);

        EventPropertyType eventPropertyType = eventType.addProperty(PROPERTY_NAME, ValueType.STRING, ACCESS_PATH);

        assertThat(eventType.getPropertyTypes()).hasSize(1)
                .contains(eventPropertyType);
    }

    @Test
    public void testSaveWithoutProperties() {
        EventTypeImpl eventType = EventTypeImpl.from(dataModel, TOPIC);

        eventType.save();

        verify(eventTypeFactory).persist(eventType);
    }

    @Test
    public void testSaveWithProperties() {
        EventTypeImpl eventType = EventTypeImpl.from(dataModel, TOPIC);
        EventPropertyType eventPropertyType = eventType.addProperty(PROPERTY_NAME, ValueType.STRING, ACCESS_PATH);

        eventType.save();

        verify(eventTypeFactory).persist(eventType);
        assertThat(eventType.getPropertyTypes()).hasSize(1);
    }

    @Test
    public void testUpdateRemovingProperty() {
        EventTypeImpl eventType = EventTypeImpl.from(dataModel, TOPIC);
        EventPropertyType eventPropertyType = eventType.addProperty(PROPERTY_NAME, ValueType.STRING, ACCESS_PATH);
        eventType.save();
        when(eventTypePropertyFactory.find("eventType", eventType)).thenReturn(Arrays.asList(eventPropertyType));

        eventType.removePropertyType(eventPropertyType);
        eventType.update();

        verify(eventTypeFactory).update(eventType);
        assertThat(eventType.getPropertyTypes()).isEmpty();
    }

    @Test
    public void testUpdateAddingProperty() {
        EventTypeImpl eventType = EventTypeImpl.from(dataModel, TOPIC);
        EventPropertyType eventPropertyType1 = eventType.addProperty(PROPERTY_NAME, ValueType.STRING, ACCESS_PATH);
        eventType.save();
        when(eventTypePropertyFactory.find("eventType", eventType)).thenReturn(Arrays.asList(eventPropertyType1));

        EventPropertyType eventPropertyType2 = eventType.addProperty("newProperty", ValueType.STRING, ACCESS_PATH);
        eventType.update();

        verify(eventTypeFactory).update(eventType);
        assertThat(eventType.getPropertyTypes()).hasSize(2).contains(eventPropertyType1, eventPropertyType2);
    }

    @Test
    public void testCreate() {
        EventTypeImpl eventType = EventTypeImpl.from(dataModel, TOPIC);
        eventType.addProperty(PROPERTY_NAME, ValueType.STRING, ACCESS_PATH);

        LocalEvent localEvent = eventType.create(SOURCE);

        assertThat(localEvent.getDateTime()).isEqualTo(NOW);
        assertThat(localEvent.getType()).isEqualTo(eventType);
        assertThat(localEvent.getSource()).isEqualTo(SOURCE);
    }

    @Override
    protected Object getInstanceA() {
        if (instanceA == null) {
            instanceA = EventTypeImpl.from(dataModel, TOPIC);
        }
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return EventTypeImpl.from(dataModel, TOPIC);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Collections.singletonList(EventTypeImpl.from(dataModel, TOPIC + "2"));
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }
}
