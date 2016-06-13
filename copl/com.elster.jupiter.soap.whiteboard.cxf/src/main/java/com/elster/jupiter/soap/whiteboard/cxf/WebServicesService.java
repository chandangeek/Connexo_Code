package com.elster.jupiter.soap.whiteboard.cxf;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

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
     * The webservice for which the configuration is passed as argument will be created (EndPoint for Inbound, ServiceRegistration for outbound)
     * This method is not to be used externally, should move to IWebServicesService
     *
     * @param endPointConfiguration
     */
    void publishEndPoint(EndPointConfiguration endPointConfiguration);

    /**
     * The webservice for which the configuration is passed as argument will be torn down and no longer available (EndPoint for Inbound, ServiceRegistration for outbound)
     * This method is not to be used externally, should move to IWebServicesService
     *
     * @param endPointConfiguration
     */
    void removeEndPoint(EndPointConfiguration endPointConfiguration);

    /**
     * Returns true is the configured webservice is currently running on the current appserver
     * This method is not to be used externally, should move to IWebServicesService
     *
     * @param endPointConfiguration
     * @return true is web service is running/published, false otherwise
     */
    boolean isPublished(EndPointConfiguration endPointConfiguration);

    /**
     * Get a list of configurations for web services. Each such configuration is know as EndPointConfiguration
     *
     * @return
     */
    List<String> getEndPoints();

    boolean isInbound(String webServiceName);
}
