/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.energyict.mdc.common.rest.MapBasedXmlAdapter;

public class EndDeviceDomainAdapter extends MapBasedXmlAdapter<EndDeviceDomain> {

    public EndDeviceDomainAdapter() {
        for (EndDeviceDomain domain : EndDeviceDomain.values()) {
            register(domain.name(), domain);
        }
    }
}
