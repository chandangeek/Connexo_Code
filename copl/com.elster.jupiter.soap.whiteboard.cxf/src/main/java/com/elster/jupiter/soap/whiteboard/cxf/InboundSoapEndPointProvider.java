package com.elster.jupiter.soap.whiteboard.cxf;

/**
 * Provider allows whiteboard to pick up deployed components that implement this interface.
 * Allows registering Inbound SOAP services.
 */
public interface InboundSoapEndPointProvider extends EndPointProvider {
    Object get();
}
