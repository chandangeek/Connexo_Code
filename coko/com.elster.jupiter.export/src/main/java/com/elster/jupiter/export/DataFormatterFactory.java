package com.elster.jupiter.export;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.HasName;

import java.util.List;

public interface DataFormatterFactory extends HasName {

    List<PropertySpec<?>> getProperties();

    DataFormatter createDataFormatter(List<DataExportProperty> properties);
}
