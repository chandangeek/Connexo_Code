/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.energyict.mdc.common.rest.MapBasedXmlAdapter;

public class EndDeviceSubDomainAdapter extends MapBasedXmlAdapter<EndDeviceSubDomain> {

    public EndDeviceSubDomainAdapter() {
        for (EndDeviceSubDomain subDomain : EndDeviceSubDomain.values()) {
            register(subDomain.name(), subDomain);
        }
    }
}
