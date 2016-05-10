package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.EndPointConfiguration;

/**
 * Implementors will be self contained Endpoint managers: they contain all intelligence to publish/stop the actual service as endpoint
 * Created by bvn on 5/10/16.
 */
public interface ManagedEndpoint {
    void publish(EndPointConfiguration endPointConfiguration);

    void stop();
}
