/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config;

/**
 * Models a {@link ComServer} that is capable of setting up inbound connections.<br>
 * Will additionally detect the following when polling for changes:
 * <ul>
 * <li>Adding ComPorts</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-27 (17:39)
 */
public interface OutboundCapableComServer extends ComServer, OutboundCapable {
}