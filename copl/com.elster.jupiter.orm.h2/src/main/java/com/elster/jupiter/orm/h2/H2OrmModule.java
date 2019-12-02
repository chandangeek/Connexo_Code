/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.h2;

import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.orm.impl.OrmServiceImpl;
import com.elster.jupiter.orm.schema.SchemaInfoProvider;
import com.google.inject.Scopes;

public class H2OrmModule extends OrmModule {

    public H2OrmModule() {
        super(new H2SchemaInfo());
    }

    public H2OrmModule(SchemaInfoProvider schemaInfoProvider) {
        super(schemaInfoProvider);
    }

    @Override
    protected void configure() {
        super.configure();
        bind(SchemaInfoProvider.class).toInstance(getSchemaInfoProvider());
        bind(OrmService.class).to(OrmServiceImpl.class).in(Scopes.SINGLETON);
    }
}