package com.energyict.mdc.engine.impl.meterdata;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.engine.impl.commands.store.NoopDeviceCommand;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;

import java.util.Date;

/**
 * Implementation of a Register, collected from a Device.
 * If no data could be collected,
 * then a proper {@link com.energyict.mdc.issues.Issue}
 * and {@link com.energyict.mdc.protocol.api.device.data.ResultType}
 * should be returned.
 *
 * @author gna
 * @since 2/04/12 - 16:35
 */
public abstract class DeviceRegister extends CollectedDeviceData implements CollectedRegister {

    /**
     * The identifier of the referenced Register
     */
    private final RegisterIdentifier registerIdentifier;

    /**
     * The readingType of the Collected Register
     */
    private final ReadingType readingType;

    /**
     * The collected {@link Quantity}
     */
    private Quantity collectedQuantity;

    /**
     * Defines the optional collected reading text
     */
    private String text;

    /**
     * The timeStamp when the data was collected
     */
    private Date readTime;

    /**
     * Defines the start of the measurement period covered by this reading. Most
     * registers are since the start of measurement, in this case fromTime is null
     */
    private Date fromTime;

    /**
     * Defines the end of the measurement period covered by this reading. For
     * most registers this will be equal to the read time. For billing point
     * register, this is the time of the billing point
     */
    private Date toTime;

    /**
     * Defines the time the metered event took place. For most registers this
     * will be null. For maximum demand registers, this is the interval time the
     * maximum demand was registered
     */
    private Date eventTime;

    /**
     * Default constructor
     *
     * @param registerIdentifier the identifier of the Register
     */
    public DeviceRegister(RegisterIdentifier registerIdentifier, ReadingType readingType) {
        super();
        this.registerIdentifier = registerIdentifier;
        this.readingType = readingType;
    }

    @Override
    public boolean isConfiguredIn(DataCollectionConfiguration configuration) {
        return configuration.isConfiguredToCollectRegisterData();
    }

    /**
     * Set all collected device information, leave the text field empty
     */
    public void setCollectedData(final Quantity collectedQuantity) {
        this.collectedQuantity = collectedQuantity;
    }

    /**
     * Set all collected device information
     *
     * @param collectedQuantity the collected Quantity value
     * @param text              the optional text, collected from the device
     */
    @Override
    public void setCollectedData(Quantity collectedQuantity, String text) {
        this.collectedQuantity = collectedQuantity;
        this.text = text;
    }

    /**
     * Set all collected device information
     *
     * @param text the optional text, collected from the device
     */
    public void setCollectedData(final String text) {
        this.text = text;
    }

    public Quantity getCollectedQuantity() {
        return collectedQuantity;
    }

    public String getText() {
        return text;
    }

    public Date getReadTime() {
        return readTime;
    }

    public Date getFromTime() {
        return fromTime;
    }

    public Date getToTime() {
        return toTime;
    }

    public Date getEventTime() {
        return eventTime;
    }

    @Override
    public ReadingType getReadingType() {
        return this.readingType;
    }

    @Override
    public RegisterIdentifier getRegisterIdentifier() {
        return this.registerIdentifier;
    }

    public void setReadTime(final Date readTime) {
        this.readTime = readTime;
    }

    protected void setFromTime(final Date fromTime) {
        this.fromTime = fromTime;
    }

    protected void setToTime(final Date toTime) {
        this.toTime = toTime;
    }

    protected void setEventTime(final Date eventTime) {
        this.eventTime = eventTime;
    }

    /**
     * Set the collected timeStamps<br/>
     * <i>Should be used for BillingRegisters</i>
     *
     * @param readTime the {@link #readTime}
     * @param fromTime the {@link #fromTime}
     * @param toTime   the {@link #toTime}
     */
    @Override
    public void setCollectedTimeStamps(final Date readTime, final Date fromTime, final Date toTime) {
        setReadTime(readTime);
        setFromTime(fromTime);
        setToTime(toTime);
        setEventTime(null);
    }

    /**
     * Set the collected timeStamps.<br/>
     * <i>Should be used for MaximumDemand registers</i>
     *
     * @param readTime  the {@link #readTime}
     * @param fromTime  the {@link #fromTime}
     * @param toTime    the {@link #toTime}
     * @param eventTime the {@link #eventTime}
     */
    @Override
    public void setCollectedTimeStamps(final Date readTime, final Date fromTime, final Date toTime, final Date eventTime) {
        setReadTime(readTime);
        setFromTime(fromTime);
        setToTime(toTime);
        setEventTime(eventTime);
    }

    @Override
    public DeviceCommand toDeviceCommand(IssueService issueService, MeterDataStoreCommand meterDataStoreCommand) {
        return new NoopDeviceCommand();
    }
}
