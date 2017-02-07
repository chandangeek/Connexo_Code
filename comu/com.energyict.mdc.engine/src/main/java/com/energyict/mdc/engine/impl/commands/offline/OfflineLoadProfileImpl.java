/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.offline;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfile;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfileChannel;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An offline implementation of an {@link LoadProfile}.
 *
 * @author gna
 * @since 30/05/12 - 10:26
 */
public class OfflineLoadProfileImpl implements OfflineLoadProfile {

    /**
     * The {@link LoadProfile} which is going offline
     */
    private final LoadProfile loadProfile;
    private final Device device;
    private final TopologyService topologyService;
    private final Map<Device, List<Device>> deviceTopologies;
    private IdentificationService identificationService;

    /**
     * The ID of the {@link LoadProfile} that will go offline
     */
    private long loadProfileId;

    /**
     * The ID of the {@link LoadProfileType LoadProfileType} of this {@link LoadProfile}
     */
    private long loadProfileTypeId;

    /**
     * The ID of the {@link com.energyict.mdc.protocol.api.device.BaseDevice} which owns this {@link LoadProfile}
     */
    private long deviceId;

    /**
     * The {@link ObisCode} of the {@link LoadProfile}
     */
    private ObisCode loadProfileObisCode;

    /**
     * The interval of the {@link LoadProfile}
     */
    private TimeDuration loadProfileInterval;

    /**
     * The date of the last correctly stored interval of this {@link LoadProfile}
     */
    private Instant lastReading;

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
     * <b>OR</b> slave devices belonging to the {@link LoadProfile} of the same type
     */
    private List<OfflineLoadProfileChannel> allLoadProfileChannels;

    private String deviceMRID;

    public OfflineLoadProfileImpl(final LoadProfile loadProfile, TopologyService topologyService, IdentificationService identificationService) {
        this(loadProfile, topologyService, identificationService, new HashMap<>());
    }

    OfflineLoadProfileImpl(LoadProfile loadProfile, TopologyService topologyService, IdentificationService identificationService, Map<Device, List<Device>> deviceTopologies) {
        super();
        this.loadProfile = loadProfile;
        this.topologyService = topologyService;
        this.identificationService = identificationService;
        this.device = loadProfile.getDevice();
        this.deviceTopologies = deviceTopologies;
        goOffline();
    }

    /**
     * Triggers the capability to go offline and will copy all information
     * from the database into memory so that normal business operations can continue.<br>
     * Note that this may cause recursive calls to other objects that can go offline.
     */
    protected void goOffline() {
        setLoadProfileId(this.loadProfile.getId());
        setDeviceId(this.loadProfile.getDevice().getId());
        setLoadProfileTypeId(this.loadProfile.getLoadProfileSpec().getLoadProfileType().getId());
        setSerialNumber(this.loadProfile.getDevice().getSerialNumber());
        setLastReading(this.loadProfile.getLastReading().orElse(null));
        setLoadProfileInterval(this.loadProfile.getLoadProfileSpec().getInterval());
        setLoadProfileObisCode(this.loadProfile.getLoadProfileSpec().getDeviceObisCode());
        setLoadProfileChannels(convertToOfflineChannels(this.loadProfile.getChannels()));
        setAllLoadProfileChannels(convertToOfflineChannels(getAllChannelsForLoadProfile(this.loadProfile)));
        setDeviceMRID(this.loadProfile.getDevice().getmRID());
    }

    private List<Channel> getAllChannelsForLoadProfile(LoadProfile loadProfile) {
        List<Channel> channels = new ArrayList<>(loadProfile.getChannels());
        channels.addAll(
                getPhysicalConnectedDevices(loadProfile.getDevice())
                        .stream()
                        .filter(Device::isLogicalSlave)
                        .flatMap(slave -> slave.getChannels().stream())
                        .filter(c -> c.getLoadProfile().getLoadProfileTypeId() == loadProfile.getLoadProfileTypeId())
                        .collect(Collectors.toList()));
        return channels;
    }

    private List<Device> getPhysicalConnectedDevices(Device device) {
        List<Device> connectedDevices = deviceTopologies.get(device);
        if (connectedDevices == null) {
            connectedDevices = this.topologyService.findPhysicalConnectedDevices(device);
            deviceTopologies.put(device, connectedDevices);
        }
        return connectedDevices;
    }

    /**
     * Convert the given {@link com.energyict.mdc.protocol.api.device.BaseChannel channels} to {@link OfflineLoadProfileChannel offlineLoadProfileChannels}
     *
     * @param channels the channels to go offline
     * @return a list of {@link OfflineLoadProfileChannel offlineLoadProfileChannels}
     */
    protected List<OfflineLoadProfileChannel> convertToOfflineChannels(final List<Channel> channels) {
        return channels.stream().map(OfflineLoadProfileChannelImpl::new).collect(Collectors.toList());
    }

    /**
     * Returns the database ID of this {@link com.energyict.mdc.protocol.api.device.offline.OfflineDevice Rtus'} {@link LoadProfile}
     *
     * @return the ID of the {@link LoadProfile}
     */
    @Override
    public long getLoadProfileId() {
        return loadProfileId;
    }

    /**
     * Returns the database ID of the {@link LoadProfileType LoadProfileType} of this {@link LoadProfile}
     *
     * @return the ID of the {@link LoadProfileType LoadProfileType}
     */
    @Override
    public long getLoadProfileTypeId() {
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
    public Optional<Instant> getLastReading() {
        return Optional.ofNullable(lastReading);
    }

    /**
     * Returns the ID of the {@link com.energyict.mdc.protocol.api.device.BaseDevice} for the {@link LoadProfile} object.
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
        return Collections.unmodifiableList(loadProfileChannels);
    }

    /**
     * Returns the receiver's {@link OfflineLoadProfileChannel} AND the {@link com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfileChannel} of
     * all slave devices belonging to load profiles of the same type
     *
     * @return a <CODE>List</CODE> of {@link com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfileChannel} objects
     */
    @Override
    public List<OfflineLoadProfileChannel> getAllChannels() {
        return Collections.unmodifiableList(allLoadProfileChannels);
    }

    @Override
    public DeviceIdentifier<?> getDeviceIdentifier() {
        return this.identificationService.createDeviceIdentifierForAlreadyKnownDevice(device);
    }

    @Override
    public LoadProfileIdentifier getLoadProfileIdentifier() {
        return this.identificationService.createLoadProfileIdentifierForAlreadyKnownLoadProfile(loadProfile, loadProfileObisCode);
    }

    protected void setAllLoadProfileChannels(final List<OfflineLoadProfileChannel> allLoadProfileChannels) {
        this.allLoadProfileChannels = allLoadProfileChannels;
    }

    private void setLastReading(final Instant lastReading) {
        this.lastReading = lastReading;
    }

    private void setLoadProfileChannels(final List<OfflineLoadProfileChannel> loadProfileChannels) {
        this.loadProfileChannels = loadProfileChannels;
    }

    private void setLoadProfileId(final long loadProfileId) {
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

    private void setLoadProfileTypeId(final long loadProfileTypeId) {
        this.loadProfileTypeId = loadProfileTypeId;
    }

    private void setSerialNumber(final String serialNumber) {
        this.serialNumber = serialNumber;
    }

    private void setDeviceMRID(String deviceMRID) {
        this.deviceMRID = deviceMRID;
    }

    @Override
    public String getDeviceMRID() {
        return deviceMRID;
    }
}
