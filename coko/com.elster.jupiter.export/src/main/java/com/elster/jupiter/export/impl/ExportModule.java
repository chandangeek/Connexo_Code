/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class ExportModule extends AbstractModule {
    @Override
    protected void configure() {

        bind(DataExportService.class).to(DataExportServiceImpl.class).in(Scopes.SINGLETON);

    }
}
