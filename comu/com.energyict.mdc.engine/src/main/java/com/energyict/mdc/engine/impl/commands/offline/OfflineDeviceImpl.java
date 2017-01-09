package com.energyict.mdc.engine.impl.commands.offline;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.MacException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.engine.impl.EventType;
import com.energyict.mdc.engine.impl.cache.DeviceCache;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags;
import com.energyict.mdc.protocol.api.device.offline.OfflineCalendar;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceContext;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfile;
import com.energyict.mdc.protocol.api.device.offline.OfflineLogBook;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

/**
 * An offline implementation version of an {@link com.energyict.mdc.protocol.api.device.BaseDevice}
 * mainly containing information which is relevant to use at offline-time.
 * <p>
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
     * The ID of the Persistent Device object.
     */
    private long id;

    /**
     * The SerialNumber of the Device.
     */
    private String serialNumber;

    /**
     * Contains all protocol related allProperties.
     */
    private TypedProperties allProperties;

    /**
     * Contains all Offline Slave devices.
     */
    private List<OfflineDevice> slaveDevices = Collections.emptyList();

    /**
     * Contains the {@link OfflineLoadProfile offlineLoadProfiles} which are owned by this {@link OfflineDevice}.
     */
    private List<OfflineLoadProfile> masterLoadProfiles = Collections.emptyList();

    /**
     * Contains all {@link OfflineLoadProfile offlineLoadProfiles} which are owned by this {@link OfflineDevice} <b>AND</b> OR any slave device.
     */
    private List<OfflineLoadProfile> allLoadProfiles = Collections.emptyList();

    /**
     * Contains all {@link OfflineLogBook offlineLogBooks} which are owned by this {@link OfflineDevice}.
     */
    private List<OfflineLogBook> allLogBooks = Collections.emptyList();

    /**
     * Contains all {@link OfflineRegister rtuRegisters} which are owned by this {@link OfflineDevice} or a slave which has the
     * {@link com.energyict.mdc.device.config.DeviceType#isLogicalSlave() rtuType.isLogicalSlave} checked.
     */
    private List<OfflineRegister> allRegisters = Collections.emptyList();

    /**
     * Contains all {@link DeviceMessageStatus#PENDING pending} {@link OfflineDeviceMessage}.
     */
    private List<OfflineDeviceMessage> pendingDeviceMessages = Collections.emptyList();

    /**
     * Contains all {@link DeviceMessageStatus#PENDING pending} {@link OfflineDeviceMessage}
     * that have become invalid since their creation.
     */
    private List<OfflineDeviceMessage> pendingInvalidDeviceMessages = Collections.emptyList();

    /**
     * Contains all {@link DeviceMessageStatus#SENT sent} {@link OfflineDeviceMessage}.
     */
    private List<OfflineDeviceMessage> sentDeviceMessages = Collections.emptyList();

    /**
     * The used DeviceProtocolPluggableClass.
     */
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;

    /**
     * The used DeviceProtocolCache for this Device.
     */
    private DeviceProtocolCache deviceProtocolCache;

    private List<OfflineCalendar> calendars = Collections.emptyList();

    private Map<Device, List<Device>> deviceTopologies = new HashMap<>();

    private boolean firmwareManagementAllowed = false;
    private boolean touCalendarAllowed = false;
    private MacException macException;

    public interface ServiceProvider {

        Thesaurus thesaurus();

        TopologyService topologyService();

        Optional<DeviceCache> findProtocolCacheByDevice(Device device);

        IdentificationService identificationService();

        DeviceConfigurationService deviceConfigurationService();

        FirmwareService firmwareService();

        EventService eventService();


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
        addProperties(this.device.getDeviceProtocolProperties());

        if (this.device.getDeviceProtocolPluggableClass().isPresent()) {
            setDeviceProtocolPluggableClass(this.device.getDeviceProtocolPluggableClass().get());
        }
        if (context.needsSlaveDevices()) {
            List<Device> downstreamDevices = getPhysicalConnectedDevices(this.device);
            List<Device> downStreamEndDevices = new ArrayList<>(downstreamDevices.size());
            downStreamEndDevices.addAll(downstreamDevices.stream().collect(Collectors.toList()));
            setSlaveDevices(convertToOfflineRtus(downStreamEndDevices));
        }
        if (context.needsMasterLoadProfiles()) {
            setMasterLoadProfiles(convertToOfflineLoadProfiles(this.device.getLoadProfiles(), serviceProvider.topologyService()));
        }
        if (context.needsAllLoadProfiles()) {
            setAllLoadProfiles(convertToOfflineLoadProfiles(getAllLoadProfilesIncludingDownStreams(this.device), serviceProvider.topologyService()));
        }
        if (context.needsLogBooks()) {
            setAllLogBooks(convertToOfflineLogBooks(this.device.getLogBooks()));
        }
        if (context.needsRegisters()) {
            setAllRegisters(convertToOfflineRegister(createCompleteRegisterList()));
        }
        if (context.needsPendingMessages()) {
            try {
                serviceProvider.eventService().postEvent(EventType.COMMANDS_WILL_BE_SENT.topic(), null);
                PendingMessagesValidator validator = new PendingMessagesValidator(this.device);
                List<DeviceMessage<Device>> pendingMessages = getAllPendingMessagesIncludingSlaves(device);
                List<DeviceMessage<Device>> reallyPending = new ArrayList<>();
                List<DeviceMessage<Device>> invalidSinceCreation = new ArrayList<>();
                pendingMessages
                        .stream()
                        .forEach(deviceMessage -> {
                            if (validator.isStillValid(deviceMessage)) {
                                deviceMessage.setProtocolInformation(
                                        this.serviceProvider.thesaurus()
                                                .getFormat(MessageSeeds.CALENDAR_NO_LONGER_ALLOWED)
                                                .format(
                                                        validator.failingCalendarNames(deviceMessage),
                                                        this.device.getDeviceType().getName()));
                                reallyPending.add(deviceMessage);
                            } else {
                                invalidSinceCreation.add(deviceMessage);
                            }
                        });
                setAllPendingMessages(createOfflineMessageList(reallyPending));
                setAllPendingInvalidMessages(createOfflineMessageList(invalidSinceCreation));
            } catch (MacException e) {
                this.macException = e;
            }
        }


        if (context.needsSentMessages())

        {
            setAllSentMessages(createOfflineMessageList(getAllSentMessagesIncludingSlaves(device)));
        }

        if (context.needsFirmwareVersions())

        {
            this.firmwareManagementAllowed = serviceProvider.firmwareService().findFirmwareManagementOptions(device.getDeviceType()).isPresent();
        }

        if (context.needsTouCalendar())

        {
            this.touCalendarAllowed = serviceProvider.deviceConfigurationService().findTimeOfUseOptions(device.getDeviceType()).isPresent();
        }

        setDeviceCache(serviceProvider);

        this.

                setCalendars();

    }

    private List<Device> getPhysicalConnectedDevices(Device device) {
        List<Device> connectedDevices = deviceTopologies.get(device);
        if (connectedDevices == null) {
            connectedDevices = serviceProvider.topologyService().findPhysicalConnectedDevices(device);
            deviceTopologies.put(device, connectedDevices);
        }
        return connectedDevices;
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
    private List<Register> createCompleteRegisterList() {
        List<Register> registers = new ArrayList<>();
        registers.addAll(this.device.getRegisters());
        registers.addAll(
                getPhysicalConnectedDevices(device)
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
    private List<LoadProfile> getAllLoadProfilesIncludingDownStreams(Device device) {
        List<LoadProfile> allLoadProfiles = new ArrayList<>(device.getLoadProfiles());
        getPhysicalConnectedDevices(device).stream().
                filter(this::checkTheNeedToGoOffline).
                forEach(slave -> allLoadProfiles.addAll(getAllLoadProfilesIncludingDownStreams(slave)));
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
                .map(lp -> new OfflineLoadProfileImpl(lp, topologyService, serviceProvider.identificationService(), deviceTopologies))
                .collect(Collectors.toList());
    }

    private List<OfflineLogBook> convertToOfflineLogBooks(final List<LogBook> logBooks) {
        List<OfflineLogBook> offlineLogBooks = new ArrayList<>(logBooks.size());
        offlineLogBooks.addAll(logBooks.stream().map(logBook -> new OfflineLogBookImpl(logBook, serviceProvider.identificationService())).collect(Collectors.toList()));
        return offlineLogBooks;
    }

    private List<OfflineRegister> convertToOfflineRegister(final List<Register> registers) {
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
    public List<OfflineDeviceMessage> getAllInvalidPendingDeviceMessages() {
        return Collections.unmodifiableList(this.pendingInvalidDeviceMessages);
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
    void addProperties(TypedProperties properties) {
        if (this.allProperties == null) {
            this.allProperties = TypedProperties.empty();
        }
        // adding the SerialNumber as a property value because legacy protocols check the serialNumber based on the property value
        this.allProperties.setProperty(MeterProtocol.SERIALNUMBER, getSerialNumber());
        if (properties.getInheritedProperties() != null) {
            this.allProperties.setAllProperties(properties.getInheritedProperties());
        }
        this.allProperties.setAllProperties(properties);
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
        getPhysicalConnectedDevices(device).stream().
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
        allDeviceMessages.addAll(device.getMessagesByState(DeviceMessageStatus.SENT));
        getPhysicalConnectedDevices(device).stream().
                filter(this::checkTheNeedToGoOffline).
                forEach(slave -> allDeviceMessages.addAll(getAllSentMessagesIncludingSlaves(slave)));
        return allDeviceMessages;
    }

    private List<OfflineDeviceMessage> createOfflineMessageList(final List<DeviceMessage<Device>> deviceMessages) {
        return deviceMessages
                .stream()
                .filter(deviceMessage -> deviceMessage.getDevice().getDeviceProtocolPluggableClass().isPresent())
                .map(this::toOfflineDeviceMessage)
                .collect(Collectors.toList());
    }

    private OfflineDeviceMessageImpl toOfflineDeviceMessage(DeviceMessage<Device> deviceMessage) {
        return new OfflineDeviceMessageImpl(
                deviceMessage,
                deviceMessage.getDevice().getDeviceProtocolPluggableClass().get().getDeviceProtocol(),
                serviceProvider.identificationService());
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

    private void setAllLogBooks(List<OfflineLogBook> allLogBooks) {
        this.allLogBooks = allLogBooks;
    }

    private void setAllPendingMessages(final List<OfflineDeviceMessage> allPendingMessages) {
        this.pendingDeviceMessages = allPendingMessages;
    }

    private void setAllPendingInvalidMessages(final List<OfflineDeviceMessage> messages) {
        this.pendingInvalidDeviceMessages = messages;
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

    @Override
    public List<OfflineCalendar> getCalendars() {
        return Collections.unmodifiableList(this.calendars);
    }

    @Override
    public boolean touCalendarManagementAllowed() {
        return touCalendarAllowed;
    }

    @Override
    public boolean firmwareVersionManagementAllowed() {
        return firmwareManagementAllowed;
    }

    @Override
    public Optional<MacException> getMacException() {
        return Optional.ofNullable(macException);
    }

    private void setCalendars() {
        this.calendars = this.device.getDeviceType().getAllowedCalendars().stream().map(OfflineCalendarImpl::from).collect(Collectors.toList());
    }

}
