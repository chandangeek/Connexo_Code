package com.elster.jupiter.export;

public class TextLineExportData implements FormattedExportData {

    private final StructureMarker structureMarker;
    private final String payload;

    private TextLineExportData(StructureMarker structureMarker, String payload) {
        this.structureMarker = structureMarker;
        this.payload = payload;
    }

    public static FormattedExportData of(StructureMarker structureMarker, String payload) {
        return new TextLineExportData(structureMarker, payload);
    }

    @Override
    public String getAppendablePayload() {
        return payload;
    }

    @Override
    public StructureMarker getStructureMarker() {
        return structureMarker;
    }
}
