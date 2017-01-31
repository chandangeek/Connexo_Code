/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

/**
 * Created by bvn on 4/29/16.
 */
@ProviderType
public interface WebServicesService {
    public static final String COMPONENT_NAME = "WSS";


    /**
     * Get a list of registered web services.
     *
     * @return
     */
    List<WebService> getWebServices();

    /**
     * The webservice for which the configuration is passed as argument will be created (EndPoint for Soap Inbound, ServiceRegistration for outbound, Application for rest inbound)
     *
     * @param endPointConfiguration
     */
    void publishEndPoint(EndPointConfiguration endPointConfiguration);

    /**
     * The webservice for which the configuration is passed as argument will be torn down and no longer available
     *
     * @param endPointConfiguration
     */
    void removeEndPoint(EndPointConfiguration endPointConfiguration);

    /**
     * Returns true is the configured webservice is currently running on the current appserver
     *
     * @param endPointConfiguration
     * @return true is web service is running/published, false otherwise
     */
    boolean isPublished(EndPointConfiguration endPointConfiguration);

    /**
     * Get a list of end points configuration names that are currently published/running
     *
     * @return
     */
    List<EndPointConfiguration> getPublishedEndPoints();

    boolean isInbound(String webServiceName);

    /**
     * Find a web service by the given name. Web services are registered through the soap whiteboard
     *
     * @param webServiceName
     * @return Empty is no such web service has been registered
     */
    Optional<WebService> getWebService(String webServiceName);

    /**
     * All endpoints created for the local appserver will be removed
     */
    void removeAllEndPoints();
}
