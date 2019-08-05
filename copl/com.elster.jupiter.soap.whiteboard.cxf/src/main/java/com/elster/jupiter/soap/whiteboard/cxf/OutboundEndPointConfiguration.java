/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf;

import aQute.bnd.annotation.ProviderType;

/**
 * Created by bvn on 5/4/16.
 */
@ProviderType
public interface OutboundEndPointConfiguration extends EndPointConfiguration {
    void setUsername(String name);

    String getUsername();

    void setPassword(String pass);

    String getPassword();
}
