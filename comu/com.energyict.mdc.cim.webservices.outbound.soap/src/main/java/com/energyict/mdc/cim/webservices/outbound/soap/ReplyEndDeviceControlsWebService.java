/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface ReplyEndDeviceControlsWebService {
    String NAME = "CIM ReplyEndDeviceControls";

    void call();
}
