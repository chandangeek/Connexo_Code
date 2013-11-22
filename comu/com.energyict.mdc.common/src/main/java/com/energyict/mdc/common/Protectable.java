/*
 * Protectable.java
 *
 * Created on 26 mei 2003, 11:41
 */

package com.energyict.mdc.common;

import com.energyict.mdc.common.UserAction;

/**
 * Interface Proctectable represents a protectable businessobject.
 *
 * @author Karel
 */
public interface Protectable {

    /**
     * test whether the given action is authorized for the current user.
     *
     * @param action action to perform
     * @return true if the current user is authorized to execute the requested action,
     *         false otherwise.
     */
    boolean isAuthorized(UserAction action);
}
