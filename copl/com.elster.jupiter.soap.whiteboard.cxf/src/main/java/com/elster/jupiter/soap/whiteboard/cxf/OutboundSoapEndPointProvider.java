/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf;

import aQute.bnd.annotation.ConsumerType;

import javax.xml.ws.Service;

/**
 * Provider allows whiteboard to pick up deployed components that implement this interface.
 * Allows registering Inbound SOAP services.
 */
@ConsumerType
public interface OutboundSoapEndPointProvider extends EndPointProvider {
    Service get();

    Class getService();
}
