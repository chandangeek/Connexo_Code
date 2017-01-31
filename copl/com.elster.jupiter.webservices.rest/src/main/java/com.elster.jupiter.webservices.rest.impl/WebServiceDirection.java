/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointConfiguration;

/**
 * Created by bvn on 6/10/16.
 */
public enum WebServiceDirection implements TranslationKey {
    INBOUND("Inbound") {
        @Override
        public EndPointConfiguration create(EndPointConfigurationInfoFactory factory, EndPointConfigurationInfo info) {
            return factory.createInboundEndPointConfiguration(info);
        }

        @Override
        public EndPointConfiguration applyChanges(EndPointConfigurationInfoFactory factory, EndPointConfiguration endPointConfiguration, EndPointConfigurationInfo info) {
            return factory.updateEndPointConfiguration(((InboundEndPointConfiguration) endPointConfiguration), info);
        }


    }, OUTBOUND("Outbound") {
        @Override
        public EndPointConfiguration create(EndPointConfigurationInfoFactory factory, EndPointConfigurationInfo info) {
            return factory.createOutboundEndPointConfiguration(info);
        }

        @Override
        public EndPointConfiguration applyChanges(EndPointConfigurationInfoFactory factory, EndPointConfiguration endPointConfiguration, EndPointConfigurationInfo info) {
            return factory.updateEndPointConfiguration(((OutboundEndPointConfiguration) endPointConfiguration), info);
        }

    };

    private String name;

    WebServiceDirection(String name) {
        this.name = name;
    }

    public String getDisplayName(Thesaurus thesaurus) {
        return thesaurus.getString(getTranslationKey(this), this.getDefaultFormat());
    }

    public static String getTranslationKey(WebServiceDirection ll) {
        return "webservices.direction." + ll.name().toLowerCase();
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getKey() {
        return name();
    }

    @Override
    public String getDefaultFormat() {
        return toString();
    }

    public abstract EndPointConfiguration create(EndPointConfigurationInfoFactory factory, EndPointConfigurationInfo info);

    public abstract EndPointConfiguration applyChanges(EndPointConfigurationInfoFactory factory, EndPointConfiguration endPointConfiguration, EndPointConfigurationInfo info);

}
