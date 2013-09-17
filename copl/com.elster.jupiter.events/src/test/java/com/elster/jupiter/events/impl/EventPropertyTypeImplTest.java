package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.ValueType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EventPropertyTypeImplTest {

    private static final String TOPIC = "topic";
    private static final String NAME = "name";
    private static final String ACCESS_PATH = "owner.age";
    private static final int POSITION = 5;
    private EventPropertyTypeImpl eventPropertyType;

    @Mock
    private EventType eventType;

    @Before
    public void setUp() {
        when(eventType.getTopic()).thenReturn(TOPIC);

        eventPropertyType = new EventPropertyTypeImpl(eventType, NAME, ValueType.INTEGER, ACCESS_PATH, POSITION);


    }

    @After
    public void tearDown() {

    }

    @Test
    public void testGetValueType() {
        assertThat(eventPropertyType.getValueType()).isEqualTo(ValueType.INTEGER);
    }

    @Test
    public void testGetAccessPath() {
        assertThat(eventPropertyType.getAccessPath()).isEqualTo(ACCESS_PATH);
    }

    @Test
    public void testGetPosition() {
        assertThat(eventPropertyType.getPosition()).isEqualTo(POSITION);
    }

    @Test
    public void testGetName() {
        assertThat(eventPropertyType.getName()).isEqualTo(NAME);
    }

}
