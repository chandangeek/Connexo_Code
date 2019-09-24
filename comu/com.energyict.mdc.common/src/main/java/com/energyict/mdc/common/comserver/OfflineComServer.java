/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.comserver;

/**
 * Models a {@link ComServer} that will run offline,
 * i.e. isolated from the online database.
 * Such ComServers are typically used by engineers in the field
 * that need to communicate with devices they are working on/with.
 * These engineers need to be able to initiate communication
 * with devices but are not expecting devices to initiate
 * communication with their offline ComServer.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-27 (17:40)
 */
public interface OfflineComServer extends ComServer, InboundCapableComServer, OutboundCapableComServer {

    /**
     * Gets the {@link OnlineComServer} that this remote ComServer
     * will talk to when it needs information from the database
     * or has information available that needs to be stored in the database.
     *
     * @return The OnlineComServer
     */
    public OnlineComServer getOnlineComServer();

    public void setOnlineComServer(OnlineComServer onlineComServer);

    interface OfflineComServerBuilder<OCS extends OfflineComServer> extends ComServerBuilder<OCS, OfflineComServerBuilder> {

        public OfflineComServerBuilder onlineComServer(OnlineComServer onlineComServer);

    }

    }
}