package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.NoopDeviceCommand;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;

import java.util.Date;

/**
 * Implementation of a standard Register, collected from a Device.
 * <p/>
 * If data is collected, then a proper collected data <b>AND</b> {@link #readTime} should be set by
 * {@link #setCollectedData(Quantity, String)} and {@link #setReadTime(java.util.Date)}
 * <p/>
 * If no data could be collected, the a proper {@link com.energyict.mdc.issues.Issue} and {@link com.energyict.mdc.protocol.api.device.data.ResultType}
 * should be returned by calling the {@link #setFailureInformation(com.energyict.mdc.protocol.api.device.data.ResultType, com.energyict.mdc.issues.Issue)}.
 *
 * @author gna
 * @since 4/04/12 - 12:08
 */
public class DefaultDeviceRegister extends DeviceRegister {

    /**
     * Default constructor
     *
     * @param registerIdentifier the register identifier linked the to readOut data
     */
    public DefaultDeviceRegister(RegisterIdentifier registerIdentifier) {
        super(registerIdentifier);
    }

    /**
     * Set the time the reading was recorded
     *
     * @param readTime the time the reading was recorded
     */
    public void setReadTime(Date readTime) {
        super.setReadTime(readTime);
        super.setToTime(readTime);
        super.setFromTime(null);
        super.setEventTime(null);
    }

    @Override
    public DeviceCommand toDeviceCommand(IssueService issueService) {
        return new NoopDeviceCommand();

    }
}
