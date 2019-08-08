/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf;

import aQute.bnd.annotation.ProviderType;

/**
 * Created by bvn on 6/6/16.
 */
@ProviderType
public interface WebService {
    String getName();

    boolean isInbound();

    WebServiceProtocol getProtocol();

    String getApplicationName();
}
