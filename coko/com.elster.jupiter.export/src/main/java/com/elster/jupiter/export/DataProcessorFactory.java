package com.elster.jupiter.export;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.HasName;

import java.util.List;

public interface DataProcessorFactory extends HasName {

    List<PropertySpec<?>> getProperties();

    DataProcessor createDataFormatter(List<DataExportProperty> properties);

    String getName();

    DataProcessor createTemplateDataFormatter();
}
