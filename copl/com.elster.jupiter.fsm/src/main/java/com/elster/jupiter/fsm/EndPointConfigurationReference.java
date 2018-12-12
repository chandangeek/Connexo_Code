/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

import aQute.bnd.annotation.ProviderType;

/**
 * References an externally end point configuration.
 */
@ProviderType
public interface EndPointConfigurationReference {

    EndPointConfiguration getStateChangeEndPointConfiguration();
}