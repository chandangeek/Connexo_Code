package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.cbo.Quantity;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;

/**
 * Implementation of a Maximum Demand Register, collected from a Device.
 * <p>
 * If data is collected, then a proper collected data with corresponding timeStamps:
 * <ul>
 * <li>{@link #readTime} </li>
 * <li>{@link #fromTime}</li>
 * <li>{@link #toTime}</li>
 * <li>{@link #eventTime}</li>
 * </ul>
 * ... should be set by {@link #setCollectedData(Quantity, String)} and
 * {@link #setCollectedTimeStamps}
 * <p>
 * If no data could be collected, the a proper {@link com.energyict.mdc.upl.issue.Issue} and {@link com.energyict.mdc.upl.meterdata.ResultType}
 * should be returned by calling the {@link #setFailureInformation(com.energyict.mdc.upl.meterdata.ResultType, com.energyict.mdc.upl.issue.Issue)}.
 *
 * @author gna
 * @since 4/04/12 - 13:08
 */
public class MaximumDemandDeviceRegister extends DeviceQuantityRegister {

    /**
     * Default constructor
     *
     * @param registerIdentifier the register identifier linked the to readOut data
     */
    public MaximumDemandDeviceRegister(RegisterIdentifier registerIdentifier) {
        super(registerIdentifier);
    }
}