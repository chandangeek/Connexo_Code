/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.offline;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.MacException;
import com.elster.jupiter.util.HasId;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.config.SecurityPropertySet;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.LoadProfile;
import com.energyict.mdc.common.device.data.LogBook;
import com.energyict.mdc.common.device.data.Register;
import com.energyict.mdc.common.device.data.SecurityAccessor;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.engine.impl.EventType;
import com.energyict.mdc.engine.impl.cache.DeviceCache;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.core.remote.MapXmlMarshallAdapter;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.identifiers.DeviceIdentifierByConnectionTypeAndProperty;
import com.energyict.mdc.identifiers.DeviceIdentifierByDeviceName;
import com.energyict.mdc.identifiers.DeviceIdentifierById;
import com.energyict.mdc.identifiers.DeviceIdentifierByMRID;
import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.identifiers.DeviceIdentifierForAlreadyKnownDevice;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineKeyAccessor;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.DeviceProtocolPluggableClassImpl;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.LoadProfileType;
import com.energyict.mdc.upl.meterdata.RegisterGroup;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.offline.DeviceOfflineFlags;
import com.energyict.mdc.upl.offline.OfflineCalendar;
import com.energyict.mdc.upl.offline.OfflineDeviceContext;
import com.energyict.mdc.upl.offline.OfflineLoadProfile;
import com.energyict.mdc.upl.offline.OfflineLogBook;
import com.energyict.mdc.upl.offline.OfflineRegister;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
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
 * An offline implementation version of an {@link com.energyict.mdc.upl.meterdata.Device}
 * mainly containing information which is relevant to use at offline-time.
 * <p>
 *
 * @author gna
 * @since 12/04/12 - 13:58
 */
@XmlRootElement
public class OfflineDeviceImpl implements ServerOfflineDevice {

    /**
     * The Device which is going offline
     */
    private Device device;
    private DeviceIdentifier deviceIdentifier;
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
    private List<OfflineDevice> slaveDevices = new ArrayList<>();

    /**
     * Contains the {@link OfflineLoadProfile offlineLoadProfiles} which are owned by this {@link OfflineDevice}.
     */
    private List<OfflineLoadProfile> masterLoadProfiles = new ArrayList<>();

    /**
     * Contains all {@link OfflineLoadProfile offlineLoadProfiles} which are owned by this {@link OfflineDevice} <b>AND</b> OR any slave device.
     */
    private List<OfflineLoadProfile> allOfflineLoadProfiles = new ArrayList<>();

    /**
     * Contains all {@link OfflineLogBook offlineLogBooks} which are owned by this {@link OfflineDevice}.
     */
    private List<OfflineLogBook> allOfflineLogBooks = new ArrayList<>();

    /**
     * Contains all {@link OfflineRegister rtuRegisters} which are owned by this {@link OfflineDevice} or a slave which has the
     * {@link DeviceType#isLogicalSlave() rtuType.isLogicalSlave} checked.
     */
    private List<OfflineRegister> allOfflineRegisters = new ArrayList<>();

    /**
     * Contains all {@link DeviceMessageStatus#PENDING pending} {@link OfflineDeviceMessage}.
     */
    private List<OfflineDeviceMessage> pendingDeviceMessages = new ArrayList<>();

    /**
     * Contains all {@link DeviceMessageStatus#PENDING pending} {@link OfflineDeviceMessage}
     * that have become invalid since their creation.
     */
    private List<OfflineDeviceMessage> pendingInvalidDeviceMessages = new ArrayList<>();

    /**
     * Contains all {@link DeviceMessageStatus#SENT sent} {@link OfflineDeviceMessage}.
     */
    private List<OfflineDeviceMessage> sentDeviceMessages = new ArrayList<>();

    /**
     * The used DeviceProtocolPluggableClass.
     */
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;

    /**
     * The used DeviceProtocolCache for this Device.
     */
    private DeviceProtocolCache deviceProtocolCache;

    private List<OfflineCalendar> calendars = new ArrayList<>();

    private boolean firmwareManagementAllowed = false;
    private boolean touCalendarAllowed = false;
    private String location = "";
    private String usagePoint = "";
    private MacException macException;
    private List<OfflineKeyAccessor> keyAccessors = new ArrayList<>();
    private HashMap<String, TypedProperties> securityPropertySetAttributeToKeyAccessorTypeMapping;
    private String mRid;
    private TimeZone timeZone;

    public OfflineDeviceImpl() {
        super();
    }

    public OfflineDeviceImpl(Device device, OfflineDeviceContext offlineDeviceContext, ServiceProvider serviceProvider) {
        this(device, offlineDeviceContext, serviceProvider, new HashMap<>());
    }

    private OfflineDeviceImpl(Device device, OfflineDeviceContext offlineDeviceContext, ServiceProvider serviceProvider, Map<Device, List<Device>> deviceTopologies) {
        this.device = device;
        this.serviceProvider = serviceProvider;
        goOffline(offlineDeviceContext, deviceTopologies);
    }

    /**
     * Triggers the capability to go offline and will copy all information
     * from the database into memory so that normal business operations can continue.<br>
     * Note that this may cause recursive calls to other objects that can go offline.
     */
    private void goOffline(OfflineDeviceContext context, Map<Device, List<Device>> deviceTopologies) {
        List<SecurityAccessor> slaveSecurityAccessors = new ArrayList<>();

        setLocation(device.getLocation().map(Object::toString).orElse(""));
        setUsagePoint(device.getUsagePoint().map(UsagePoint::getName).orElse(""));
        setId(device.getId());
        setSerialNumber(device.getSerialNumber());
        setmRID(device.getmRID());
        addProperties(device.getDeviceProtocolProperties());

        if (device.getDeviceProtocolPluggableClass().isPresent()) {
            setDeviceProtocolPluggableClass(device.getDeviceProtocolPluggableClass().get());
        }
        if (context.needsSlaveDevices()) {
            List<Device> downstreamDevices = getPhysicalConnectedDevices(device, deviceTopologies);
            setSlaveDevices(convertToOfflineRtus(downstreamDevices, deviceTopologies));

            for (final Device device : downstreamDevices) {
                slaveSecurityAccessors.addAll( device.getSecurityAccessors() );
            }
        }
        if (context.needsMasterLoadProfiles()) {
            setMasterLoadProfiles(convertToOfflineLoadProfiles(device.getLoadProfiles(), serviceProvider.topologyService(), deviceTopologies));
        }
        if (context.needsAllLoadProfiles()) {
            setAllLoadProfiles(convertToOfflineLoadProfiles(getAllLoadProfilesIncludingDownStreams(device, deviceTopologies), serviceProvider.topologyService(), deviceTopologies));
        }
        if (context.needsLogBooks()) {
            setAllLogBooks(convertToOfflineLogBooks(getAllLogBooksIncludingDownStreams(device, deviceTopologies)));
        }
        if (context.needsRegisters()) {
            setAllOfflineRegisters(convertToOfflineRegister(createCompleteRegisterList(deviceTopologies)));
        }

        List<SecurityAccessor> allSecurityAccessors = new ArrayList<>(slaveSecurityAccessors);
        allSecurityAccessors.addAll( device.getSecurityAccessors() );

        setAllKeyAccessors(convertToOfflineKeyAccessors( allSecurityAccessors ));
        setSecurityPropertySetAttributeToKeyAccessorType(device);
        if (context.needsPendingMessages()) {
            setAllPendingMessages(deviceTopologies);
        }
        if (context.needsSentMessages()) {
            setAllSentMessages(createOfflineMessageList(getAllSentMessagesIncludingSlaves(device, deviceTopologies)));
        }
        if (context.needsFirmwareVersions()) {
            firmwareManagementAllowed = serviceProvider.firmwareService().findFirmwareManagementOptions(device.getDeviceType()).isPresent();
        }
        if (context.needsTouCalendar()) {
            touCalendarAllowed = serviceProvider.deviceConfigurationService().findTimeOfUseOptions(device.getDeviceType()).isPresent();
        }
        setDeviceCache(serviceProvider);
        setCalendars();
    }

    private void setAllPendingMessages(Map<Device, List<Device>> deviceTopologies) {
        try {
            List<DeviceMessage> reallyPending = new ArrayList<>();
            List<DeviceMessage> invalidSinceCreation = new ArrayList<>();
            fillReallyPendingAndInvalidMessagesLists(deviceTopologies, reallyPending, invalidSinceCreation);
            setAllPendingMessages(createOfflineMessageList(reallyPending));
            setAllPendingInvalidMessages(createOfflineMessageList(invalidSinceCreation));
        } catch (MacException e) {
            macException = e;
        }
    }

    private void fillReallyPendingAndInvalidMessagesLists(Map<Device, List<Device>> deviceTopologies, List<DeviceMessage> reallyPending, List<DeviceMessage> invalidSinceCreation) {
        serviceProvider.eventService().postEvent(EventType.COMMANDS_WILL_BE_SENT.topic(), null);
        PendingMessagesValidator validator = new PendingMessagesValidator(device);
        List<DeviceMessage> pendingMessages = getAllPendingMessagesIncludingSlaves(device, deviceTopologies);
        pendingMessages
                .forEach(deviceMessage -> {
                    if (validator.isStillValid(deviceMessage)) {
                        reallyPending.add(deviceMessage);
                    } else {
                        deviceMessage.setProtocolInformation(
                                serviceProvider.thesaurus()
                                        .getFormat(MessageSeeds.CALENDAR_NO_LONGER_ALLOWED)
                                        .format(
                                                validator.failingCalendarNames(deviceMessage),
                                                device.getDeviceType().getName()));
                        invalidSinceCreation.add(deviceMessage);
                    }
                });
    }

    private List<Device> getPhysicalConnectedDevices(Device device, Map<Device, List<Device>> deviceTopologies) {
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
            Serializable cacheObject = deviceProtocolCache.get().getCacheObject();
            if (cacheObject != null) {
                this.deviceProtocolCache = (DeviceProtocolCache) cacheObject;
                this.deviceProtocolCache.setContentChanged(false); // Cache is loaded from DB, so make sure it is marked clean, i.e. not dirty or changed
            }
        }
    }

    /**
     * Get a List of all RtuRegisters, including those of the slave devices with the needsProxy checked.
     *
     * @return the list of all RtuRegisters
     */
    private List<Register> createCompleteRegisterList(Map<Device, List<Device>> deviceTopologies) {
        List<Register> registers = new ArrayList<>();
        registers.addAll(device.getRegisters());
        registers.addAll(
                getPhysicalConnectedDevices(device, deviceTopologies)
                        .stream()
                        .filter(this::checkTheNeedToGoOffline)
                        .flatMap(slave -> slave.getRegisters().stream())
                        .collect(Collectors.toList()));
        return registers;
    }

    /**
     * Create a <CODE>List</CODE> of all the <CODE>LoadProfiles</CODE> a <CODE>Device</CODE> has,
     * including the physically connected devices which have the
     * {@link DeviceType#isLogicalSlave()} flag checked.
     *
     * @param device the <CODE>Device</CODE> to collect the <CODE>LoadProfiles</CODE> from
     * @param deviceTopologies
     * @return a List of <CODE>LoadProfiles</CODE>
     */
    private List<LoadProfile> getAllLoadProfilesIncludingDownStreams(Device device, Map<Device, List<Device>> deviceTopologies) {
        List<LoadProfile> allLoadProfiles = new ArrayList<>(device.getLoadProfiles());
        getPhysicalConnectedDevices(device, deviceTopologies).stream().
                filter(this::checkTheNeedToGoOffline).
                forEach(slave -> allLoadProfiles.addAll(getAllLoadProfilesIncludingDownStreams(slave, deviceTopologies)));
        return allLoadProfiles;
    }

    private List<LogBook> getAllLogBooksIncludingDownStreams(Device device, Map<Device, List<Device>> deviceTopologies) {
        List<LogBook> allLogBooks = new ArrayList<>(device.getLogBooks());
        getPhysicalConnectedDevices(device, deviceTopologies).stream().
                filter(this::checkTheNeedToGoOffline).
                forEach(slave -> allLogBooks.addAll(getAllLogBooksIncludingDownStreams(slave, deviceTopologies )));
        return allLogBooks;
    }

    /**
     * Converts the given {@link Device}s to {@link OfflineDevice}s if they have the option
     * {@link DeviceType#isLogicalSlave()} checked.
     *
     * @param downstreamRtus The rtus to go offline
     * @param deviceTopologies
     * @return a list of OfflineDevice
     */
    private List<OfflineDevice> convertToOfflineRtus(List<Device> downstreamRtus, Map<Device, List<Device>> deviceTopologies) {
        List<OfflineDevice> offlineSlaves = new ArrayList<>(downstreamRtus.size());
        for (Device downstreamRtu : downstreamRtus) {
            OfflineDevice offlineDevice = new OfflineDeviceImpl(downstreamRtu,
                    new DeviceOfflineFlags(DeviceOfflineFlags.SLAVE_DEVICES_FLAG, DeviceOfflineFlags.FIRMWARE_VERSIONS_FLAG),
                    serviceProvider, deviceTopologies);
            offlineSlaves.add(offlineDevice);

            List<OfflineDevice> slaveDevices = offlineDevice
                    .getAllSlaveDevices()
                    .stream()
                    .map(serviceProvider.protocolPluggableService()::adapt)
                    .collect(Collectors.toList());

            offlineSlaves.addAll(slaveDevices);
        }
        return offlineSlaves;
    }

    private boolean checkTheNeedToGoOffline(Device downstreamRtu) {
        return downstreamRtu.getDeviceType().isLogicalSlave();
    }

    private List<OfflineLoadProfile> convertToOfflineLoadProfiles(final List<LoadProfile> loadProfiles, TopologyService topologyService, Map<Device, List<Device>> deviceTopologies) {
        return loadProfiles
                .stream()
                .map(lp -> new OfflineLoadProfileImpl(lp, topologyService, serviceProvider.identificationService(), deviceTopologies))
                .collect(Collectors.toList());
    }

    private List<OfflineLogBook> convertToOfflineLogBooks(final List<LogBook> logBooks) {
        return logBooks.stream().map(logBook -> new OfflineLogBookImpl(logBook, serviceProvider.identificationService())).collect(Collectors.toList());
    }

    private List<OfflineRegister> convertToOfflineRegister(final List<Register> registers) {
        return registers.stream().map(register -> new OfflineRegisterImpl(register, serviceProvider.identificationService())).collect(Collectors.toList());
    }

    private List<OfflineKeyAccessor> convertToOfflineKeyAccessors(final List<SecurityAccessor> securityAccessors) {
        List<OfflineKeyAccessor> offlineKeyAccessors = new ArrayList<>(securityAccessors.size());
        offlineKeyAccessors.addAll(securityAccessors.stream().map(keyAccessor -> new OfflineKeyAccessorImpl(keyAccessor, serviceProvider.identificationService())).collect(Collectors.toList()));
        return offlineKeyAccessors;
    }

    @Override
    @XmlAttribute
    public long getId() {
        return id;
    }

    private void setId(final long id) {
        this.id = id;
    }

    @Override
    @XmlAttribute
    public TimeZone getTimeZone() {
        if (device != null) {
            timeZone = device.getTimeZone();
        }
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    @XmlAttribute
    public String getSerialNumber() {
        return serialNumber;
    }

    private void setSerialNumber(final String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @Override
    @XmlAttribute
    public String getmRID() {
        return mRid;
    }

    public void setmRID(String mRid) {
        this.mRid = mRid;
    }

    @Override
    public String getExternalName() {
        return null;    //Not available in Connexo
    }

    @Override
    @XmlAttribute
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
    @Override

    @XmlAttribute
    public String getUsagePoint() {
        return usagePoint;
    }

    public void setUsagePoint(String usagePoint) {
        this.usagePoint = usagePoint;
    }

    @Override
    public com.energyict.mdc.upl.properties.TypedProperties getAllProperties() {
        return allProperties;
    }

    @Override
    @XmlElement(type = OfflineDeviceImpl.class)
    public List<OfflineDevice> getAllSlaveDevices() {
        return slaveDevices;
    }

    @Override
    @XmlElement(type = OfflineLoadProfileImpl.class)
    public List<OfflineLoadProfile> getMasterOfflineLoadProfiles() {
        return masterLoadProfiles;
    }

    @Override
    @XmlElement(type = OfflineLoadProfileImpl.class)
    public List<OfflineLoadProfile> getAllOfflineLoadProfiles() {
        return allOfflineLoadProfiles;
    }

    @Override
    @XmlElement(type = OfflineLogBookImpl.class)
    public List<OfflineLogBook> getAllOfflineLogBooks() {
        return allOfflineLogBooks;
    }

    @Override
    @XmlElement(type = OfflineRegisterImpl.class)
    public List<OfflineRegister> getAllOfflineRegisters() {
        return allOfflineRegisters;
    }

    private void setAllOfflineRegisters(final List<OfflineRegister> registers) {
        allOfflineRegisters = registers;
    }

    @Override
    public List<OfflineRegister> getRegistersForRegisterGroup(List<RegisterGroup> rtuRegisterGroups) {
        List<Long> ids = rtuRegisterGroups
                .stream()
                .map(HasId.class::cast)     //Downcast from UPL to CXO
                .map(HasId::getId)
                .collect(Collectors.toList());

        return getAllOfflineRegisters()
                .stream()
                .filter(register -> register.inAtLeastOneGroup(ids))
                .collect(Collectors.toList());
    }

    @Override
    public List<OfflineRegister> getRegistersForRegisterGroupAndMRID(List<Long> deviceRegisterGroupIds, String mRID) {
        return getAllOfflineRegisters()
                .stream()
                .filter(register -> register.getDeviceMRID().equals(mRID))
                .filter(register -> register.inAtLeastOneGroup(deviceRegisterGroupIds))
                .collect(Collectors.toList());
    }

    @Override
    @XmlAttribute
    @XmlElement(type = OfflineDeviceMessageImpl.class)
    public List<OfflineDeviceMessage> getAllPendingDeviceMessages() {
        return pendingDeviceMessages;
    }

    @Override
    @XmlElement(type = OfflineDeviceMessageImpl.class)
    public List<OfflineDeviceMessage> getAllInvalidPendingDeviceMessages() {
        return Collections.unmodifiableList(pendingInvalidDeviceMessages);
    }

    @Override
    @XmlAttribute
    @XmlElement(type = OfflineDeviceMessageImpl.class)
    public List<OfflineDeviceMessage> getAllSentDeviceMessages() {
        return sentDeviceMessages;
    }

    @Override
    public List<OfflineLoadProfile> getLoadProfilesForLoadProfileTypes(List<LoadProfileType> loadProfileTypes) {
        List<Long> ids = loadProfileTypes
                .stream()
                .map(HasId.class::cast)     //Downcast from UPL to CXO
                .map(HasId::getId)
                .collect(Collectors.toList());

        return getAllOfflineLoadProfiles()
                .stream()
                .filter(loadProfile -> ids.contains(loadProfile.getLoadProfileTypeId()))
                .collect(Collectors.toList());
    }

    /**
     * Add the given properties to the {@link #allProperties}
     *
     * @param properties the Properties to add
     */
    void addProperties(TypedProperties properties) {
        if (allProperties == null) {
            allProperties = TypedProperties.empty();
        }
        // adding the SerialNumber as a property value because legacy protocols check the serialNumber based on the property value
        allProperties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER.getName(), getSerialNumber());
        if (properties.getInheritedProperties() != null) {
            allProperties.setAllProperties(properties.getInheritedProperties());
        }
        allProperties.setAllLocalProperties(properties);
    }

    /**
     * Create a list of all the pending DeviceMessages a device has,
     * including his slave devices which have the
     * islogicalSlave flag checked.
     *
     * @param device the Device to collect the pending DeviceMessages from
     * @param deviceTopologies
     * @return a List of pending DeviceMessages
     */
    private List<DeviceMessage> getAllPendingMessagesIncludingSlaves(Device device, Map<Device, List<Device>> deviceTopologies) {
        List<DeviceMessage> allDeviceMessages = new ArrayList<>();
        allDeviceMessages.addAll(device.getMessagesByState(DeviceMessageStatus.PENDING));
        getPhysicalConnectedDevices(device, deviceTopologies).stream().
                filter(this::checkTheNeedToGoOffline).
                forEach(slave -> allDeviceMessages.addAll(getAllPendingMessagesIncludingSlaves(slave, deviceTopologies)));
        return allDeviceMessages;
    }

    /**
     * Create a list of all the sent DeviceMessages a device has,
     * including his slave devices which have the
     * islogicalSlave flag checked.
     *
     * @param device the Device to collect the sent DeviceMessages from
     * @param deviceTopologies
     * @return a List of sent DeviceMessages
     */
    private List<DeviceMessage> getAllSentMessagesIncludingSlaves(Device device, Map<Device, List<Device>> deviceTopologies) {
        List<DeviceMessage> allDeviceMessages = new ArrayList<>();
        allDeviceMessages.addAll(device.getMessagesByState(DeviceMessageStatus.SENT));
        getPhysicalConnectedDevices(device, deviceTopologies).stream().
                filter(this::checkTheNeedToGoOffline).
                forEach(slave -> allDeviceMessages.addAll(getAllSentMessagesIncludingSlaves(slave, deviceTopologies)));
        return allDeviceMessages;
    }

    private List<OfflineDeviceMessage> createOfflineMessageList(final List<DeviceMessage> deviceMessages) {
        return deviceMessages
                .stream()
                .filter(deviceMessage -> Optional.ofNullable(deviceMessage.getSpecification()).isPresent())
                .filter(deviceMessage -> ((Device) deviceMessage.getDevice()).getDeviceProtocolPluggableClass().isPresent()) //Downcast to CXO Device
                .map(this::toOfflineDeviceMessage)
                .collect(Collectors.toList());
    }

    private OfflineDeviceMessageImpl toOfflineDeviceMessage(DeviceMessage deviceMessage) {
        return new OfflineDeviceMessageImpl(
                deviceMessage,
                ((Device) deviceMessage.getDevice()).getDeviceProtocolPluggableClass().get().getDeviceProtocol(),  //Downcast to CXO Device
                serviceProvider.identificationService(),
                serviceProvider.protocolPluggableService(),
                serviceProvider.deviceMessageSpecificationService(),
                this);
    }

    private void setSlaveDevices(final List<OfflineDevice> offlineDevices) {
        this.slaveDevices = offlineDevices;
    }

    private void setMasterLoadProfiles(final List<OfflineLoadProfile> masterLoadProfiles) {
        this.masterLoadProfiles = masterLoadProfiles;
    }

    private void setAllLoadProfiles(final List<OfflineLoadProfile> allLoadProfiles) {
        this.allOfflineLoadProfiles = allLoadProfiles;
    }

    private void setAllLogBooks(List<OfflineLogBook> allLogBooks) {
        this.allOfflineLogBooks = allLogBooks;
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

    @XmlElement(type = DeviceProtocolPluggableClassImpl.class)
    public DeviceProtocolPluggableClass getDeviceProtocolPluggableClass() {
        return deviceProtocolPluggableClass;
    }

    private void setDeviceProtocolPluggableClass(DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        this.deviceProtocolPluggableClass = deviceProtocolPluggableClass;
    }

    @Override
    public String getProtocolName() {
        return ((DeviceProtocolPluggableClassImpl) deviceProtocolPluggableClass).getPluggableClass().getName();
    }

    @Override
    public DeviceProtocolCache getDeviceProtocolCache() {
        return deviceProtocolCache;
    }

    @XmlElements( {
            @XmlElement(type = DeviceIdentifierById.class),
            @XmlElement(type = DeviceIdentifierBySerialNumber.class),
            @XmlElement(type = DeviceIdentifierByMRID.class),
            @XmlElement(type = DeviceIdentifierForAlreadyKnownDevice.class),
            @XmlElement(type = DeviceIdentifierByDeviceName.class),
            @XmlElement(type = DeviceIdentifierByConnectionTypeAndProperty.class),
    })
    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        if (deviceIdentifier == null && serviceProvider.identificationService() != null)
            deviceIdentifier = serviceProvider.identificationService().createDeviceIdentifierForAlreadyKnownDevice(getId(), getmRID());
        return deviceIdentifier;
    }

    @Override
    public List<OfflineCalendar> getCalendars() {
        return Collections.unmodifiableList(calendars);
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
        this.calendars = device.getDeviceType().getAllowedCalendars().stream().map(OfflineCalendarImpl::from).collect(Collectors.toList());
    }

    private void setAllKeyAccessors(final List<OfflineKeyAccessor> allKeyAccessors) {
        this.keyAccessors = allKeyAccessors;
    }

    @Override
    public List<OfflineKeyAccessor> getAllOfflineKeyAccessors() {
        return Collections.unmodifiableList(keyAccessors);
    }

    @Override
    @XmlElement
    @XmlJavaTypeAdapter(MapXmlMarshallAdapter.class)
    public Map<String, TypedProperties> getSecurityPropertySetAttributeToKeyAccessorTypeMapping() {
        return securityPropertySetAttributeToKeyAccessorTypeMapping;
    }

    public void setSecurityPropertySetAttributeToKeyAccessorTypeMapping(HashMap<String, TypedProperties> securityPropertySetAttributeToKeyAccessorTypeMapping) {
        this.securityPropertySetAttributeToKeyAccessorTypeMapping = securityPropertySetAttributeToKeyAccessorTypeMapping;
    }

    private void setSecurityPropertySetAttributeToKeyAccessorType(Device device) {
        securityPropertySetAttributeToKeyAccessorTypeMapping = new HashMap<>();
        device.getDeviceConfiguration().getSecurityPropertySets().forEach(this::addSecurityPropertySetAttributeToKeyAccessorTypeMappings);

    }

    private void addSecurityPropertySetAttributeToKeyAccessorTypeMappings(SecurityPropertySet securityPropertySet) {
        TypedProperties mappings = TypedProperties.empty();
        securityPropertySet.getConfigurationSecurityProperties().forEach(each -> mappings.setProperty(each.getName(), each.getSecurityAccessorType().getName()));
        securityPropertySetAttributeToKeyAccessorTypeMapping.put(securityPropertySet.getName(), mappings);
    }

    public interface ServiceProvider {

        Thesaurus thesaurus();

        ProtocolPluggableService protocolPluggableService();

        DeviceMessageSpecificationService deviceMessageSpecificationService();

        TopologyService topologyService();

        Optional<DeviceCache> findProtocolCacheByDevice(Device device);

        IdentificationService identificationService();

        DeviceConfigurationService deviceConfigurationService();

        FirmwareService firmwareService();

        EventService eventService();
    }
}