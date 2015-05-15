package com.elster.jupiter.fileimport;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.HasName;

import java.util.List;
import java.util.Map;

public interface FileImporterFactory extends HasName {

    List<PropertySpec> getProperties();

    FileImporter createImporter(Map<String, Object> properties);

    String getName();

    String getDestinationName();
    String getApplicationName();


    void validateProperties(List<FileImporterProperty> properties);

}