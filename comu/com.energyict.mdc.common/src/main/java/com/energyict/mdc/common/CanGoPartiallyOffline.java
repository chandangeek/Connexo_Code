/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common;

public interface CanGoPartiallyOffline<OT extends Offline, C> extends CanGoOffline<OT> {

    /**
     * Creates an Offline instance copying the information indicated to be necessary by the given context parameter.
     *
     * @param context the context object which holds the key to know wht to copy and what not.
     * @return an offline representation of this object.
     */
    public OT goOffline(C context);

}