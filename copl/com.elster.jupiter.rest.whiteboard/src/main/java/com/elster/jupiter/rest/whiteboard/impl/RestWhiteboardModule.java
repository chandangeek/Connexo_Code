/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.whiteboard.impl;

import com.elster.jupiter.rest.whiteboard.ReferenceResolver;

import com.google.inject.AbstractModule;

public class RestWhiteboardModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ReferenceResolver.class).to(ReferenceResolverWhiteboard.class);

    }
}
