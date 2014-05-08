package com.energyict.mdc.engine.impl.commands.offline;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfileChannel;


/**
 * An offline implementation version of a {@link com.energyict.mdc.protocol.api.device.BaseChannel} mainly containing information which is relevant to use at offline-time.
 *
 * @author gna
 * @since 30/05/12 - 9:55
 */
public class OfflineLoadProfileChannelImpl implements OfflineLoadProfileChannel {

    /**
     * The {@link com.energyict.mdc.protocol.api.device.BaseChannel} which is going offline
     */
    private final Channel channel;

    /**
     * The ObisCode used by the {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile} for this channel
     */
    private ObisCode channelObisCode;
    /**
     * The {@link Unit} for this channel
     */
    private Unit unit;

    /**
     * The ID of the {@link com.energyict.mdc.protocol.api.device.BaseDevice Device} owning this {@link com.energyict.mdc.protocol.api.device.BaseChannel}
     */
    private int rtuId;

    /**
     * The ID of the {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile} where this {@link com.energyict.mdc.protocol.api.device.BaseChannel} is referring to
     */
    private int loadProfileId;

    /**
     * Indication whether to store meterData in this channel
     */
    private boolean storeData;

    /**
     * The SerialNumber of the {@link com.energyict.mdc.protocol.api.device.BaseDevice Device} which owns this {@link com.energyict.mdc.protocol.api.device.BaseChannel}
     */
    private String serialNumber;

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
        setChannelObisCode(this.channel.getChannelSpec().getDeviceObisCode());
        setUnit(this.channel.getUnit());
        setRtuId((int) this.channel.getDevice().getId());
        setLoadProfileId((int) this.channel.getLoadProfile().getId());
        setStoreData(true);
        setSerialNumber(this.channel.getDevice().getSerialNumber());
    }

    /**
     * Returns the {@link ObisCode} for this {@link com.energyict.mdc.protocol.api.device.BaseChannel} in the {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile}
     *
     * @return the {@link ObisCode}
     */
    @Override
    public ObisCode getObisCode() {
        return this.channelObisCode;
    }

    /**
     * Returns the ID of the {@link com.energyict.mdc.protocol.api.device.BaseDevice} for the {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile} object.
     *
     * @return the ID of the {@link com.energyict.mdc.protocol.api.device.BaseDevice}.
     */
    @Override
    public int getRtuId() {
        return this.rtuId;
    }

    /**
     * Returns the ID of the {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile}
     *
     * @return the ID of the {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile}.
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
     * Returns the SerialNumber of the {@link com.energyict.mdc.protocol.api.device.BaseDevice Device}
     *
     * @return the SerialNumber of the {@link com.energyict.mdc.protocol.api.device.BaseDevice Device}
     */
    @Override
    public String getMasterSerialNumber() {
        return serialNumber;
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
}
