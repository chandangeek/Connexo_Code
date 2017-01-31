/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf;

import aQute.bnd.annotation.ConsumerType;

import javax.ws.rs.client.WebTarget;

/**
 * Provider allows whiteboard to pick up deployed components that implement this interface.
 * Allows registering Outbound REST services.
 */
@ConsumerType
public interface OutboundRestEndPointProvider<S> extends EndPointProvider {
    S get(WebTarget client);

    Class<S> getService();
}
