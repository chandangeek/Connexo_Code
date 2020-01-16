/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.schema.SchemaInfoProvider;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.util.json.JsonService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import javax.sql.DataSource;
import javax.validation.ValidationProviderResolver;
import java.time.Clock;

public class OrmModule extends AbstractModule {

    private SchemaInfoProvider schemaInfoProvider;

    public OrmModule() {
    }

    public OrmModule(SchemaInfoProvider schemaInfoProvider) {
        this.schemaInfoProvider = schemaInfoProvider;
    }

    @Override
    protected void configure() {
        requireBinding(Clock.class);
        requireBinding(DataSource.class);
        requireBinding(JsonService.class);
        requireBinding(ThreadPrincipalService.class);
        requireBinding(Publisher.class);
        requireBinding(ValidationProviderResolver.class);
    }

    public SchemaInfoProvider getSchemaInfoProvider() {
        return schemaInfoProvider;
    }

    public void setSchemaInfoProvider(SchemaInfoProvider schemaInfoProvider) {
        this.schemaInfoProvider = schemaInfoProvider;
    }
}