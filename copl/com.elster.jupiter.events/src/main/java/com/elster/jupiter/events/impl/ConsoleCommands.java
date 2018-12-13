/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventPropertyType;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Stream;

@Component(name = "com.elster.jupiter.events.console", service = ConsoleCommands.class, property = {
        "osgi.command.scope=events",
        "osgi.command.function=eventTypes"
}, immediate = true)
public class ConsoleCommands {

    private volatile EventService eventService;

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    public void eventTypes(String... components) {
        Stream<EventType> eventTypes = null;
        if (components.length == 0) {
            eventTypes = eventService.getEventTypes().stream();
        } else {
            eventTypes = Arrays.stream(components)
                    .map(eventService::getEventTypesForComponent)
                    .flatMap(Collection::stream);
        }
        eventTypes.map(this::toString)
                .forEach(System.out::println);
    }

    private String toString(EventType eventType) {
        StringBuilder builder = new StringBuilder()
                .append("Component : ").append(eventType.getComponent())
                .append(" - Scope : ").append(eventType.getScope())
                .append(" - Category : ").append(eventType.getCategory())
                .append(" - Name : ").append(eventType.getName()).append('\n')
                .append('\t').append("Topic : ").append(eventType.getTopic()).append('\n')
                .append('\t').append("Publish : ").append(eventType.shouldPublish() ? "Y" : "N").append('\n');
        eventType.getPropertyTypes().stream()
                .sorted(Comparator.comparing(EventPropertyType::getPosition))
                .forEach(property -> {
                    builder.append('\t')
                            .append('\t')
                            .append(property.getPosition())
                            .append(' ')
                            .append(property.getAccessPath())
                            .append(' ')
                            .append(property.getValueType());
                });
        return builder.toString();
    }
}
