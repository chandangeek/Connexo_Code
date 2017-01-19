package com.energyict.mdc.engine.impl.commands.offline;

import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.offline.OfflineLoadProfile;
import com.energyict.mdc.upl.offline.OfflineLoadProfileChannel;
import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlElement;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * The ObisCode of the load profile
     */
    private String name;

    /**
     * The ID of the {@link LoadProfile} that will go offline
     */
    private long loadProfileId;

    /**
     * The ID of the {@link LoadProfileType LoadProfileType} of this {@link LoadProfile}
     */
    private long loadProfileTypeId;

    /**
     * The ID of the {@link com.energyict.mdc.upl.meterdata.Device} which owns this {@link LoadProfile}
     */
    private int deviceId;

    /**
     * The {@link ObisCode} of the {@link LoadProfile}
     */
    private ObisCode loadProfileObisCode;

    /**
     * The interval of the {@link LoadProfile}
     */
    private TemporalAmount loadProfileInterval;

    /**
     * The date of the last correctly stored interval of this {@link LoadProfile}
     */
    private Date lastReading;

    /**
     * The serialNumber of the master {@link com.energyict.mdc.upl.meterdata.Device Device}
     */
    private String serialNumber;
    /**
     * Represents a list of {@link OfflineLoadProfileChannel offlineLoadProfileChannels} which are owned by the master {@link com.energyict.mdc.upl.meterdata.Device Device}
     */
    private List<OfflineLoadProfileChannel> loadProfileChannels;
    /**
     * Represents a list of  {@link OfflineLoadProfileChannel offlineLoadProfileChannels} which are owned by the master
     * <b>OR</b> slave devices belonging to the {@link LoadProfile} of the same type
     */
    private List<OfflineLoadProfileChannel> allLoadProfileChannels;

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
        setName(this.loadProfile.getDeviceObisCode().toString());
        setLoadProfileId(this.loadProfile.getId());
        setDeviceId((int) this.loadProfile.getDevice().getId());
        setLoadProfileTypeId(this.loadProfile.getLoadProfileSpec().getLoadProfileType().getId());
        setSerialNumber(this.loadProfile.getDevice().getSerialNumber());
        setLastReading(this.loadProfile.getLastReading());
        setLoadProfileInterval(this.loadProfile.getLoadProfileSpec().getInterval().asTemporalAmount());
        setLoadProfileObisCode(this.loadProfile.getLoadProfileSpec().getDeviceObisCode());
        setLoadProfileChannels(convertToOfflineChannels(this.loadProfile.getChannels()));
        setAllLoadProfileChannels(convertToOfflineChannels(getAllChannelsForLoadProfile(this.loadProfile)));
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
     * Convert the given {@link Channel channels} to {@link OfflineLoadProfileChannel offlineLoadProfileChannels}
     *
     * @param channels the channels to go offline
     * @return a list of {@link OfflineLoadProfileChannel offlineLoadProfileChannels}
     */
    protected List<OfflineLoadProfileChannel> convertToOfflineChannels(final List<Channel> channels) {
        return channels.stream().map(OfflineLoadProfileChannelImpl::new).collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    private void setLoadProfileId(final long loadProfileId) {
        this.loadProfileId = loadProfileId;
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

    private void setLoadProfileTypeId(final long loadProfileTypeId) {
        this.loadProfileTypeId = loadProfileTypeId;
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
    public TemporalAmount interval() {
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

    private void setLastReading(final Date lastReading) {
        this.lastReading = lastReading;
    }

    /**
     * Returns the ID of the {@link com.energyict.mdc.upl.meterdata.Device} for the {@link LoadProfile} object.
     *
     * @return the ID of the {@link com.energyict.mdc.upl.meterdata.Device}.
     */
    @Override
    public int getDeviceId() {
        return this.deviceId;
    }

    private void setDeviceId(final int deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Returns the SerialNumber of the Master {@link com.energyict.mdc.upl.meterdata.Device Device}
     *
     * @return the SerialNumber of the Master {@link com.energyict.mdc.upl.meterdata.Device Device}
     */
    @Override
    public String getMasterSerialNumber() {
        return serialNumber;
    }

    /**
     * Returns the receiver's {@link OfflineLoadProfileChannel}.<br/>
     * <b>Be aware that this will only return the channels of the MASTER rtu.</b>
     * If you require all channels of this LoadProfile, including those of the slave devices with the same LoadProfileType, then use
     * {@link #getAllOfflineChannels()} instead.
     *
     * @return a <CODE>List</CODE> of {@link com.energyict.mdc.upl.offline.OfflineLoadProfileChannel} objects
     */
    @Override
    public List<OfflineLoadProfileChannel> getOfflineChannels() {
        return Collections.unmodifiableList(loadProfileChannels);
    }

    /**
     * Returns the receiver's {@link OfflineLoadProfileChannel} AND the {@link com.energyict.mdc.upl.offline.OfflineLoadProfileChannel} of
     * all slave devices belonging to load profiles of the same type
     *
     * @return a <CODE>List</CODE> of {@link com.energyict.mdc.upl.offline.OfflineLoadProfileChannel} objects
     */
    @Override
    public List<OfflineLoadProfileChannel> getAllOfflineChannels() {
        return Collections.unmodifiableList(allLoadProfileChannels);
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
    public DeviceIdentifier getDeviceIdentifier() {
        return this.identificationService.createDeviceIdentifierForAlreadyKnownDevice(device);
    }

    @Override
    public LoadProfileIdentifier getLoadProfileIdentifier() {
        return this.identificationService.createLoadProfileIdentifierForAlreadyKnownLoadProfile(loadProfile, loadProfileObisCode);
    }

    protected void setAllLoadProfileChannels(final List<OfflineLoadProfileChannel> allLoadProfileChannels) {
        this.allLoadProfileChannels = allLoadProfileChannels;
    }

    private void setLoadProfileChannels(final List<OfflineLoadProfileChannel> loadProfileChannels) {
        this.loadProfileChannels = loadProfileChannels;
    }

    private void setLoadProfileInterval(TemporalAmount loadProfileInterval) {
        this.loadProfileInterval = loadProfileInterval;
    }

    private void setLoadProfileObisCode(final ObisCode loadProfileObisCode) {
        this.loadProfileObisCode = loadProfileObisCode;
    }

    private void setSerialNumber(final String serialNumber) {
        this.serialNumber = serialNumber;
    }
}