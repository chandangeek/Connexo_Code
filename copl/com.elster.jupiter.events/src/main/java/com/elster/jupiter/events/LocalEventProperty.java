/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.events;

public interface LocalEventProperty {

    Object getValue();

    <T> T getValue(Class<T> clazz);

}
