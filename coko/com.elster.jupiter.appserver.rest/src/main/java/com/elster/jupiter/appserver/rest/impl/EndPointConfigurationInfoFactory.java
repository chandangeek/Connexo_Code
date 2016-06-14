package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

/**
 * Created by bvn on 6/14/16.
 */
public class EndPointConfigurationInfoFactory {

    public EndPointConfigurationInfo from(EndPointConfiguration endPointConfiguration) {
        EndPointConfigurationInfo info = new EndPointConfigurationInfo();
        info.name = endPointConfiguration.getName();
        info.webServiceName = endPointConfiguration.getWebServiceName();
        info.isActive = endPointConfiguration.isActive();
        return info;
    }
}
