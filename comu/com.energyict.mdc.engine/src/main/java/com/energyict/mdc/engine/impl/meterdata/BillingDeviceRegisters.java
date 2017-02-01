/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.meterdata;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;

/**
 * Implementation of a Billing Register, collected from a Device.
 * <p/>
 * If data is collected, then a proper collected data with corresponding timeStamps:
 * <ul>
 * <li>{@link #readTime} </li>
 * <li>{@link #fromTime}</li>
 * <li>{@link #toTime}</li>
 * <li>{@link #eventTime}</li>
 * </ul>
 * ... should be set by {@link #setCollectedData(Quantity, String)} and
 * {@link #setCollectedTimeStamps}
 * <p/>
 * If no data could be collected, the a proper {@link com.energyict.mdc.issues.Issue} and {@link com.energyict.mdc.protocol.api.device.data.ResultType}
 * should be returned by calling the {@link #setFailureInformation(com.energyict.mdc.protocol.api.device.data.ResultType, com.energyict.mdc.issues.Issue)}.
 *
 * @author gna
 * @since 4/04/12 - 13:14
 */
public class BillingDeviceRegisters extends DeviceQuantityRegister {

    /**
     * Default constructor
     *
     * @param registerIdentifier the register identifier linked the to readOut data
     */
    public BillingDeviceRegisters(RegisterIdentifier registerIdentifier, ReadingType readingType) {
        super(registerIdentifier, readingType);
    }

}