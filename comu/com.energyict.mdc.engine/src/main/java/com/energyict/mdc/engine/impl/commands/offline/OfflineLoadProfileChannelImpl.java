package com.energyict.mdc.engine.impl.commands.offline;

import com.energyict.cbo.Unit;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.upl.offline.OfflineLoadProfileChannel;
import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlElement;


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
    private long deviceId;

    /**
     * The ID of the {@link com.energyict.mdc.upl.meterdata.LoadProfile} where this {@link Channel} is referring to
     */
    private long loadProfileId;

    /**
     * Indication whether to store meterData in this channel
     */
    private boolean storeData;

    /**
     * The SerialNumber of the {@link com.energyict.mdc.upl.meterdata.Device Device} which owns this {@link Channel}
     */
    private String serialNumber;
    /**
     * The ReadingType MRID (string) of the Kore channel that will store the data
     */
    private String readingTypeMRID;

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
        setDeviceId(this.channel.getDevice().getId());
        setLoadProfileId(this.channel.getLoadProfile().getId());
        setStoreData(true);
        setSerialNumber(this.channel.getDevice().getSerialNumber());
        setReadingTypeMRID(this.channel.getChannelSpec().getReadingType().getMRID());
    }

    @Override
    public String getName() {
        return getReadingTypeMRID();
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
    public int getDeviceId() {
        return (int) this.deviceId;
    }

    private void setDeviceId(final long deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Returns the ID of the {@link com.energyict.mdc.upl.meterdata.LoadProfile}
     *
     * @return the ID of the {@link com.energyict.mdc.upl.meterdata.LoadProfile}.
     */
    @Override
    public long getLoadProfileId() {
        return this.loadProfileId;
    }

    private void setLoadProfileId(final long loadProfileId) {
        this.loadProfileId = loadProfileId;
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

    private void setUnit(final Unit unit) {
        this.unit = unit;
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

    private void setStoreData(final boolean storeData) {
        this.storeData = storeData;
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

    @XmlElement(name = "type")
    public String getXmlType() {
        return getClass().getName();
    }

    @Override
    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }

    @Override
    public String getReadingTypeMRID() {
        return this.readingTypeMRID;
    }

    private void setReadingTypeMRID(String readingTypeMRID) {
        this.readingTypeMRID = readingTypeMRID;
    }

    private void setChannelObisCode(final ObisCode channelObisCode) {
        this.channelObisCode = channelObisCode;
    }

    private void setSerialNumber(final String serialNumber) {
        this.serialNumber = serialNumber;
    }
}