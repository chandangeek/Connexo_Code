/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import com.elster.jupiter.properties.HasDynamicProperties;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ConsumerType;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@ConsumerType
public interface DataSelectorFactory extends HasDynamicProperties, HasName {

    DataSelector createDataSelector(Map<String, Object> properties, Logger logger);

    void validateProperties(List<DataExportProperty> properties);

    String getDisplayName();

    boolean isDefault();

    List<String> targetApplications();
}
