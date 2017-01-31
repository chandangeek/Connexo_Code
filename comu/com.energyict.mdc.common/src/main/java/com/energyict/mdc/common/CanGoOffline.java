/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common;

/**
 * Represents the capability to go offline, i.e. to be disconnected from the database.
 * Such objects will copy all information from the database before going offline
 * so they can continue normal business operations without the need to talk to
 * the database for additional information.<br>
 * Note that this may cause recursive calls to other objects that can go offline.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-09 (09:25)
 */
public interface CanGoOffline<OT extends Offline> {

    /**
     * Triggers the capability to go offline and will copy all information
     * from the database into memory so that normal business operations can continue.<br>
     * Note that this may cause recursive calls to other objects that can go offline.
     *
     * @return The Offline version of this object
     */
    public OT goOffline();

}