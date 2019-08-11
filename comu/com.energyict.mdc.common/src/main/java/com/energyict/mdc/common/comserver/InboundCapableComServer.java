/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.comserver;

/**
 * Models a {@link ComServer} that is capable of accepting inbound connections.<br>
 * Will additionally detect the following when polling for changes:
 * <ul>
 * <li>Adding ComPorts</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-27 (17:39)
 */
public interface InboundCapableComServer extends ComServer, InboundCapable {
}