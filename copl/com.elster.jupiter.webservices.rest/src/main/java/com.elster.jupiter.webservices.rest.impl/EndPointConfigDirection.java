/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointConfiguration;

/**
 * Created by bvn on 6/8/16.
 */
public enum EndPointConfigDirection {
    Inbound {
        @Override
        public EndPointConfiguration create(EndPointConfigurationInfoFactory factory, EndPointConfigurationInfo info) {
            return factory.createInboundEndPointConfiguration(info);
        }

        @Override
        public EndPointConfiguration applyChanges(EndPointConfigurationInfoFactory factory, EndPointConfiguration endPointConfiguration, EndPointConfigurationInfo info) {
            return factory.updateEndPointConfiguration(((InboundEndPointConfiguration) endPointConfiguration), info);
        }

    },
    Outbound {
        @Override
        public EndPointConfiguration create(EndPointConfigurationInfoFactory factory, EndPointConfigurationInfo info) {
            return factory.createOutboundEndPointConfiguration(info);
        }

        @Override
        public EndPointConfiguration applyChanges(EndPointConfigurationInfoFactory factory, EndPointConfiguration endPointConfiguration, EndPointConfigurationInfo info) {
            return factory.updateEndPointConfiguration(((OutboundEndPointConfiguration) endPointConfiguration), info);
        }
    };

    public abstract EndPointConfiguration create(EndPointConfigurationInfoFactory factory, EndPointConfigurationInfo info);

    public abstract EndPointConfiguration applyChanges(EndPointConfigurationInfoFactory factory, EndPointConfiguration endPointConfiguration, EndPointConfigurationInfo info);
}
