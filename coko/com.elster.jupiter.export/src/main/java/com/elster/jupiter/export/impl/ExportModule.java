package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportService;
import com.google.inject.AbstractModule;

public class ExportModule extends AbstractModule {
    @Override
    protected void configure() {

        bind(DataExportService.class).to(DataExportServiceImpl.class);

    }
}
