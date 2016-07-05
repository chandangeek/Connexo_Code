package com.elster.jupiter.soap.whiteboard.cxf;

import javax.ws.rs.client.WebTarget;

/**
 * Provider allows whiteboard to pick up deployed components that implement this interface.
 * Allows registering Outbound REST services.
 */
public interface OutboundRestEndPointProvider<S> extends EndPointProvider {
    S get(WebTarget client);

    Class<S> getService();
}
