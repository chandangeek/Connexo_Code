package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointConfiguration;

import javax.inject.Inject;
import java.time.Clock;

/**
 * Created by bvn on 5/4/16.
 */
public final class OutboundEndPointConfigurationImpl extends EndPointConfigurationImpl implements OutboundEndPointConfiguration {

    private String username;
    private String password;

    @Inject
    public OutboundEndPointConfigurationImpl(DataModel dataModel, Clock clock) {
        super(clock, dataModel);
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }
}
