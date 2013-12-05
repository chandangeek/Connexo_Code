package com.energyict.mdc.protocol.device.data;

import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.protocol.device.data.identifiers.RegisterIdentifier;

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
    public Date getReadTime();

    /**
     * Returns the start of the measurement period covered by this reading. Most
     * registers are since the start of measurement. In this case null is
     * returned
     *
     * @return the from date or null
     */
    public Date getFromTime();

    /**
     * Returns the end of the measurement period covered by this reading. For
     * most registers this will be equal to the read time. For billing point
     * register, this is the time of the billing point
     *
     * @return the to time
     */
    public Date getToTime();

    /**
     * Returns the time the metered event took place. For most registers this
     * will be null. For maximum demand registers, this is the interval time the
     * maximum demand was registered
     *
     * @return the event time or null
     */
    public Date getEventTime();

    /**
     * Should provide an identifier to uniquely identify the requested Register.
     *
     * @return the {@link RegisterIdentifier registerIdentifier}
     *         of the BusinessObject which is actionHolder of the request
     */
    public RegisterIdentifier getRegisterIdentifier();

    /**
     * Set the collected timeStamps.<br/>
     * <i>Should be used for MaximumDemand registers</i>
     *
     * @param readTime  the readTime
     * @param fromTime  the fromTime
     * @param toTime    the toTime
     * @param eventTime the eventTime
     */
    public void setCollectedTimeStamps(Date readTime, Date fromTime, Date toTime, Date eventTime);

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
    public void setCollectedTimeStamps(Date readTime, Date fromTime, Date toTime);

    /**
     * Set the collected TimeStamps
     *
     * @param readTime the time the register was read
     */
    public void setReadTime(Date readTime);
}
