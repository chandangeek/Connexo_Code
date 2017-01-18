package com.energyict.mdc.upl.meterdata;

import com.energyict.cbo.Quantity;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;

import java.util.Date;

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
    Quantity getCollectedQuantity();

    /**
     * Get the collected text
     *
     * @return the collected Text
     */
    String getText();

    /**
     * Returns the time the register reading was performed
     *
     * @return the read time
     */
    Date getReadTime();

    /**
     * Set the collected TimeStamps
     *
     * @param readTime the time the register was read
     */
    void setReadTime(Date readTime);

    /**
     * Returns the start of the measurement period covered by this reading. Most
     * registers are since the start of measurement. In this case null is
     * returned
     *
     * @return the from date or null
     */
    Date getFromTime();

    /**
     * Returns the end of the measurement period covered by this reading. For
     * most registers this will be equal to the read time. For billing point
     * register, this is the time of the billing point
     *
     * @return the to time
     */
    Date getToTime();

    /**
     * Returns the time the metered event took place. For most registers this
     * will be null. For maximum demand registers, this is the interval time the
     * maximum demand was registered
     *
     * @return the event time or null
     */
    Date getEventTime();

    /**
     * Should provide an identifier to uniquely identify the requested Register.
     *
     * @return the {@link RegisterIdentifier registerIdentifier}
     * of the BusinessObject which is actionHolder of the request
     */
    RegisterIdentifier getRegisterIdentifier();

    /**
     * Indicates whether this is a text register
     *
     * @return true if this is a text register, false otherwise
     */
    boolean isTextRegister();

    /**
     * Set the collected timeStamps.<br/>
     * <i>Should be used for MaximumDemand registers</i>
     *
     * @param readTime  the readTime
     * @param fromTime  the fromTime
     * @param toTime    the toTime
     * @param eventTime the eventTime
     */
    void setCollectedTimeStamps(Date readTime, Date fromTime, Date toTime, Date eventTime);

    /**
     * Set all collected device information, leave the text field empty
     */
    void setCollectedData(final Quantity collectedQuantity);

    /**
     * Set all collected device information
     *
     * @param collectedQuantity the collected Quantity value
     * @param text              the optional text, collected from the device
     */
    void setCollectedData(Quantity collectedQuantity, String text);

    /**
     * A register with text only, the quantity is empty
     */
    void setCollectedData(String text);

    /**
     * Set the collected timeStamps<br/>
     * <i>Should be used for BillingRegisters</i>
     *
     * @param readTime the readTime}
     * @param fromTime the fromTime
     * @param toTime   the toTime
     */
    void setCollectedTimeStamps(Date readTime, Date fromTime, Date toTime);

}