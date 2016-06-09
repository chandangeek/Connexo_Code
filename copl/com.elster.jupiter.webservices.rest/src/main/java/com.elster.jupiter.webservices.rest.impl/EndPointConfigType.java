package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

/**
 * Created by bvn on 6/8/16.
 */
public enum EndPointConfigType {
    Inbound {
        @Override
        public EndPointConfiguration create(EndPointConfigurationInfoFactory factory, EndPointConfigurationInfo info) {
            return factory.createInboundEndPointConfiguration(info);
        }
    },
    Outbound {
        @Override
        public EndPointConfiguration create(EndPointConfigurationInfoFactory factory, EndPointConfigurationInfo info) {
            return factory.createOutboundEndPointConfiguration(info);
        }
    };

    public abstract EndPointConfiguration create(EndPointConfigurationInfoFactory factory, EndPointConfigurationInfo info);
}
