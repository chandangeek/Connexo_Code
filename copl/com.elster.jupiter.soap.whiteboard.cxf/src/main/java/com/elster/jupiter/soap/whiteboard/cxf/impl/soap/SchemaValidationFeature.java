/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl.soap;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.validation.SchemaValidationTypeProvider;

public class SchemaValidationFeature extends org.apache.cxf.feature.validation.SchemaValidationFeature {
    public SchemaValidationFeature(SchemaValidationTypeProvider provider) {
        super(provider);
    }

    @Override
    public void initialize(Server server, Bus bus) {
        super.initialize(server, bus);
        EnabledSchemaValidationInterceptor interceptor = new EnabledSchemaValidationInterceptor();
        server.getEndpoint().getInInterceptors().add(interceptor);
    }
}