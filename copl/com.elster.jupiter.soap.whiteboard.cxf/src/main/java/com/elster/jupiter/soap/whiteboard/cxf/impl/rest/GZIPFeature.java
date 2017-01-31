/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl.rest;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

import javax.inject.Inject;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * This endpointconfiguration-aware feature if register the GzipEncoder if compression was selected.
 * The conventional way of configuring compression (using EncodingFilter.enableFor(secureConfig, GZipEncoder.class)) can
 * not be used due to a Jersey bug (https://java.net/jira/browse/JERSEY-3074): we need to split output writing ourselves
 * because Jersey does not offer a hook to catch output before it gets compressed.
 */
public class GZIPFeature implements Feature {

    private EndPointConfiguration endPointConfiguration;

    @Inject
    public GZIPFeature() {
    }

    public GZIPFeature init(EndPointConfiguration endPointConfiguration) {
        this.endPointConfiguration = endPointConfiguration;
        return this;
    }

    @Override
    public boolean configure(FeatureContext context) {
        if (endPointConfiguration.isHttpCompression()) {
            context.register(GZIPWriterFilterInterceptor.class);
            return true;
        }
        return false;
    }
}
