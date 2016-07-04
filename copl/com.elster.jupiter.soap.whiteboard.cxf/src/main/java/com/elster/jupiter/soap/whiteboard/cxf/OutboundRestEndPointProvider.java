package com.elster.jupiter.soap.whiteboard.cxf;

import javax.xml.ws.Service;

/**
 * Provider allows whiteboard to pick up deployed components that implement this interface.
 * Allows registering Outbound REST services.
 */
public interface OutboundRestEndPointProvider extends EndPointProvider {
    Service get();

    Class getService();
}
