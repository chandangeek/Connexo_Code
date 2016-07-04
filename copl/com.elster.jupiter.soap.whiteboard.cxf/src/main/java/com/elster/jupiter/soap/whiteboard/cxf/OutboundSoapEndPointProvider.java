package com.elster.jupiter.soap.whiteboard.cxf;

import javax.xml.ws.Service;

/**
 * Provider allows whiteboard to pick up deployed components that implement this interface.
 * Allows registering Inbound SOAP services.
 */
public interface OutboundSoapEndPointProvider extends EndPointProvider {
    Service get();

    Class getService();
}
