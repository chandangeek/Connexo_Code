/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.modbus.enerdis.cdt;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.protocols.mdc.inbound.rtuplusserver.DiscoverResult;
import com.energyict.protocols.mdc.inbound.rtuplusserver.DiscoverTools;

import javax.inject.Inject;
import java.io.IOException;
import java.time.Clock;
import java.util.Date;


/**
 * RecDigit Cct meter is a pulse counter.
 */

public class RecDigitCdtE extends RecDigitCdt {

    @Override
    public String getProtocolDescription() {
        return "Enerdis Recdigit CDT E Modbus";
    }

    private final Clock clock;

    @Inject
    public RecDigitCdtE(PropertySpecService propertySpecService, Clock clock) {
        super(propertySpecService);
        this.clock = clock;
    }

    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactoryCdtE(this));
    }

    public int getProfileInterval() throws IOException {
        throw new UnsupportedException();
    }

    /* meter does not have the time */
    public Date getTime() throws IOException {
        return Date.from(this.clock.instant());
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    public DiscoverResult discover(DiscoverTools discoverTools) {
        return null;
    }

}
