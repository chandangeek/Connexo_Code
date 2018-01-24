/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf;

import aQute.bnd.annotation.ProviderType;

/**
 * Models properties created for an end point
 */
@ProviderType
public interface EndPointProperty {

    EndPointConfiguration getEndPointConfiguration();

    String getName();

    Object getValue();

    void setValue(Object value);

}