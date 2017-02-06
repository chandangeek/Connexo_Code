/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.slp.importers.impl;

import com.elster.jupiter.slp.SyntheticLoadProfileService;
import com.elster.jupiter.slp.impl.SyntheticLoadProfileServiceImpl;
import com.elster.jupiter.slp.importers.impl.correctionfactor.CorrectionFactorImporterFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;


public class SyntheticLoadProfileImportModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(SyntheticLoadProfileDataImporterContext.class).to(SyntheticLoadProfileDataImporterContext.class).in(Scopes.SINGLETON);
    }
}
