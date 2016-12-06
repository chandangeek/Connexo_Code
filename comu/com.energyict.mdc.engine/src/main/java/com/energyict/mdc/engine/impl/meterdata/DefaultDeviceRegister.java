package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.cbo.Quantity;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;

import java.time.Instant;

/**
 * Implementation of a standard Register, collected from a Device.
 * <p>
 * If data is collected, then a proper collected data <b>AND</b> {@link #readTime} should be set by
 * {@link #setCollectedData(Quantity, String)} and {@link #setReadTime(Instant)}
 * <p>
 * If no data could be collected, the a proper {@link com.energyict.mdc.upl.tasks.Issue} and {@link com.energyict.mdc.upl.meterdata.ResultType}
 * should be returned by calling the {@link #setFailureInformation(com.energyict.mdc.upl.meterdata.ResultType, com.energyict.mdc.upl.tasks.Issue)}.
 *
 * @author gna
 * @since 4/04/12 - 12:08
 */
public class DefaultDeviceRegister extends DeviceQuantityRegister {

    /**
     * Default constructor
     *
     * @param registerIdentifier the register identifier linked the to readOut data
     */
    public DefaultDeviceRegister(RegisterIdentifier registerIdentifier, String readingTypeMRID) {
        super(registerIdentifier, readingTypeMRID);
    }

    @Override
    public void setReadTime(Instant readTime) {
        super.setReadTime(readTime);
        super.setToTime(readTime);
        super.setFromTime(null);
        super.setEventTime(null);
    }
}