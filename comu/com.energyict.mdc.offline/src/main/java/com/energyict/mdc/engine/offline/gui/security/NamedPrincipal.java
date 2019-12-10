package com.energyict.mdc.engine.offline.gui.security;

import com.energyict.mdc.common.ApplicationException;

import java.io.Serializable;
import java.security.Principal;

/**
 * A security principal is an entity that can be positively identified and
 * verified via a technique known as Authentication (wikipedia.org).
 * <p/>
 * This Principal implementation represents an EIServer user.
 */

public class NamedPrincipal implements Principal, Serializable {

    /**
     * login name of user
     */
    private String name;

    /**
     * @param name login name of user
     */
    public NamedPrincipal(String name) {
        if (name == null) {
            throw new ApplicationException("name may not be null");
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return "NamedPrincipal [ name=" + name + " ]";
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (this == o) {
            return true;
        }

        if (!(o instanceof NamedPrincipal)) {
            return false;
        }
        NamedPrincipal that = (NamedPrincipal) o;

        if (this.getName().equals(that.getName())) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return name.hashCode();
    }

}
