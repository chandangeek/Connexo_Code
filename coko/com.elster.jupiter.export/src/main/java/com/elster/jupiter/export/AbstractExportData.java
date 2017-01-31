/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

public class AbstractExportData<T> implements ExportData {

    private final T data;
    private final StructureMarker structureMarker;

    protected AbstractExportData(T data, StructureMarker structureMarker) {
        this.data = data;
        this.structureMarker = structureMarker;
    }

    @Override
    public final StructureMarker getStructureMarker() {
        return structureMarker;
    }

    protected final T getData() {
        return data;
    }
}
