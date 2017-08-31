/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.properties.PropertySpecService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class ExportProcessorsModule extends AbstractModule {
    @Override
    protected void configure() {
        requireBinding(PropertySpecService.class);
        requireBinding(DataExportService.class);
        requireBinding(NlsService.class);

        bind(CsvUsagePointDataFormatterFactory.class).in(Scopes.SINGLETON);
    }
}
