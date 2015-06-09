package com.elster.jupiter.fileimport;

import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.HasDynamicProperties;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public interface FileImporterFactory extends HasDynamicProperties, HasName {

    FileImporter createImporter(Map<String, Object> properties);

    String getDisplayName();

    String getDisplayName(String property);


    String getDestinationName();
    String getApplicationName();


    void validateProperties(List<FileImporterProperty> properties);

    void init(Logger logger);

    NlsKey getNlsKey();

    NlsKey getPropertyNlsKey(String property);

    String getDefaultFormat();

    String getPropertyDefaultFormat(String property);

    List<String> getRequiredProperties();

}