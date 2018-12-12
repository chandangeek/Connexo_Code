/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.events.rest.impl;

import com.elster.jupiter.events.EventType;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EventTypeResourceTest extends EventApplicationTest {
    private static final String EVENT_TYPE_TOPIC = "event-topic";

    private EventType mockEventType(String topic) {
        EventType eventType = mock(EventType.class);
        when(eventType.getTopic()).thenReturn(topic);
        when(eventType.getComponent()).thenReturn("TST");
        when(eventType.getScope()).thenReturn("scope");
        when(eventType.getCategory()).thenReturn("category");
        when(eventType.getName()).thenReturn(topic);
        when(eventType.shouldPublish()).thenReturn(true);
        when(eventType.getVersion()).thenReturn(1L);
        return eventType;
    }

    @Test
    public void getListOfAllEventTopics() {
        List<EventType> eventTypes = new ArrayList<>();
        eventTypes.add(mockEventType("topic1"));
        eventTypes.add(mockEventType("topic2"));
        when(eventService.getEventTypes()).thenReturn(eventTypes);

        String response = target("/eventtypes").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(2);
        assertThat(model.<List>get("$.eventTypes")).isNotEmpty();
    }

    @Test
    public void getEventTopicByTopic() {
        EventType eventType = mockEventType(EVENT_TYPE_TOPIC);
        when(eventService.getEventType(EVENT_TYPE_TOPIC)).thenReturn(Optional.of(eventType));

        String response = target("/eventtypes/" + EVENT_TYPE_TOPIC).request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(1);
        assertThat(model.<List>get("$.eventTypes")).isNotEmpty();
        assertThat(model.<String>get("$.eventTypes[0].topic")).isEqualTo(EVENT_TYPE_TOPIC);
        assertThat(model.<String>get("$.eventTypes[0].component")).isEqualTo("TST");
        assertThat(model.<String>get("$.eventTypes[0].scope")).isEqualTo("scope");
        assertThat(model.<String>get("$.eventTypes[0].category")).isEqualTo("category");
        assertThat(model.<String>get("$.eventTypes[0].name")).isEqualTo(EVENT_TYPE_TOPIC);
        assertThat(model.<Number>get("$.eventTypes[0].version")).isEqualTo(1);
        assertThat(model.<Boolean>get("$.eventTypes[0].publish")).isTrue();
    }

    @Test
    public void getEventTopicByBadTopic() {
        when(eventService.getEventType(EVENT_TYPE_TOPIC)).thenReturn(Optional.empty());

        Response response = target("/eventtypes/" + EVENT_TYPE_TOPIC).request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void updateEventTypeWithTopicNoConflict() {
        EventType eventType = mockEventType(EVENT_TYPE_TOPIC);
        when(eventType.shouldPublish()).thenReturn(false);
        when(eventService.getEventType(EVENT_TYPE_TOPIC)).thenReturn(Optional.of(eventType));
        when(eventService.findAndLockEventTypeByNameAndVersion(EVENT_TYPE_TOPIC, 1)).thenReturn(Optional.of(eventType));
        EventTypeInfo info = new EventTypeInfo();
        info.topic = EVENT_TYPE_TOPIC;
        info.version = 1L;
        info.publish = false;
        Entity<EventTypeInfo> json = Entity.json(info);

        Response response = target("/eventtypes/"+EVENT_TYPE_TOPIC).request().put(json);
        JsonModel model = JsonModel.model(response.readEntity(String.class));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(model.<Boolean>get("$.eventTypes[0].publish")).isFalse();
    }

    @Test
    public void updateEventTypeWithTopicConcurrentModification() {
        when(eventService.findAndLockEventTypeByNameAndVersion(EVENT_TYPE_TOPIC, 1)).thenReturn(Optional.empty());
        when(eventService.getEventType(EVENT_TYPE_TOPIC)).thenReturn(Optional.empty());
        EventTypeInfo info = new EventTypeInfo();
        info.topic = EVENT_TYPE_TOPIC;
        info.version = 1L;
        info.publish = false;
        Entity<EventTypeInfo> json = Entity.json(info);

        Response response = target("/eventtypes/"+EVENT_TYPE_TOPIC).request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }
}
