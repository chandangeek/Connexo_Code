package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.EndPointConfiguration;

/**
 * Implementors will be self contained Endpoint managers: they contain all intelligence to publish/stop the actual service as endpoint
 * Inbound endpoints will upon publishing offer a http service (e.g. SOAP) that can be externally reached. Outgoing endpoints
 * will offer an OSGi service (java service/interface), wrapping the SOAP related code to call an external endpoint (external to connexo)
 */
public interface ManagedEndpoint {
    /**
     * Publishes an endpoint as defined in the end point configuration. There can only be one endpoint published for a
     * configuration at a time (on a single appserver).
     *
     * @param endPointConfiguration
     */
    void publish(EndPointConfiguration endPointConfiguration);

    /**
     * Stops the endpoint. The endpoint will no longer be published
     */
    void stop();

    /**
     * @return true if the managed endpoint is an inbound endpoint, false otherwise
     */
    boolean isInbound();
}
