/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.events;

import com.elster.jupiter.util.HasName;

public interface EventPropertyType extends HasName {

    ValueType getValueType();

    String getAccessPath();

    int getPosition();

    EventType getEventType();
}
