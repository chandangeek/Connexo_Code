/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.users.Group;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Created by bvn on 5/4/16.
 */
@ProviderType
public interface EndPointConfigurationService {
    /**
     * Create a new configuration for inbound endpoints
     *
     * @param name           The name of the end point (unique)
     * @param webServiceName The web service that will be offered on the endpoint
     * @param url            The url on which to offer the service
     * @return IncomingEndPointConfigBuilder
     */
    InboundEndPointConfigBuilder newInboundEndPointConfiguration(String name, String webServiceName, String url);

    /**
     * Create a new configuration for outbound endpoints
     *
     * @param name           The name of the end point (unique)
     * @param webServiceName The web service that will be offered as a java service/interface
     * @param url            The url to which the java interface calls will be delegated as a SOAP request
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

    List<EndPointConfiguration> getEndPointConfigurationsForWebService(String webServiceName);

    /**
     * Finder for all known end point configurations. This methods supports paging.
     */
    Finder<EndPointConfiguration> findEndPointConfigurations();

    /**
     * Find endpoints for specified web services names. This methods supports paging.
     */
    Finder<EndPointConfiguration> findEndPointConfigurations(Set<String> webServiceNames);

    /**
     * Streams the end point configurations.
     */
    QueryStream<EndPointConfiguration> streamEndPointConfigurations();

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

    /**
     * Finds and locks a {@link EndPointConfiguration} which is uniquely identified by the given ID and with the given VERSION.
     *
     * @param id      the id of the EndPointConfiguration
     * @param version the version of the EndPointConfiguration
     * @return the EndPointConfiguration or empty if the EndPointConfiguration does not exist
     * or the version of the EndPointConfiguration is not equal to the specified version
     */
    Optional<EndPointConfiguration> findAndLockEndPointConfigurationByIdAndVersion(long id, long version);

    /**
     * An event is sent to all appservers to drop support for the end point configuration
     *
     * @param endPointConfiguration
     */
    void delete(EndPointConfiguration endPointConfiguration);

    @ProviderType
    interface InboundEndPointConfigBuilder {
        InboundEndPointConfigBuilder tracing();

        InboundEndPointConfigBuilder traceFile(String traceFile);

        InboundEndPointConfigBuilder httpCompression();

        InboundEndPointConfigBuilder setAuthenticationMethod(EndPointAuthentication authenticationMethod);

        InboundEndPointConfigBuilder group(Group group);

        InboundEndPointConfigBuilder clientId(String clientId);

        InboundEndPointConfigBuilder clientSecret(String clientSecret);

        InboundEndPointConfigBuilder schemaValidation();

        InboundEndPointConfigBuilder logLevel(LogLevel logLevel);

        InboundEndPointConfigBuilder setPayloadSaveStrategy(PayloadSaveStrategy payloadSaveStrategy);

        InboundEndPointConfigBuilder withProperties(Map<String, Object> properties);

        EndPointConfiguration create();
    }

    @ProviderType
    interface OutboundEndPointConfigBuilder {
        OutboundEndPointConfigBuilder tracing();

        OutboundEndPointConfigBuilder traceFile(String traceFile);

        OutboundEndPointConfigBuilder httpCompression();

        OutboundEndPointConfigBuilder username(String username);

        OutboundEndPointConfigBuilder password(String password);

        OutboundEndPointConfigBuilder schemaValidation();

        OutboundEndPointConfigBuilder logLevel(LogLevel logLevel);

        OutboundEndPointConfigBuilder setPayloadSaveStrategy(PayloadSaveStrategy payloadSaveStrategy);

        EndPointConfiguration create();

        OutboundEndPointConfigBuilder setAuthenticationMethod(EndPointAuthentication id);

        OutboundEndPointConfigBuilder withProperties(Map<String, Object> properties);
    }
}
