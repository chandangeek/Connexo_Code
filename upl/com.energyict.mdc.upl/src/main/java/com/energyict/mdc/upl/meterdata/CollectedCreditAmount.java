package com.energyict.mdc.upl.meterdata;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * A CollectedCreditAmount identifies the credit amount on a device and its type
 *
 * @author dborisov dmitriy.borisov@orioninc.com
 * @since 31/04/2021 - 12:00
 */
public interface CollectedCreditAmount extends CollectedData {

    /**
     * @return the DeviceIdentifier for which these command is applicable
     */
    DeviceIdentifier getDeviceIdentifier();

    /**
     * @return the current type of the credit.
     * An empty string can be returned in case the device doesn't support credit functionality
     */
    public String getCreditType();

    public void setCreditType(String creditType);

    /**
     * @return the current status of the device breaker. An empty optional can be returned in case the device doesn't support credit functionality
     */
    Optional<BigDecimal> getCreditAmount();

    void setCreditAmount(BigDecimal creditAmount);

    /**
     * Setter which can be used to inject the {@link DataCollectionConfiguration dataCollectionConfiguration}
     */
    void setDataCollectionConfiguration(DataCollectionConfiguration configuration);
}