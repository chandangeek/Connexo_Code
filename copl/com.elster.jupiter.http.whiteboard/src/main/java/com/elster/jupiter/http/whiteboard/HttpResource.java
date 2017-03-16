/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.http.whiteboard;

public final class HttpResource {
    private final String alias;
    private final String localName;
    private final Resolver resolver;
    private final StartPage startPage;

    public HttpResource(String alias, String localName, Resolver resolver) {
        this(alias, localName, resolver, new DefaultStartPage(alias));
    }

    public HttpResource(String alias, String localName, Resolver resolver, StartPage startPage) {
        this.alias = alias;
        this.localName = localName;
        this.resolver = resolver;
        this.startPage = startPage;
    }

    public String getAlias() {
        return alias;
    }

    public String getLocalName() {
        return localName;
    }

    public Resolver getResolver() {
        return resolver;
    }

    public StartPage getStartPage() {
        return startPage;
    }


}
