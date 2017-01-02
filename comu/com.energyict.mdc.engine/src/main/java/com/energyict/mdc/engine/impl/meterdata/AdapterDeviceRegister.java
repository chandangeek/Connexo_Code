package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.cbo.Quantity;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;

import java.time.Instant;

/**
 * Implementation of a Register, collected from a Device, <b>only</b> used by the ProtocolAdapters to map
 * older {@link com.energyict.mdc.protocol.api.device.data.RegisterValue}s to a {@link DeviceRegister}.
 * <p>
 * If data is collected, then a proper collected data <b>AND</b> {@link #readTime} should be set by
 * {@link #setCollectedData(Quantity, String)} and {@link #setReadTime(Instant)}.
 * <p>
 * If no data could be collected, then a proper {@link com.energyict.mdc.upl.issue.Issue} and {@link com.energyict.mdc.upl.meterdata.ResultType}
 * should be returned by calling the {@link #setFailureInformation(com.energyict.mdc.upl.meterdata.ResultType, com.energyict.mdc.upl.issue.Issue)}.
 *
 * @author gna
 * @since 4/04/12 - 15:05
 */
public class AdapterDeviceRegister extends MaximumDemandDeviceRegister {

    /**
     * Default constructor
     *
     * @param registerIdentifier the register identifier linked the to readOut data
     */
    public AdapterDeviceRegister(RegisterIdentifier registerIdentifier, String readingTypeMRID) {
        super(registerIdentifier, readingTypeMRID);
    }
}