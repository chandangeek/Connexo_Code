package com.elster.jupiter.events;

public interface EventTypeBuilder {

    EventTypeBuilder component(String component);

    EventTypeBuilder scope(String component);

    EventTypeBuilder category(String component);

    EventTypeBuilder name(String component);

    EventTypeBuilder shouldPublish();

    EventTypeBuilder shouldNotPublish();

    EventTypeBuilder withProperty(String name, ValueType valueType, String accessPath);

    EventType create();
}
