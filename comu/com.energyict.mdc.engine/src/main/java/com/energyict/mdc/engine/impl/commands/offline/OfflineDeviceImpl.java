package com.energyict.mdc.engine.impl.commands.offline;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.engine.impl.cache.DeviceCache;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceContext;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfile;
import com.energyict.mdc.protocol.api.device.offline.OfflineLogBook;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

/**
 * An offline implementation version of an {@link com.energyict.mdc.protocol.api.device.BaseDevice}
 * mainly containing information which is relevant to use at offline-time.
 * <p/>
 *
 * @author gna
 * @since 12/04/12 - 13:58
 */
public class OfflineDeviceImpl implements OfflineDevice {

    /**
     * The Device which is going offline
     */
    private final Device device;
    private ServiceProvider serviceProvider;

    /**
     * The ID of the Persistent Device object
     */
    private long id;

    /**
     * The SerialNumber of the Device
     */
    private String serialNumber;

    /**
     * Contains all protocol related allProperties
     */
    private TypedProperties allProperties;

    /**
     * Contains all Offline Slave devices
     */
    private List<OfflineDevice> slaveDevices = Collections.emptyList();

    /**
     * Contains the {@link OfflineLoadProfile offlineLoadProfiles} which are owned by this {@link OfflineDevice}
     */
    private List<OfflineLoadProfile> masterLoadProfiles = Collections.emptyList();

    /**
     * Contains all {@link OfflineLoadProfile offlineLoadProfiles} which are owned by this {@link OfflineDevice} <b>AND</b> OR any slave device
     */
    private List<OfflineLoadProfile> allLoadProfiles = Collections.emptyList();

    /**
     * Contains all {@link OfflineLogBook offlineLogBooks} which are owned by this {@link OfflineDevice}
     */
    private List<OfflineLogBook> allLogBooks = Collections.emptyList();

    /**
     * Contains all {@link OfflineRegister rtuRegisters} which are owned by this {@link OfflineDevice} or a slave which has the
     * {@link com.energyict.mdc.device.config.DeviceType#isLogicalSlave() rtuType.isLogicalSlave} checked
     */
    private List<OfflineRegister> allRegisters = Collections.emptyList();

    /**
     * Contains all {@link DeviceMessageStatus#PENDING pending} {@link OfflineDeviceMessage}
     */
    private List<OfflineDeviceMessage> pendingDeviceMessages = Collections.emptyList();
    /**
     * Contains all {@link DeviceMessageStatus#SENT sent} {@link OfflineDeviceMessage}
     */
    private List<OfflineDeviceMessage> sentDeviceMessages = Collections.emptyList();
    /**
     * The used DeviceProtocolPluggableClass
     */
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;

    /**
     * The used DeviceProtocolCache for this Device
     */
    private DeviceProtocolCache deviceProtocolCache;

    public interface ServiceProvider {

        TopologyService topologyService();

        Optional<DeviceCache> findProtocolCacheByDevice(Device device);

        IdentificationService identificationService();

    }

    public OfflineDeviceImpl(Device device, OfflineDeviceContext offlineDeviceContext, ServiceProvider serviceProvider) {
        this.device = device;
        this.serviceProvider = serviceProvider;
        goOffline(offlineDeviceContext);
    }

    /**
     * Triggers the capability to go offline and will copy all information
     * from the database into memory so that normal business operations can continue.<br>
     * Note that this may cause recursive calls to other objects that can go offline.
     */
    private void goOffline(OfflineDeviceContext context) {
        setId(this.device.getId());
        setSerialNumber(this.device.getSerialNumber());
        this.allProperties = TypedProperties.empty();

        if (this.device.getDeviceProtocolPluggableClass() != null) {
            setDeviceProtocolPluggableClass(this.device.getDeviceProtocolPluggableClass());
        }

        addProperties(TypedProperties.empty());
        addProperties(this.device.getDeviceProtocolProperties());

        if (context.needsSlaveDevices()) {
            List<Device> downstreamDevices = serviceProvider.topologyService().findPhysicalConnectedDevices(this.device);
            List<Device> downStreamEndDevices = new ArrayList<>(downstreamDevices.size());
            downStreamEndDevices.addAll(downstreamDevices.stream().collect(Collectors.toList()));
            setSlaveDevices(convertToOfflineRtus(downStreamEndDevices));
        }
        if (context.needsMasterLoadProfiles()) {
            setMasterLoadProfiles(convertToOfflineLoadProfiles(this.device.getLoadProfiles(), serviceProvider.topologyService()));
        }
        if (context.needsAllLoadProfiles()) {
            setAllLoadProfiles(convertToOfflineLoadProfiles(getAllLoadProfilesIncludingDownStreams(this.device, serviceProvider.topologyService()), serviceProvider.topologyService()));
        }
        if (context.needsLogBooks()) {
            setAllLogBooks(convertToOfflineLogBooks(this.device.getLogBooks()));
        }
        if (context.needsRegisters()) {
            setAllRegisters(convertToOfflineRegister(createCompleteRegisterList(serviceProvider.topologyService())));
        }
        if (context.needsPendingMessages()) {
            setAllPendingMessages(createOfflineMessageList(getAllPendingMessagesIncludingSlaves(device)));
        }
        if (context.needsSentMessages()) {
            setAllSentMessages(createOfflineMessageList(getAllSentMessagesIncludingSlaves(device)));
        }
        setDeviceCache(serviceProvider);
    }

    /**
     * We get the cache from the DataBase. The object will only be set if it is an instance of {@link DeviceProtocolCache}.
     * Otherwise an nullObject will be provided to the {@link DeviceProtocol} so it can
     * be refetched from the Device.
     *
     * @param serviceProvider The ServiceProvider
     */
    private void setDeviceCache(ServiceProvider serviceProvider) {
        Optional<DeviceCache> deviceProtocolCache = serviceProvider.findProtocolCacheByDevice(device);
        if (deviceProtocolCache.isPresent()) {
            Serializable cacheObject = deviceProtocolCache.get().getSimpleCacheObject();
            if (cacheObject != null) {
                this.deviceProtocolCache = (DeviceProtocolCache) cacheObject;
                this.deviceProtocolCache.markClean(); // Cache is loaded from DB, so make sure it is marked clean, i.e. not dirty or changed
            }
        }
    }

    /**
     * Get a List of all RtuRegisters, including those of the slave devices with the needsProxy checked.
     *
     * @return the list of all RtuRegisters
     */
    private List<Register> createCompleteRegisterList(TopologyService topologyService) {
        List<Register> registers = new ArrayList<>();
        registers.addAll(this.device.getRegisters());
        registers.addAll(
                topologyService
                        .findPhysicalConnectedDevices(device)
                        .stream()
                        .filter(this::checkTheNeedToGoOffline)
                        .flatMap(slave -> slave.getRegisters().stream())
                        .collect(Collectors.toList()));
        return registers;
    }

    /**
     * Create a <CODE>List</CODE> of all the <CODE>LoadProfiles</CODE> a <CODE>Device</CODE> has,
     * including the physically connected devices which have the
     * {@link com.energyict.mdc.device.config.DeviceType#isLogicalSlave()} flag checked.
     *
     * @param device the <CODE>Device</CODE> to collect the <CODE>LoadProfiles</CODE> from
     * @return a List of <CODE>LoadProfiles</CODE>
     */
    private List<LoadProfile> getAllLoadProfilesIncludingDownStreams(Device device, TopologyService topologyService) {
        List<LoadProfile> allLoadProfiles = new ArrayList<>(device.getLoadProfiles());
        topologyService.findPhysicalConnectedDevices(device).stream().
                filter(this::checkTheNeedToGoOffline).
                forEach(slave -> allLoadProfiles.addAll(getAllLoadProfilesIncludingDownStreams(slave, topologyService)));
        return allLoadProfiles;
    }

    /**
     * Converts the given {@link Device}s to {@link OfflineDevice}s if they have the option
     * {@link com.energyict.mdc.device.config.DeviceType#isLogicalSlave()} checked.
     *
     * @param downstreamRtus The rtus to go offline
     * @return a list of OfflineDevice
     */
    private List<OfflineDevice> convertToOfflineRtus(List<Device> downstreamRtus) {
        List<OfflineDevice> offlineSlaves = new ArrayList<>(downstreamRtus.size());
        for (Device downstreamRtu : downstreamRtus) {
            OfflineDevice offlineDevice = new OfflineDeviceImpl(downstreamRtu, new DeviceOfflineFlags(DeviceOfflineFlags.SLAVE_DEVICES_FLAG), serviceProvider);
            offlineSlaves.add(offlineDevice);
            offlineSlaves.addAll(offlineDevice.getAllSlaveDevices());
        }
        return offlineSlaves;
    }

    private boolean checkTheNeedToGoOffline(Device downstreamRtu) {
        return downstreamRtu.getDeviceType().isLogicalSlave();
    }

    private List<OfflineLoadProfile> convertToOfflineLoadProfiles(final List<LoadProfile> loadProfiles, TopologyService topologyService) {
        return loadProfiles
                .stream()
                .map(lp -> new OfflineLoadProfileImpl(lp, topologyService, serviceProvider.identificationService()))
                .collect(Collectors.toList());
    }

    private List<OfflineLogBook> convertToOfflineLogBooks(final List<LogBook> logBooks){
        List<OfflineLogBook> offlineLogBooks = new ArrayList<>(logBooks.size());
        offlineLogBooks.addAll(logBooks.stream().map(logBook -> new OfflineLogBookImpl(logBook, serviceProvider.identificationService())).collect(Collectors.toList()));
        return offlineLogBooks;
    }

    private List<OfflineRegister> convertToOfflineRegister(final List<Register> registers){
        List<OfflineRegister> offlineRegisters = new ArrayList<>(registers.size());
        offlineRegisters.addAll(registers.stream().map(register -> new OfflineRegisterImpl(register, serviceProvider.identificationService())).collect(Collectors.toList()));
        return offlineRegisters;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public TimeZone getTimeZone() {
        return this.device.getTimeZone();
    }

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

    @Override
    public TypedProperties getAllProperties() {
        return allProperties;
    }

    @Override
    public List<OfflineDevice> getAllSlaveDevices() {
        return slaveDevices;
    }

    @Override
    public List<OfflineLoadProfile> getMasterOfflineLoadProfiles() {
        return this.masterLoadProfiles;
    }

    @Override
    public List<OfflineLoadProfile> getAllOfflineLoadProfiles() {
        return this.allLoadProfiles;
    }

    @Override
    public List<OfflineLoadProfile> getAllOfflineLoadProfilesForMRID(String mrid) {
        return getAllOfflineLoadProfiles().stream().filter(offlineLoadProfile -> offlineLoadProfile.getDeviceMRID().equals(mrid)).collect(Collectors.toList());
    }

    @Override
    public List<OfflineLogBook> getAllOfflineLogBooks() {
        return this.allLogBooks;
    }

    @Override
    public List<OfflineLogBook> getAllOfflineLogBooksForMRID(String mrid) {
        return getAllOfflineLogBooks().stream().filter(offlineLogBook -> offlineLogBook.getDeviceMRID().equals(mrid)).collect(Collectors.toList());
    }

    @Override
    public List<OfflineRegister> getAllRegisters() {
        return allRegisters;
    }

    @Override
    public List<OfflineRegister> getRegistersForRegisterGroupAndMRID(final List<Long> deviceRegisterGroupIds, String mrid) {
        return getAllRegisters().stream().filter(register -> register.inAtLeastOneGroup(deviceRegisterGroupIds) && register.getDeviceMRID().equals(mrid)).collect(Collectors.toList());
    }

    @Override
    public List<OfflineDeviceMessage> getAllPendingDeviceMessages() {
        return this.pendingDeviceMessages;
    }

    @Override
    public List<OfflineDeviceMessage> getAllSentDeviceMessages() {
        return this.sentDeviceMessages;
    }

    /**
     * Add the given properties to the {@link #allProperties}
     *
     * @param properties the Properties to add
     */
    void addProperties(TypedProperties... properties) {
        if (this.allProperties == null) {
            this.allProperties = TypedProperties.empty();
        }
        for (TypedProperties props : properties) {
            this.allProperties.setAllProperties(props);
        }
        // adding the SerialNumber as a property value because legacy protocols check the serialNumber based on the property value
        this.allProperties.setProperty(MeterProtocol.SERIALNUMBER, getSerialNumber());
    }

    /**
     * Create a list of all the pending DeviceMessages a device has,
     * including his slave devices which have the
     * islogicalSlave flag checked.
     *
     * @param device the Device to collect the pending DeviceMessages from
     * @return a List of pending DeviceMessages
     */
    private List<DeviceMessage<Device>> getAllPendingMessagesIncludingSlaves(Device device) {
        List<DeviceMessage<Device>> allDeviceMessages = new ArrayList<>();
        allDeviceMessages.addAll(device.getMessagesByState(DeviceMessageStatus.PENDING));
        TopologyService topologyService = serviceProvider.topologyService();
        topologyService.findPhysicalConnectedDevices(device).stream().
                filter(this::checkTheNeedToGoOffline).
                forEach(slave -> allDeviceMessages.addAll(getAllPendingMessagesIncludingSlaves(slave)));
        return allDeviceMessages;
    }

    /**
     * Create a list of all the sent DeviceMessages a device has,
     * including his slave devices which have the
     * islogicalSlave flag checked.
     *
     * @param device the Device to collect the sent DeviceMessages from
     * @return a List of sent DeviceMessages
     */
    private List<DeviceMessage<Device>> getAllSentMessagesIncludingSlaves(Device device) {
        List<DeviceMessage<Device>> allDeviceMessages = new ArrayList<>();
        allDeviceMessages.addAll(device.getMessagesByState(DeviceMessageStatus.PENDING));
        TopologyService topologyService = serviceProvider.topologyService();
        topologyService.findPhysicalConnectedDevices(device).stream().
                filter(this::checkTheNeedToGoOffline).
                forEach(slave -> allDeviceMessages.addAll(getAllSentMessagesIncludingSlaves(slave)));
        return allDeviceMessages;
    }

    private List<OfflineDeviceMessage> createOfflineMessageList(final List<DeviceMessage<Device>> deviceMessages) {
        List<OfflineDeviceMessage> offlineDeviceMessages = new ArrayList<>(deviceMessages.size());
        offlineDeviceMessages.addAll(deviceMessages.stream().map(deviceMessage -> new OfflineDeviceMessageImpl(deviceMessage, deviceProtocolPluggableClass.getDeviceProtocol(), serviceProvider.identificationService())).collect(Collectors.toList()));
        return offlineDeviceMessages;
    }

    private void setId(final long id) {
        this.id = id;
    }

    private void setAllRegisters(final List<OfflineRegister> registers) {
        this.allRegisters = registers;
    }

    private void setSerialNumber(final String serialNumber) {
        this.serialNumber = serialNumber;
    }

    private void setSlaveDevices(final List<OfflineDevice> offlineDevices) {
        this.slaveDevices = offlineDevices;
    }

    private void setMasterLoadProfiles(final List<OfflineLoadProfile> masterLoadProfiles) {
        this.masterLoadProfiles = masterLoadProfiles;
    }

    private void setAllLoadProfiles(final List<OfflineLoadProfile> allLoadProfiles) {
        this.allLoadProfiles = allLoadProfiles;
    }

    public void setAllLogBooks(List<OfflineLogBook> allLogBooks) {
        this.allLogBooks = allLogBooks;
    }

    private void setAllPendingMessages(final List<OfflineDeviceMessage> allPendingMessages) {
        this.pendingDeviceMessages = allPendingMessages;
    }

    private void setAllSentMessages(final List<OfflineDeviceMessage> allSentMessages) {
        this.sentDeviceMessages = allSentMessages;
    }

    public DeviceProtocolPluggableClass getDeviceProtocolPluggableClass() {
        return deviceProtocolPluggableClass;
    }

    @Override
    public DeviceProtocolCache getDeviceProtocolCache() {
        return deviceProtocolCache;
    }

    @Override
    public DeviceIdentifier<?> getDeviceIdentifier() {
        return this.serviceProvider.identificationService().createDeviceIdentifierForAlreadyKnownDevice(device);
    }

    @Override
    public List<OfflineRegister> getAllRegistersForMRID(String mrid) {
        return getAllRegisters().stream().filter(offlineRegister -> offlineRegister.getDeviceMRID().equals(mrid)).collect(Collectors.toList());
    }

    private void setDeviceProtocolPluggableClass(DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        this.deviceProtocolPluggableClass = deviceProtocolPluggableClass;
    }

}
