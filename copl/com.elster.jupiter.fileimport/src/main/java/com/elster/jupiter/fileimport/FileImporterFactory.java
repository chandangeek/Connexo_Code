/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport;

import aQute.bnd.annotation.ConsumerType;
import com.elster.jupiter.properties.HasDynamicProperties;
import com.elster.jupiter.util.HasName;

import java.util.List;
import java.util.Map;

@ConsumerType
public interface FileImporterFactory extends HasDynamicProperties, HasName {

    FileImporter createImporter(Map<String, Object> properties);

    String getDisplayName();

    String getDestinationName();

    String getApplicationName();

    void validateProperties(List<FileImporterProperty> properties);

    /**
     * Indicates if the FileImporter does its own transaction management or not.
     * If the FileImporter does its own transaction management, this method should return false.
     * When the importer does it's own transaction management, any interaction (including logging) with the fileImportOccurrence
     * needs to happen <b>outside</b> of a transaction
     *
     * @return
     */
    default boolean requiresTransaction() {
        return true;
    }

}