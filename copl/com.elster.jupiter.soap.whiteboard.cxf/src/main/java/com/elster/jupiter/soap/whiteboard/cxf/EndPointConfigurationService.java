package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.domain.util.Finder;

import java.util.Optional;

/**
 * Created by bvn on 5/4/16.
 */
public interface EndPointConfigurationService {
    /**
     * Create a new configuration for inbound endpoints
     *
     * @param name The name of the end point (unique)
     * @param webServiceName The web service that will be offered on the endpoint
     * @param url The url on which to offer the service
     * @return IncomingEndPointConfigBuilder
     */
    InboundEndPointConfigBuilder newInboundEndPointConfiguration(String name, String webServiceName, String url);

    /**
     * Create a new configuration for outbound endpoints
     *
     * @param name The name of the end point (unique)
     * @param webServiceName The web service that will be offered as a java service/interface
     * @param url The url to which the java interface calls will be delegated as a SOAP request
     * @return OutgoingEndPointConfigBuilder
     */
    OutboundEndPointConfigBuilder newOutboundEndPointConfiguration(String name, String webServiceName, String url);

    /**
     * Gets an existing end point configuration by name
     *
     * @param name The end point config name
     * @return The endpoint config by that name, or Empty if none found
     */
    Optional<EndPointConfiguration> getEndPointConfiguration(String name);

    /**
     * Gets an existing end point configuration by db id
     *
     * @param id The end point config id
     * @return The endpoint config by that id, or Empty if none found
     */
    Optional<EndPointConfiguration> getEndPointConfiguration(long id);

    Finder<EndPointConfiguration> findEndPointConfigurations();

    /**
     * The endPointConfiguration will be changed to 'active' and a system wide event will be sent notifying all appservers
     * the end point config has changed
     *
     * @param endPointConfiguration The EndPointConfiguration to activate
     */
    void activate(EndPointConfiguration endPointConfiguration);

    /**
     * The endPointConfiguration will be changed to 'inactive' and a system wide event will be sent notifying all appservers
     * the end point config has changed
     *
     * @param endPointConfiguration The EndPointConfiguration to de-activate
     */
    void deactivate(EndPointConfiguration endPointConfiguration);

    interface InboundEndPointConfigBuilder {
        InboundEndPointConfigBuilder tracing();

        InboundEndPointConfigBuilder httpCompression();

        InboundEndPointConfigBuilder authenticated();

        InboundEndPointConfigBuilder schemaValidation();

        InboundEndPointConfigBuilder logLevel(LogLevel logLevel);

        EndPointConfiguration create();
    }

    interface OutboundEndPointConfigBuilder {
        OutboundEndPointConfigBuilder tracing();

        OutboundEndPointConfigBuilder httpCompression();

        OutboundEndPointConfigBuilder username(String username);

        OutboundEndPointConfigBuilder password(String password);

        OutboundEndPointConfigBuilder schemaValidation();

        OutboundEndPointConfigBuilder logLevel(LogLevel logLevel);

        EndPointConfiguration create();
    }
}
