/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.devices.topology;

import com.elster.jupiter.fileimport.csvimport.exceptions.FileImportParserException;
import com.energyict.mdc.device.data.importers.impl.FileImportDescription;
import com.energyict.mdc.device.data.importers.impl.FileImportDescriptionBasedParser;
import com.energyict.mdc.device.data.importers.impl.FileImportRecord;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;

import org.apache.commons.csv.CSVParser;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

public class DeviceTopologyImportParser<T extends FileImportRecord> extends FileImportDescriptionBasedParser {

    public DeviceTopologyImportParser(FileImportDescription<T> descriptor) {
        super(descriptor);
    }

    public void init(CSVParser csvParser) {
        initHeaders(csvParser);
    }

    private void initHeaders(CSVParser parser) {
        headers = parser.getHeaderMap().entrySet()
                .stream()
                .filter(entry -> entry.getKey() != null && !entry.getKey().isEmpty() && entry.getValue() != null)
                .sorted(Comparator.comparing(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        long numberOfMandatoryColumns = 2;
        if (headers.size() != numberOfMandatoryColumns) {
            throw new FileImportParserException(MessageSeeds.MISSING_TITLE_ERROR, numberOfMandatoryColumns, headers.size());
        }
    }
}
