package com.energyict.mdc.device.data.importers.impl;

import com.energyict.mdc.device.data.importers.impl.exceptions.FileImportParserException;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.util.List;
import java.util.stream.Collectors;

public interface FileImportParser<T extends FileImportRecord>{

    T parse(CSVRecord csvRecord, FileImportRecordContext recordContext) throws FileImportParserException;

    default List<String> parseHeaders(CSVParser parser) {
        return parser.getHeaderMap().entrySet()
                .stream()
                .filter(entry -> entry.getKey() != null && !entry.getKey().isEmpty() && entry.getValue() != null)
                .sorted((e1, e2) -> e1.getValue().compareTo(e2.getValue()))
                .map(entry -> entry.getKey())
                .collect(Collectors.toList());
    }

}
