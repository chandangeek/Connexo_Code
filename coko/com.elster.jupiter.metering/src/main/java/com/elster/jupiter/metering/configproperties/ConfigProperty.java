/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.configproperties;

import com.elster.jupiter.util.HasName;

public interface ConfigProperty extends HasName {

    Object getValue();

    String getStringValue();

    void setValue(Object value);

    void setName(String name);

    void save();

}
