/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.HasName;

public interface CustomTaskProperty extends HasName {

    CustomTask getTask();

    String getDisplayName();

    Object getValue();

    void setValue(Object value);

    void save();

    boolean instanceOfSpec(PropertySpec spec);
}
