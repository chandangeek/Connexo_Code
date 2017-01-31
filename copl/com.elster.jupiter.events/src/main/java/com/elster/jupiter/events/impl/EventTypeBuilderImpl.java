/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.DataModel;

public class EventTypeBuilderImpl implements EventTypeBuilder {

    private final EventTypeImpl underConstruction;

    public EventTypeBuilderImpl(DataModel dataModel, String topic) {
        underConstruction = EventTypeImpl.from(dataModel, topic);
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
    public EventTypeBuilder enableForUseInStateMachines() {
        underConstruction.enableForUseInStateMachines();
        return this;
    }

    @Override
    public EventTypeBuilder disableForUseInStateMachines() {
        underConstruction.disableForUseInStateMachines();
        return this;
    }

    @Override
    public EventTypeBuilder withProperty(String name, ValueType valueType, String accessPath) {
        underConstruction.addProperty(name, valueType, accessPath);
        return this;
    }

    @Override
    public EventType create() {
        underConstruction.save();
        return underConstruction;
    }
}
