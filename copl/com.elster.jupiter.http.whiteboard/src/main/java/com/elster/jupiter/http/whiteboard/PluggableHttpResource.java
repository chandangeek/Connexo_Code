package com.elster.jupiter.http.whiteboard;

public final class PluggableHttpResource extends HttpResource{
    public PluggableHttpResource(String alias, String localName, Resolver resolver) {
        super(alias, localName, resolver);
    }

    public PluggableHttpResource(String alias, String localName, Resolver resolver, StartPage startPage) {
        super(alias, localName, resolver, startPage);
    }
}
