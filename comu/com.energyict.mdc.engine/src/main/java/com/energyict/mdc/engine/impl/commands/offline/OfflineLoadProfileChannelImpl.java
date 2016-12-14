package com.energyict.mdc.engine.impl.commands.offline;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.cbo.Unit;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfileChannel;
import com.energyict.obis.ObisCode;

import java.math.BigDecimal;


/**
 * An offline implementation version of a {@link Channel} mainly containing information which is relevant to use at offline-time.
 *
 * @author gna
 * @since 30/05/12 - 9:55
 */
public class OfflineLoadProfileChannelImpl implements OfflineLoadProfileChannel {

    /**
     * The {@link Channel} which is going offline
     */
    private final Channel channel;

    /**
     * The ObisCode used by the {@link com.energyict.mdc.upl.meterdata.LoadProfile} for this channel
     */
    private ObisCode channelObisCode;
    /**
     * The {@link Unit} for this channel
     */
    private Unit unit;

    /**
     * The ID of the {@link com.energyict.mdc.upl.meterdata.Device Device} owning this {@link Channel}
     */
    private int rtuId;

    /**
     * The ID of the {@link com.energyict.mdc.upl.meterdata.LoadProfile} where this {@link Channel} is referring to
     */
    private int loadProfileId;

    /**
     * Indication whether to store meterData in this channel
     */
    private boolean storeData;

    /**
     * The SerialNumber of the {@link com.energyict.mdc.upl.meterdata.Device Device} which owns this {@link Channel}
     */
    private String serialNumber;
    /**
     * The ReadingType of the Kore channel that will store the data
     */
    private ReadingType readingType;
    /**
     * The configured overflow
     */
    private BigDecimal overflow;

    public OfflineLoadProfileChannelImpl(Channel channel) {
        this.channel = channel;
        goOffline();
    }

    /**
     * Triggers the capability to go offline and will copy all information
     * from the database into memory so that normal business operations can continue.<br>
     * Note that this may cause recursive calls to other objects that can go offline.
     */
    protected void goOffline() {
        setChannelObisCode(this.channel.getObisCode());
        setUnit(this.channel.getUnit());
        setRtuId((int) this.channel.getDevice().getId());
        setLoadProfileId((int) this.channel.getLoadProfile().getId());
        setStoreData(true);
        setSerialNumber(this.channel.getDevice().getSerialNumber());
        setReadingType(this.channel.getChannelSpec().getReadingType());
        this.channel.getOverflow().ifPresent(overflow -> this.overflow = overflow);
    }

    /**
     * Returns the {@link ObisCode} for this {@link Channel} in the {@link com.energyict.mdc.upl.meterdata.LoadProfile}
     *
     * @return the {@link ObisCode}
     */
    @Override
    public ObisCode getObisCode() {
        return this.channelObisCode;
    }

    /**
     * Returns the ID of the {@link com.energyict.mdc.upl.meterdata.Device} for the {@link com.energyict.mdc.upl.meterdata.LoadProfile} object.
     *
     * @return the ID of the {@link com.energyict.mdc.upl.meterdata.Device}.
     */
    @Override
    public int getRtuId() {
        return this.rtuId;
    }

    /**
     * Returns the ID of the {@link com.energyict.mdc.upl.meterdata.LoadProfile}
     *
     * @return the ID of the {@link com.energyict.mdc.upl.meterdata.LoadProfile}.
     */
    @Override
    public int getLoadProfileId() {
        return this.loadProfileId;
    }

    /**
     * Returns the receiver's configured unit.
     *
     * @return the configured unit.
     */
    @Override
    public Unit getUnit() {
        return this.unit;
    }

    /**
     * Indication whether we should store data for this channel
     *
     * @return true if we should store data for this channel, false otherwise
     */
    @Override
    public boolean isStoreData() {
        return storeData;
    }

    /**
     * Returns the SerialNumber of the {@link com.energyict.mdc.upl.meterdata.Device Device}
     *
     * @return the SerialNumber of the {@link com.energyict.mdc.upl.meterdata.Device Device}
     */
    @Override
    public String getMasterSerialNumber() {
        return serialNumber;
    }

    @Override
    public ReadingType getReadingType() {
        return this.readingType;
    }

    @Override
    public BigDecimal getOverflow() {
        return this.overflow;
    }

    private void setChannelObisCode(final ObisCode channelObisCode) {
        this.channelObisCode = channelObisCode;
    }

    private void setUnit(final Unit unit) {
        this.unit = unit;
    }

    private void setLoadProfileId(final int loadProfileId) {
        this.loadProfileId = loadProfileId;
    }

    private void setRtuId(final int rtuId) {
        this.rtuId = rtuId;
    }

    private void setSerialNumber(final String serialNumber) {
        this.serialNumber = serialNumber;
    }

    private void setStoreData(final boolean storeData) {
        this.storeData = storeData;
    }

    private void setReadingType(ReadingType readingType) {
        this.readingType = readingType;
    }
}
