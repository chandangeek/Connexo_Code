package com.elster.jupiter.fileimport;

import aQute.bnd.annotation.ConsumerType;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.properties.HasDynamicProperties;
import com.elster.jupiter.util.HasName;

import java.util.List;
import java.util.Map;

@ConsumerType
public interface FileImporterFactory extends HasDynamicProperties, HasName {

    FileImporter createImporter(Map<String, Object> properties);

    String getDisplayName();

    String getDisplayName(String property);

    String getDestinationName();

    String getApplicationName();

    void validateProperties(List<FileImporterProperty> properties);

    NlsKey getNlsKey();

    NlsKey getPropertyNlsKey(String property);

    String getDefaultFormat();

    String getPropertyDefaultFormat(String property);

    List<String> getRequiredProperties();

    /**
     * Indicates if the FileImporter does its own transaction management or not
     *
     * @return
     */
    default boolean requiresTransaction() {
        return true;
    }

    ;
}