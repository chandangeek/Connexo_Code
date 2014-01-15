package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol;

import com.energyict.mdc.protocol.api.exceptions.LegacyProtocolException;
import com.energyict.mdc.protocol.api.tasks.support.DeviceClockSupport;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;

import java.io.IOException;
import java.util.Date;

/**
 * Adapter between a {@link SmartMeterProtocol} and a {@link DeviceClockSupport}
 *
 * @author gna
 * @since 5/04/12 - 13:27
 */
public class SmartMeterProtocolClockAdapter implements DeviceClockSupport {

    private final SmartMeterProtocol smartMeterProtocol;

    public SmartMeterProtocolClockAdapter(final SmartMeterProtocol smartMeterProtocol) {
        this.smartMeterProtocol = smartMeterProtocol;
    }

    /**
     * Write the given new time to the Device.
     *
     * @param timeToSet the new time to set
     */
    @Override
    public void setTime(final Date timeToSet) {
        try {
            this.smartMeterProtocol.setTime(timeToSet);
        } catch (IOException e) {
            throw new LegacyProtocolException(e);
        }
    }

    /**
     * @return the actual time of the Device
     */
    @Override
    public Date getTime() {
        try {
            return this.smartMeterProtocol.getTime();
        } catch (IOException e) {
            throw new LegacyProtocolException(e);
        }
    }
}
