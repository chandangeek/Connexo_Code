/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.SetMultimap;

import java.util.Collection;

@ProviderType
public interface OutboundEndPointProvider extends EndPointProvider {
    /**
     * Returns a builder-like {@link RequestSender} interface for sending outbound requests.
     * @param methodName The name of the method in endpoint (port) class to send the request with.
     * @return {@link RequestSender}.
     */
    RequestSender using(String methodName);

    @ProviderType
    interface RequestSender {
        /**
         * @param endPointConfigurations The collection of {@link EndPointConfiguration}s where the request should be sent.
         * @return Self.
         */
        RequestSender toEndpoints(Collection<EndPointConfiguration> endPointConfigurations);

        /**
         * @param endPointConfigurations The array of {@link EndPointConfiguration}s where the request should be sent.
         * @return Self.
         */
        RequestSender toEndpoints(EndPointConfiguration... endPointConfigurations);

        /**
         * Indicates that the request should be sent to all active {@link EndPointConfiguration}s related to the web service in context.
         * @return Self.
         */
        RequestSender toAllEndpoints();

        /**
         * Terminal operation that sends the request.
         * @param request The request to send.
         * @return The map of {@link EndPointConfiguration}s, where the request was sent, to received responses.
         * In case of one-way web service, the result map contains keys mapped to null response.
         * In case of some failure related to some endpoint(s), corresponding web service call occurrence(s) is(are) failed,
         * and related endpoint configuration(s) is(are) NOT included into the result map.
         * @throws RuntimeException In case the request should have been sent to all available endpoint configurations, but no suitable one is found.
         */
        BulkWebServiceCallResult send(Object request);

        /**
         * Terminal operation that sends the raw xml request.
         * @param message The raw xml message to send.
         * @return The map of {@link EndPointConfiguration}s, where the request was sent, to received responses.
         * In case of one-way web service, the result map contains keys mapped to {@code null} response.
         * In case of some failure related to some endpoint(s), corresponding web service call occurrence(s) is(are) failed,
         * and related endpoint configuration(s) is(are) NOT included into the result map.
         * @throws RuntimeException In case the request should have been sent to all endpoint configurations, but no suitable one is found.
         */
        BulkWebServiceCallResult sendRawXml(String message);

        RequestSender withRelatedAttributes(SetMultimap<String,String> values);
    }
}
