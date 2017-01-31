/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import com.elster.jupiter.properties.HasDynamicProperties;
import com.elster.jupiter.util.HasName;

import java.util.List;
import java.util.Map;

public interface DataFormatterFactory extends HasDynamicProperties, HasName {

    DataFormatter createDataFormatter(Map<String, Object> properties);

    void validateProperties(List<DataExportProperty> properties);

    String getDisplayName();

}
