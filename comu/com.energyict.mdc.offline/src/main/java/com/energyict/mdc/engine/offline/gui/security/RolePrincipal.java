package com.energyict.mdc.engine.offline.gui.security;

import java.io.Serializable;
import java.security.Principal;

/**
 * @author bvn
 * @since 28/02/2012
 */
public class RolePrincipal implements Principal, Serializable {

    private static String WEB_CLIENT_USER = "WEB-CLIENT-USER";
    private static String FULL_CLIENT_USER = "FULL-CLIENT-USER";

    public final static RolePrincipal webClientUser = new RolePrincipal(WEB_CLIENT_USER);
    public final static RolePrincipal fullClientUser = new RolePrincipal(FULL_CLIENT_USER);

    private final String name;

    public RolePrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
