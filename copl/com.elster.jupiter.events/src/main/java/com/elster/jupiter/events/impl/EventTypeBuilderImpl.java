package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;

public class EventTypeBuilderImpl implements EventTypeBuilder {

    private final EventTypeImpl underConstruction;

    public EventTypeBuilderImpl(String topic) {
        underConstruction = new EventTypeImpl(topic);
    }

    @Override
    public EventTypeBuilder component(String component) {
        underConstruction.setComponent(component);
        return this;
    }

    @Override
    public EventTypeBuilder scope(String scope) {
        underConstruction.setScope(scope);
        return this;
    }

    @Override
    public EventTypeBuilder category(String category) {
        underConstruction.setCategory(category);
        return this;
    }

    @Override
    public EventTypeBuilder name(String name) {
        underConstruction.setName(name);
        return this;
    }

    @Override
    public EventTypeBuilder shouldPublish() {
        underConstruction.setPublish(true);
        return this;
    }

    @Override
    public EventTypeBuilder shouldNotPublish() {
        underConstruction.setPublish(false);
        return this;
    }

    @Override
    public EventTypeBuilder withProperty(String name, ValueType valueType, String accessPath) {
        underConstruction.addProperty(name, valueType, accessPath);
        return this;
    }

    @Override
    public EventType create() {
        return underConstruction;
    }
}
