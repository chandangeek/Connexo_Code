package com.elster.jupiter.soap.whiteboard.cxf;

/**
 * Provider allows whiteboard to pick up deployed components that implement this interface.
 * Allows registering Inbound REST services.
 */
public interface InboundRestEndPointProvider extends EndPointProvider {
    Object get();
}
