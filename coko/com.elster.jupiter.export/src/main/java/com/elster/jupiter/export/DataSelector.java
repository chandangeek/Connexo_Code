package com.elster.jupiter.export;

import aQute.bnd.annotation.ConsumerType;

import java.util.stream.Stream;

@ConsumerType
public interface DataSelector {

    Stream<ExportData> selectData(DataExportOccurrence dataExportOccurrence);
}
