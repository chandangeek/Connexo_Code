/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config;

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
public interface OfflineComServer extends ComServer, OutboundCapableComServer {

}