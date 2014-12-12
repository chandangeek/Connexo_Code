package com.elster.jupiter.export;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.HasName;

import java.util.List;
import java.util.Map;

public interface DataProcessorFactory extends HasName {

    List<PropertySpec<?>> getProperties();

    DataProcessor createDataFormatter(Map<String, Object> properties);

    String getName();


    void validateProperties(List<DataExportProperty> properties);

}
