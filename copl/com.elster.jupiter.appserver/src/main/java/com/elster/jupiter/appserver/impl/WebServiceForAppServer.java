package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.soap.whiteboard.EndPointConfiguration;

/**
 * Created by bvn on 5/19/16.
 */
public interface WebServiceForAppServer {
    EndPointConfiguration getEndPointConfiguration();

    AppServer getAppServer();
}
