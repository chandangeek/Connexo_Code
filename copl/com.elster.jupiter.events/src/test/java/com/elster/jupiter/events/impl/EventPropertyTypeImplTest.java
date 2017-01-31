/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.events.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.ValueType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EventPropertyTypeImplTest extends EqualsContractTest {

    private static final String TOPIC = "topic";
    private static final String NAME = "name";
    private static final String ACCESS_PATH = "owner.age";
    private static final int POSITION = 5;

    private EventPropertyTypeImpl instanceA;

    private EventPropertyTypeImpl eventPropertyType;

    @Mock
    private EventType eventType, otherEventType;

    @Before
    @Override
    public void equalsContractSetUp() {
        when(eventType.getTopic()).thenReturn(TOPIC);
        when(otherEventType.getTopic()).thenReturn("other" + TOPIC);

        eventPropertyType = new EventPropertyTypeImpl(eventType, NAME, ValueType.INTEGER, ACCESS_PATH, POSITION);

        super.equalsContractSetUp();
    }

    @After
    public void tearDown() {

    }

    @Override
    protected Object getInstanceA() {
        if (instanceA == null) {
            instanceA = new EventPropertyTypeImpl(eventType, NAME, ValueType.INTEGER, ACCESS_PATH, POSITION);
        }
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new EventPropertyTypeImpl(eventType, NAME, ValueType.INTEGER, ACCESS_PATH, POSITION);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Arrays.asList(
                new EventPropertyTypeImpl(otherEventType, NAME, ValueType.INTEGER, ACCESS_PATH, POSITION),
                new EventPropertyTypeImpl(eventType, "other" + NAME, ValueType.INTEGER, ACCESS_PATH, POSITION)
                );
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
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
