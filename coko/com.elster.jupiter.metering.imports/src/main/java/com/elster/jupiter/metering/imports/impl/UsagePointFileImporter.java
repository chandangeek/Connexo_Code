package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.nls.Thesaurus;

import java.util.*;

public class UsagePointFileImporter implements FileImporter {

    private volatile Thesaurus thesaurus;
    private volatile MeteringService meteringService;
    private volatile Map<String, UsagePointParser> parsers;

    private UsagePointParser usagePointParser;
    private UsagePointProcessor usagePointProcessor;

    public UsagePointFileImporter(Thesaurus thesaurus, MeteringService meteringService, Map<String, UsagePointParser> parsers, UsagePointProcessor usagePointProcessor) {
        this.parsers = parsers;
        this.thesaurus = thesaurus;
        this.meteringService = meteringService;
        this.usagePointProcessor = usagePointProcessor;
    }

    @Override
    public void process(FileImportOccurrence fileImportOccurrence) {
        String fileExtension = getFileExtension(fileImportOccurrence.getFileName()).toUpperCase();
        if (parsers.containsKey(fileExtension)) {
            usagePointParser = parsers.get(fileExtension);
        } else {
            MessageSeeds.IMPORT_USAGEPOINT_PARSER_INVALID.log(fileImportOccurrence.getLogger(), thesaurus, fileExtension);
            return;
        }
        List<UsagePointFileInfo> usagePointFileInfos = usagePointParser.parse(fileImportOccurrence, thesaurus);
        usagePointProcessor.process(usagePointFileInfos, fileImportOccurrence);
    }

    private String getFileExtension(String fileExtension) {
        int lastPointIndex = fileExtension.lastIndexOf('.');
        return lastPointIndex != -1 ? fileExtension.substring(lastPointIndex, fileExtension.length()) : "";
    }
}