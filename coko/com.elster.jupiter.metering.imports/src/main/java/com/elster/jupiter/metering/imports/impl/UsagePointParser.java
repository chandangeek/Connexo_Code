package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.nls.Thesaurus;

import java.util.List;

public interface UsagePointParser {
    List<UsagePointFileInfo> parse(FileImportOccurrence fileImportOccurrence, Thesaurus thesaurus);

    List<String> getParserFormatExtensionName();
}