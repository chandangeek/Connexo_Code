package com.elster.jupiter.http.whiteboard;

/**
 * Copyrights EnergyICT
 * Date: 18/09/2014
 * Time: 9:46
 */
public class App {
    private final String context;
    private final String name;
    private final String icon;
    private final HttpResource mainResource;
    private final String externalUrl;

    public App(String name, String icon, String context, HttpResource mainResource) {
        this.name = name;
        this.icon = icon;
        this.context = context;
        this.mainResource = mainResource;
        this.externalUrl = null;
    }

    public App(String name, String icon, String externalUrl) {
        this.name = name;
        this.icon = icon;
        this.externalUrl = externalUrl;
        this.mainResource = null;
        this.context = null;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public HttpResource getMainResource() {
        return mainResource;
    }

    public String getContext() {
        return context;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public boolean isInternalApp() {
        return externalUrl == null;
    }
}
