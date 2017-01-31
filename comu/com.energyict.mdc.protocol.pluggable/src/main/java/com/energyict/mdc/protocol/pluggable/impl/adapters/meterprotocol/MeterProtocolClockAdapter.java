/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol;

import com.energyict.mdc.protocol.api.exceptions.LegacyProtocolException;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.tasks.support.DeviceClockSupport;
import com.energyict.mdc.protocol.pluggable.MessageSeeds;

import java.io.IOException;
import java.util.Date;

/**
 * Adapter between a {@link MeterProtocol} and the {@link DeviceClockSupport}
 *
 * @author gna
 * @since 4/04/12 - 15:51
 */
public class MeterProtocolClockAdapter implements DeviceClockSupport {

    /**
     * The used <code>MeterProtocol</code> for which the adapter is working
     */
    private final MeterProtocol meterProtocol;

    public MeterProtocolClockAdapter(final MeterProtocol meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    /**
     * Write the given new time to the Device.<br>
     * <p/>
     * <b>The adapter will just call the {@link MeterProtocol#setTime()}, so the current SystemTime will be set and NOT the given parameter</b>
     *
     * @param timeToSet the new time to set
     */
    @Override
    public void setTime(final Date timeToSet) {
        //The time will be set based on the systemTime of the device
        try {
            this.meterProtocol.setTime();
        } catch (IOException e) {
            throw new LegacyProtocolException(MessageSeeds.LEGACY_IO, e);
        }
    }

    /**
     * @return the actual time of the Device
     */
    @Override
    public Date getTime() {
        try {
            return this.meterProtocol.getTime();
        } catch (IOException e) {
            throw new LegacyProtocolException(MessageSeeds.LEGACY_IO, e);
        }
    }

}