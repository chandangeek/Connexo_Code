/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.meterdata;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.engine.impl.commands.store.NoopDeviceCommand;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;

import java.time.Instant;

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
    private Instant readTime;

    /**
     * Defines the start of the measurement period covered by this reading. Most
     * registers are since the start of measurement, in this case fromTime is null
     */
    private Instant fromTime;

    /**
     * Defines the end of the measurement period covered by this reading. For
     * most registers this will be equal to the read time. For billing point
     * register, this is the time of the billing point
     */
    private Instant toTime;

    /**
     * Defines the time the metered event took place. For most registers this
     * will be null. For maximum demand registers, this is the interval time the
     * maximum demand was registered
     */
    private Instant eventTime;

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

    public Instant getReadTime() {
        return readTime;
    }

    public Instant getFromTime() {
        return fromTime;
    }

    public Instant getToTime() {
        return toTime;
    }

    public Instant getEventTime() {
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

    public void setReadTime(final Instant readTime) {
        this.readTime = readTime;
    }

    protected void setFromTime(final Instant fromTime) {
        this.fromTime = fromTime;
    }

    protected void setToTime(final Instant toTime) {
        this.toTime = toTime;
    }

    protected void setEventTime(final Instant eventTime) {
        this.eventTime = eventTime;
    }

    @Override
    public void setCollectedTimeStamps(final Instant readTime, final Instant fromTime, final Instant toTime) {
        setReadTime(readTime);
        setFromTime(fromTime);
        setToTime(toTime);
        setEventTime(null);
    }

    @Override
    public void setCollectedTimeStamps(final Instant readTime, final Instant fromTime, final Instant toTime, final Instant eventTime) {
        setReadTime(readTime);
        setFromTime(fromTime);
        setToTime(toTime);
        setEventTime(eventTime);
    }

    @Override
    public DeviceCommand toDeviceCommand(MeterDataStoreCommand meterDataStoreCommand, DeviceCommand.ServiceProvider serviceProvider) {
        return new NoopDeviceCommand();
    }

}