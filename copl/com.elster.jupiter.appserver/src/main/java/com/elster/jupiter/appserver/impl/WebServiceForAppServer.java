/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

/**
 * Created by bvn on 5/19/16.
 */
public interface WebServiceForAppServer {
    EndPointConfiguration getEndPointConfiguration();

    AppServer getAppServer();
}
