/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf;

import aQute.bnd.annotation.ConsumerType;

import javax.ws.rs.core.Application;

/**
 * Provider allows whiteboard to pick up deployed components that implement this interface.
 * Allows registering Inbound REST services.
 */
@ConsumerType
public interface InboundRestEndPointProvider extends EndPointProvider {
    Application get();
}
