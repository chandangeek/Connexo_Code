package com.energyict.mdc.engine.impl.commands.offline;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfile;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfileChannel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * An offline implementation version of an {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile} mainly containing information which is relevant to use at offline-time.
 *
 * @author gna
 * @since 30/05/12 - 10:26
 */
public class OfflineLoadProfileImpl implements OfflineLoadProfile {

    /**
     * The {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile} which is going offline
     */
    private final LoadProfile loadProfile;

    /**
     * The ID of the {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile} that will go offline
     */
    private int loadProfileId;

    /**
     * The ID of the {@link LoadProfileType LoadProfileType} of this {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile}
     */
    private int loadProfileTypeId;

    /**
     * The ID of the {@link com.energyict.mdc.protocol.api.device.BaseDevice} which owns this {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile}
     */
    private long deviceId;

    /**
     * The {@link ObisCode} of the {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile}
     */
    private ObisCode loadProfileObisCode;

    /**
     * The interval of the {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile}
     */
    private TimeDuration loadProfileInterval;

    /**
     * The date of the last correctly stored interval of this {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile}
     */
    private Date lastReading;

    /**
     * The serialNumber of the master {@link com.energyict.mdc.protocol.api.device.BaseDevice Device}
     */
    private String serialNumber;

    /**
     * Represents a list of {@link OfflineLoadProfileChannel offlineLoadProfileChannels} which are owned by the master {@link com.energyict.mdc.protocol.api.device.BaseDevice Device}
     */
    private List<OfflineLoadProfileChannel> loadProfileChannels;
    /**
     * Represents a list of  {@link OfflineLoadProfileChannel offlineLoadProfileChannels} which are owned by the master
     * <b>OR</b> slave devices belonging to the {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile} of the same type
     */
    private List<OfflineLoadProfileChannel> allLoadProfileChannels;

    public OfflineLoadProfileImpl(final LoadProfile loadProfile) {
        this.loadProfile = loadProfile;
        goOffline();
    }

    /**
     * Triggers the capability to go offline and will copy all information
     * from the database into memory so that normal business operations can continue.<br>
     * Note that this may cause recursive calls to other objects that can go offline.
     */
    protected void goOffline() {
        setLoadProfileId((int) this.loadProfile.getId());
        setDeviceId(this.loadProfile.getDevice().getId());
        setLoadProfileTypeId((int) this.loadProfile.getLoadProfileSpec().getLoadProfileType().getId());
        setSerialNumber(this.loadProfile.getDevice().getSerialNumber());
        setLastReading(this.loadProfile.getLastReading());
        setLoadProfileInterval(this.loadProfile.getLoadProfileSpec().getInterval());
        setLoadProfileObisCode(this.loadProfile.getLoadProfileSpec().getDeviceObisCode());
        setLoadProfileChannels(convertToOfflineChannels(this.loadProfile.getChannels()));
        setAllLoadProfileChannels(convertToOfflineChannels(this.loadProfile.getAllChannels()));
    }

    /**
     * Convert the given {@link com.energyict.mdc.protocol.api.device.BaseChannel channels} to {@link OfflineLoadProfileChannel offlineLoadProfileChannels}
     *
     * @param channels the channels to go offline
     * @return a list of {@link OfflineLoadProfileChannel offlineLoadProfileChannels}
     */
    protected List<OfflineLoadProfileChannel> convertToOfflineChannels(final List<Channel> channels) {
        List<OfflineLoadProfileChannel> offlineChannelList = new ArrayList<>(channels.size());
        for (Channel channel : channels) {
            offlineChannelList.add(channel.goOffline());
        }
        return offlineChannelList;
    }

    /**
     * Returns the database ID of this {@link com.energyict.mdc.protocol.api.device.offline.OfflineDevice Rtus'} {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile}
     *
     * @return the ID of the {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile}
     */
    @Override
    public int getLoadProfileId() {
        return loadProfileId;
    }

    /**
     * Returns the database ID of the {@link LoadProfileType LoadProfileType} of this {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile}
     *
     * @return the ID of the {@link LoadProfileType LoadProfileType}
     */
    @Override
    public int getLoadProfileTypeId() {
        return loadProfileTypeId;
    }

    /**
     * Returns the ObisCode for the LoadProfileType.
     *
     * @return the ObisCode (referring to a generic collection of channels having the same interval)
     */
    @Override
    public ObisCode getObisCode() {
        return loadProfileObisCode;
    }

    /**
     * Returns the LoadProfile integration period.
     *
     * @return the integration period.
     */
    @Override
    public TimeDuration getInterval() {
        return loadProfileInterval;
    }

    /**
     * return the end time of the last interval read from the device.
     *
     * @return end time of the last interval.
     */
    @Override
    public Date getLastReading() {
        return lastReading;
    }

    /**
     * Returns the ID of the {@link com.energyict.mdc.protocol.api.device.BaseDevice} for the {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile} object.
     *
     * @return the ID of the {@link com.energyict.mdc.protocol.api.device.BaseDevice}.
     */
    @Override
    public long getDeviceId() {
        return this.deviceId;
    }

    /**
     * Returns the SerialNumber of the Master {@link com.energyict.mdc.protocol.api.device.BaseDevice Device}
     *
     * @return the SerialNumber of the Master {@link com.energyict.mdc.protocol.api.device.BaseDevice Device}
     */
    @Override
    public String getMasterSerialNumber() {
        return serialNumber;
    }

    /**
     * Returns the receiver's {@link OfflineLoadProfileChannel}.<br/>
     * <b>Be aware that this will only return the channels of the MASTER rtu.</b>
     * If you require all channels of this LoadProfile, including those of the slave devices with the same LoadProfileType, then use
     * {@link #getAllChannels()} instead.
     *
     * @return a <CODE>List</CODE> of {@link com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfileChannel} objects
     */
    @Override
    public List<OfflineLoadProfileChannel> getChannels() {
        return loadProfileChannels;
    }

    /**
     * Returns the receiver's {@link OfflineLoadProfileChannel} AND the {@link com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfileChannel} of
     * all slave devices belonging to load profiles of the same type
     *
     * @return a <CODE>List</CODE> of {@link com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfileChannel} objects
     */
    @Override
    public List<OfflineLoadProfileChannel> getAllChannels() {
        return allLoadProfileChannels;
    }

    private void setAllLoadProfileChannels(final List<OfflineLoadProfileChannel> allLoadProfileChannels) {
        this.allLoadProfileChannels = allLoadProfileChannels;
    }

    private void setLastReading(final Date lastReading) {
        this.lastReading = lastReading;
    }

    private void setLoadProfileChannels(final List<OfflineLoadProfileChannel> loadProfileChannels) {
        this.loadProfileChannels = loadProfileChannels;
    }

    private void setLoadProfileId(final int loadProfileId) {
        this.loadProfileId = loadProfileId;
    }

    private void setLoadProfileInterval(TimeDuration loadProfileInterval) {
        this.loadProfileInterval = loadProfileInterval;
    }

    private void setLoadProfileObisCode(final ObisCode loadProfileObisCode) {
        this.loadProfileObisCode = loadProfileObisCode;
    }

    private void setDeviceId(final long deviceId) {
        this.deviceId = deviceId;
    }

    private void setLoadProfileTypeId(final int loadProfileTypeId) {
        this.loadProfileTypeId = loadProfileTypeId;
    }

    private void setSerialNumber(final String serialNumber) {
        this.serialNumber = serialNumber;
    }
}
