package com.energyict.mdc.common;

/**
 * Represents the capability to go partially offline, i.e. to be disconnected from the database.
 * Such objects will copy some (depending on the context) or all information from the database before going offline.
 * so they can continue normal business operations without the need to talk to
 * the database for additional information.<br>
 * Note that this may cause recursive calls to other objects that can go offline.
 *
 * Copyrights EnergyICT
 * Date: 14/03/13
 * Time: 14:04
 */
public interface CanGoPartiallyOffline<OT extends Offline, C> extends CanGoOffline<OT> {

    /**
     * Creates an Offline instance copying the information indicated to be necessary by the given context parameter.
     *
     * @param context the context object which holds the key to know wht to copy and what not.
     * @return an offline representation of this object.
     */
    public OT goOffline(C context);

}