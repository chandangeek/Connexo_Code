/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config;

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
public interface RemoteComServer extends ComServer, InboundCapableComServer, OutboundCapableComServer {

    public String getServerName();

    public void setServerName(String serverName);

    /**
     * Gets the URI on which the event registration mechanism runs.
     *
     * @return The URI
     */
    public String getEventRegistrationUri();

    public int getEventRegistrationPort();

    public void setEventRegistrationPort(int eventRegistrationPort);

    /**
     * Gets the URI that returns the status information of this ComServer.
     *
     * @return The URI
     */
    public String getStatusUri();

    public int getStatusPort();

    public void setStatusPort(int statusPort);

    /**
     * Gets the {@link OnlineComServer} that this remote ComServer
     * will talk to when it needs information from the database
     * or has information available that needs to be stored in the database.
     *
     * @return The OnlineComServer
     */
    public OnlineComServer getOnlineComServer();

    public void setOnlineComServer(OnlineComServer onlineComServer);

    interface RemoteComServerBuilder<CS extends RemoteComServer> extends ComServerBuilder<CS, RemoteComServerBuilder> {
        RemoteComServerBuilder onlineComServer(OnlineComServer onlineComServer);

        RemoteComServerBuilder serverName(String serverName);

        RemoteComServerBuilder eventRegistrationPort(int eventRegistrationPort);

        RemoteComServerBuilder statusPort(int statusPort);
    }

}