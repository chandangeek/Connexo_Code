/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.events;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface EventTypeBuilder {

    EventTypeBuilder component(String component);

    EventTypeBuilder scope(String component);

    EventTypeBuilder category(String component);

    EventTypeBuilder name(String component);

    EventTypeBuilder shouldPublish();

    EventTypeBuilder shouldNotPublish();

    EventTypeBuilder enableForUseInStateMachines();

    EventTypeBuilder disableForUseInStateMachines();

    EventTypeBuilder withProperty(String name, ValueType valueType, String accessPath);

    EventType create();
}
