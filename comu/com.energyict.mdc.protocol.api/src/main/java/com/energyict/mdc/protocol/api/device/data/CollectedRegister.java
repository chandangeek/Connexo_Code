/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.data;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;

import java.time.Instant;

/**
 * A <code>CollectedRegister</code> identifies a Register (by {@link #getRegisterIdentifier()})
 * and the value collected from a device.
 */
public interface CollectedRegister extends CollectedData {

    /**
     * Get the collected Quantity
     *
     * @return the collected Quantity
     */
    public Quantity getCollectedQuantity();

    /**
     * Get the collected text
     *
     * @return the collected Text
     */
    public String getText();

    /**
     * Returns the time the register reading was performed
     *
     * @return the read time
     */
    public Instant getReadTime();

    /**
     * Returns the start of the measurement period covered by this reading. Most
     * registers are since the start of measurement. In this case null is
     * returned
     *
     * @return the from date or null
     */
    public Instant getFromTime();

    /**
     * Returns the end of the measurement period covered by this reading. For
     * most registers this will be equal to the read time. For billing point
     * register, this is the time of the billing point
     *
     * @return the to time
     */
    public Instant getToTime();

    /**
     * Returns the time the metered event took place. For most registers this
     * will be null. For maximum demand registers, this is the interval time the
     * maximum demand was registered
     *
     * @return the event time or null
     */
    public Instant getEventTime();

    /**
     * Should provide an identifier to uniquely identify the requested Register.
     *
     * @return the {@link RegisterIdentifier registerIdentifier}
     *         of the BusinessObject which is actionHolder of the request
     */
    public RegisterIdentifier getRegisterIdentifier();

    /**
     * Gets the ReadingType of the collected Register
     *
     * @return the ReadingType of the collected Register
     */
    public ReadingType getReadingType();

    /**
     * Indicates whether this is a text register
     *
     * @return true if this is a text register, false otherwise
     */
    public boolean isTextRegister();

    /**
     * Set the collected timeStamps.<br/>
     * <i>Should be used for MaximumDemand registers</i>
     *
     * @param readTime  the readTime
     * @param fromTime  the fromTime
     * @param toTime    the toTime
     * @param eventTime the eventTime
     */
    public void setCollectedTimeStamps(Instant readTime, Instant fromTime, Instant toTime, Instant eventTime);

    /**
     * Set all collected device information, leave the text field empty
     */
    public void setCollectedData(final Quantity collectedQuantity);

    /**
     * Set all collected device information
     *
     * @param collectedQuantity the collected Quantity value
     * @param text              the optional text, collected from the device
     */
    public void setCollectedData(Quantity collectedQuantity, String text);

    /**
     * A register with text only, the quantity is empty
     */
    public void setCollectedData(String text);

    /**
     * Set the collected timeStamps<br/>
     * <i>Should be used for BillingRegisters</i>
     *
     * @param readTime the readTime}
     * @param fromTime the fromTime
     * @param toTime   the toTime
     */
    public void setCollectedTimeStamps(Instant readTime, Instant fromTime, Instant toTime);

    /**
     * Set the collected TimeStamps
     *
     * @param readTime the time the register was read
     */
    public void setReadTime(Instant readTime);

}