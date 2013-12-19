package com.energyict.mdc.engine.model;

import com.energyict.mdc.shadow.servers.RemoteComServerShadow;

/**
 * Models a {@link ComServer} that installed in the DMZ and is not
 * allowed to communicate directly with the online database for security reasons.
 * All database communication will go through a dedicated and secure API
 * that is http based. This communication will be sent to an actual {@link OnlineComServer}.
 * This http based API can additionally be setup to require username/password authentication.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-27 (17:44)
 */
public interface RemoteComServer extends ComServer<RemoteComServerShadow>, InboundCapableComServer<RemoteComServerShadow>, OutboundCapableComServer<RemoteComServerShadow> {

    /**
     * Gets the URI on which the event registration mechanism runs.
     *
     * @return The URI or <code>null</code> if this ComServer does not support event registration
     */
    public String getEventRegistrationUri ();

    /**
     * Tests if the URI for the event registration mechanism
     * is the default one or not.
     *
     * @return <code>true</code> iff the URI for the event registration mechanism is the default one
     */
    public boolean usesDefaultEventRegistrationUri ();

    /**
     * Gets the {@link OnlineComServer} that this remote ComServer
     * will talk to when it needs information from the database
     * or has information available that needs to be stored in the database.
     *
     * @return The OnlineComServer
     */
    public OnlineComServer getOnlineComServer();

    /**
     * Gets the username that is required to authenticate the usage
     * of the http based query API to communication with the related {@link OnlineComServer}.
     *
     * @return The username
     */
    public String getQueryAPIUsername();

    /**
     * Gets the password that is required to authenticate the usage
     * of the http based query API to communication with the related {@link OnlineComServer}.
     *
     * @return The password
     */
    public String getQueryAPIPassword();

}