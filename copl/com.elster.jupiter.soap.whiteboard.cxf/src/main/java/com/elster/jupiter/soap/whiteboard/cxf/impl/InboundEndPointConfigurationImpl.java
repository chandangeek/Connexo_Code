package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointAuthentication;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;

import javax.inject.Inject;
import java.time.Clock;

/**
 * Created by bvn on 5/4/16.
 */
public final class InboundEndPointConfigurationImpl extends EndPointConfigurationImpl implements InboundEndPointConfiguration {

    private EndPointAuthentication authenticationMethod;

    @Inject
    public InboundEndPointConfigurationImpl(DataModel dataModel, Clock clock) {
        super(clock, dataModel);
    }

    @Override
    public EndPointAuthentication getAuthenticationMethod() {
        return authenticationMethod;
    }

    @Override
    public void setAuthenticationMethod(EndPointAuthentication authenticated) {
        this.authenticationMethod = authenticated;
    }
}
