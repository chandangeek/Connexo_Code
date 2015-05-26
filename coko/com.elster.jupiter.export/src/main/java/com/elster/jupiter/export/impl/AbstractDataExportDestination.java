package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportDestination;
import com.elster.jupiter.export.EmailDestination;
import com.elster.jupiter.export.FileDestination;
import com.elster.jupiter.export.ReadingTypeDataExportTask;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Created by igh on 22/05/2015.
 */
public class AbstractDataExportDestination implements DataExportDestination {

    static final Map<String, Class<? extends DataExportDestination>> IMPLEMENTERS = ImmutableMap.<String, Class<? extends DataExportDestination>>of(FileDestination.TYPE_IDENTIFIER, FileDestinationImpl.class, EmailDestination.TYPE_IDENTIFIER, EmailDestinationImpl.class);

    private ReadingTypeDataExportTask task;

    public ReadingTypeDataExportTask getTask() {
        return this.task;
    }
}
