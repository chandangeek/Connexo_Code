package com.elster.jupiter.http.whiteboard;

import com.elster.jupiter.users.PrivilegeChecker;
import com.elster.jupiter.users.User;

/**
 * Copyrights EnergyICT
 * Date: 18/09/2014
 * Time: 9:46
 */
public class App {
    private final String context;
    private final String key;
    private final String name;
    private final String icon;
    private final HttpResource mainResource;
    private final String externalUrl;
    private final PrivilegeChecker privilegeChecker;

    public App(String key, String name, String icon, String context, HttpResource mainResource, String externalUrl, PrivilegeChecker privilegeChecker) {
        this.key = key;
        this.name = name;
        this.icon = icon;
        this.context = context;
        this.mainResource = mainResource;
        this.externalUrl = externalUrl;
        this.privilegeChecker = privilegeChecker;
    }

    public App(String key, String name, String icon, String context, HttpResource mainResource, PrivilegeChecker privilegeChecker) {
        this(key, name, icon, context, mainResource, null, privilegeChecker);
    }

    public App(String key, String name, String icon, String externalUrl, PrivilegeChecker privilegeChecker) {
        this(key, name, icon, null, null, externalUrl, privilegeChecker);
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
        return mainResource != null;
    }

    public String getKey() {
        return key;
    }

    public boolean isAllowed(User user) {
        return privilegeChecker != null ? privilegeChecker.allowed(user) : true;
    }
}
