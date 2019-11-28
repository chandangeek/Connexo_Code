/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.oracle;

import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.orm.impl.OrmServiceImpl;
import com.elster.jupiter.orm.schema.SchemaInfoProvider;
import com.google.inject.Scopes;

public class OracleOrmModule extends OrmModule {

    public OracleOrmModule() {
        super(new OracleSchemaInfo());
    }

    public OracleOrmModule(SchemaInfoProvider schemaInfoProvider) {
        super(schemaInfoProvider);
    }

    @Override
    protected void configure() {
        super.configure();
        bind(SchemaInfoProvider.class).toInstance(getSchemaInfoProvider());
        bind(OrmService.class).to(OrmServiceImpl.class).in(Scopes.SINGLETON);
    }
}