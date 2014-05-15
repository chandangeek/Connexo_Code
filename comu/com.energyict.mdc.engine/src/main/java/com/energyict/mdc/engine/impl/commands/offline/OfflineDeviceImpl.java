package com.energyict.mdc.engine.impl.commands.offline;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceCacheFactory;
import com.energyict.mdc.device.data.DeviceMessageFactory;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.BaseRegister;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceContext;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfile;
import com.energyict.mdc.protocol.api.device.offline.OfflineLogBook;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

/**
 * An offline implementation version of an {@link com.energyict.mdc.protocol.api.device.BaseDevice}
 * mainly containing information which is relevant to use at offline-time.
 * <p/>
 * TODO we should add some topology in here, currently all slaves don't point to their correct master
 *
 * @author gna
 * @since 12/04/12 - 13:58
 */
public class OfflineDeviceImpl implements OfflineDevice {

    /**
     * The Device which is going offline
     */
    private final Device device;

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
    private List<OfflineLoadProfile> masterLoadProfiles;

    /**
     * Contains all {@link OfflineLoadProfile offlineLoadProfiles} which are owned by this {@link OfflineDevice} <b>AND</b> OR any slave device
     */
    private List<OfflineLoadProfile> allLoadProfiles;

    /**
     * Contains all {@link OfflineLogBook offlineLogBooks} which are owned by this {@link OfflineDevice}
     */
    private List<OfflineLogBook> allLogBooks;

    /**
     * Contains all {@link OfflineRegister rtuRegisters} which are owned by this {@link OfflineDevice} or a slave which has the
     * {@link com.energyict.mdc.device.config.DeviceType#isLogicalSlave() rtuType.isLogicalSlave} checked
     */
    private List<OfflineRegister> allRegisters;

    /**
     * Contains all {@link DeviceMessageStatus#PENDING pending} {@link OfflineDeviceMessage}
     */
    private List<OfflineDeviceMessage> pendingDeviceMessages;
    /**
     * Contains all {@link DeviceMessageStatus#SENT sent} {@link OfflineDeviceMessage}
     */
    private List<OfflineDeviceMessage> sentDeviceMessages;
    /**
     * The used DeviceProtocolPluggableClass
     */
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;

    /**
     * The used DeviceProtocolCache for this Device
     */
    private DeviceProtocolCache deviceProtocolCache;

    public OfflineDeviceImpl(final Device device, OfflineDeviceContext offlineDeviceContext) {
        this.device = device;
        goOffline(offlineDeviceContext);
    }

    /**
     * Triggers the capability to go offline and will copy all information
     * from the database into memory so that normal business operations can continue.<br>
     * Note that this may cause recursive calls to other objects that can go offline.
     */
    private void goOffline(OfflineDeviceContext context) {
        setId((int) this.device.getId());
        setSerialNumber(this.device.getSerialNumber());
        this.allProperties = TypedProperties.empty();

        if (this.device.getDeviceProtocolPluggableClass() != null) {
            setDeviceProtocolPluggableClass(this.device.getDeviceProtocolPluggableClass());
        }

        addProperties(TypedProperties.empty());
        addProperties(this.device.getDeviceProtocolProperties());

        if (context.needsSlaveDevices()) {
            List<BaseDevice<Channel,LoadProfile,Register>> downstreamDevices = this.device.getPhysicalConnectedDevices();
            List<Device> downStreamEndDevices = new ArrayList<>(downstreamDevices.size());
            for (BaseDevice downstreamDevice : downstreamDevices) {
                downStreamEndDevices.add((Device) downstreamDevice);
            }
            setSlaveDevices(convertToOfflineRtus(downStreamEndDevices));
        }
        if (context.needsMasterLoadProfiles()) {
            setMasterLoadProfiles(convertToOfflineLoadProfiles(this.device.getLoadProfiles()));
        }
        if (context.needsAllLoadProfiles()) {
            setAllLoadProfiles(convertToOfflineLoadProfiles((List)getAllLoadProfilesIncludingDownStreams(this.device)));
        }
        if (context.needsLogBooks()) {
            setAllLogBooks(convertToOfflineLogBooks((List)this.device.getLogBooks()));
        }
        if (context.needsRegisters()) {
            setAllRegisters(convertToOfflineRegister((List)createCompleteRegisterList()));
        }
        if (context.needsPendingMessages()) {
            setAllPendingMessages(createPendingMessagesList());
        }
        if (context.needsSentMessages()) {
            setAllSentMessages(createSentMessagesList());
        }
        setDeviceCache();
    }

    /**
     * We get the cache from the DataBase. The object will only be set if it is an instance of {@link DeviceProtocolCache}.
     * Otherwise an nullObject will be provided to the {@link DeviceProtocol} so it can
     * be refetched from the Device.
     */
    private void setDeviceCache() {
        List<DeviceCacheFactory> modulesImplementing = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(DeviceCacheFactory.class);
        if(!modulesImplementing.isEmpty()) {
            this.deviceProtocolCache = modulesImplementing.get(0).findProtocolCacheByDeviceId(getId());
        }
    }

    /**
     * Get a List of all RtuRegisters, including those of the slave devices with the needsProxy checked.
     *
     * @return the list of all RtuRegisters
     */
    private List<BaseRegister> createCompleteRegisterList() {
        List<BaseRegister> registers = new ArrayList<>();
        registers.addAll(this.device.getRegisters());
        for (BaseDevice<?,?,?> slave : this.device.getPhysicalConnectedDevices()) {
            if (checkTheNeedToGoOffline((Device) slave)) {
                registers.addAll(slave.getRegisters());
            }
        }
        return registers;
    }

    /**
     * Create a <CODE>List</CODE> of all the <CODE>LoadProfiles</CODE> an <CODE>Device</CODE> has,
     * including his {@link com.energyict.mdc.protocol.api.device.BaseDevice#getPhysicalConnectedDevices()} which have the
     * {@link com.energyict.mdc.device.config.DeviceType#isLogicalSlave()} flag checked.
     *
     * @param rtu the <CODE>Device</CODE> to collect the <CODE>LoadProfiles</CODE> from
     * @return a List of <CODE>LoadProfiles</CODE>
     */
    private List<BaseLoadProfile<Channel>> getAllLoadProfilesIncludingDownStreams(Device rtu) {
        ArrayList<BaseLoadProfile<Channel>> allLoadProfiles = new ArrayList<BaseLoadProfile<Channel>>(rtu.getLoadProfiles());
        for (BaseDevice slave : rtu.getPhysicalConnectedDevices()) {
            if (checkTheNeedToGoOffline((Device) slave)) {
                for (BaseLoadProfile<Channel> lp : getAllLoadProfilesIncludingDownStreams((Device) slave)) {
                    if (lp.getLoadProfileTypeObisCode().anyChannel()) {
                        allLoadProfiles.add(lp);
                    } else {
                        boolean exists = false;
                        for (BaseLoadProfile<Channel> lpListItem : allLoadProfiles) {
                            if (lp.getLoadProfileTypeObisCode().equals(lpListItem.getLoadProfileTypeObisCode())) {
                                exists = true;
                            }
                        }
                        if (!exists) {
                            allLoadProfiles.add(lp);
                        }
                    }
                }
            }
        }
        return allLoadProfiles;
    }

    /**
     * Converts the given {@link com.energyict.mdc.protocol.api.device.BaseDevice rtus} to {@link OfflineDevice offlineRtus} if they have the option
     * {@link com.energyict.mdc.device.config.DeviceType#isLogicalSlave()} checked.
     *
     *
     * @param downstreamRtus the rtus to go offline
     * @return a list of {@link OfflineDevice offlineRtus}
     */
    private List<OfflineDevice> convertToOfflineRtus(final List<Device> downstreamRtus) {
        List<OfflineDevice> offlineSlaves = new ArrayList<>(downstreamRtus.size());
        for (Device downstreamRtu : downstreamRtus) {
            OfflineDevice offlineDevice = new OfflineDeviceImpl(downstreamRtu, new DeviceOfflineFlags());
            offlineSlaves.add(offlineDevice);
            offlineSlaves.addAll(offlineDevice.getAllSlaveDevices());
        }
        return offlineSlaves;
    }

    private boolean checkTheNeedToGoOffline(Device downstreamRtu) {
        return downstreamRtu.getDeviceType().isLogicalSlave();
    }

    private List<OfflineLoadProfile> convertToOfflineLoadProfiles(final List<LoadProfile> loadProfiles){
        List<OfflineLoadProfile> offlineLoadProfiles = new ArrayList<>(loadProfiles.size());
        for (LoadProfile loadProfile : loadProfiles) {
            offlineLoadProfiles.add(new OfflineLoadProfileImpl(loadProfile));
        }
        return offlineLoadProfiles;
    }

    private List<OfflineLogBook> convertToOfflineLogBooks(final List<LogBook> logBooks){
        List<OfflineLogBook> offlineLogBooks = new ArrayList<>(logBooks.size());
        for (LogBook logBook : logBooks) {
            offlineLogBooks.add(new OfflineLogBookImpl(logBook));
        }
        return offlineLogBooks;
    }

    private List<OfflineRegister> convertToOfflineRegister(final List<Register> registers){
        List<OfflineRegister> offlineRegisters = new ArrayList<>(registers.size());
        for (Register register : registers) {
            offlineRegisters.add(new OfflineRegisterImpl(register));
        }
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
    public List<OfflineLogBook> getAllOfflineLogBooks() {
        return this.allLogBooks;
    }

    @Override
    public List<OfflineRegister> getAllRegisters() {
        return allRegisters;
    }

    @Override
    public List<OfflineRegister> getRegistersForRegisterGroup(final List<Integer> deviceRegisterGroupIds) {
        List<OfflineRegister> filteredRegisters = new ArrayList<>();
        for (OfflineRegister register : getAllRegisters()) {
            if (deviceRegisterGroupIds.contains(register.getRegisterGroupId())) {
                filteredRegisters.add(register);
            }
        }
        return filteredRegisters;
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

    private List<OfflineDeviceMessage> createPendingMessagesList() {
        return createOfflineMessageList(getDeviceMessageFactory().findByDeviceAndState(device, DeviceMessageStatus.PENDING));
    }

    private List<OfflineDeviceMessage> createSentMessagesList() {
        return createOfflineMessageList(getDeviceMessageFactory().findByDeviceAndState(device, DeviceMessageStatus.SENT));
    }

    private List<OfflineDeviceMessage> createOfflineMessageList(final List<DeviceMessage> deviceMessages) {
        List<OfflineDeviceMessage> offlineDeviceMessages = new ArrayList<>(deviceMessages.size());
        for (DeviceMessage deviceMessage : deviceMessages) {
            offlineDeviceMessages.add((OfflineDeviceMessage) deviceMessage.goOffline());
        }
        return offlineDeviceMessages;
    }

    private void setId(final int id) {
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

    private DeviceMessageFactory getDeviceMessageFactory() {
        List<DeviceMessageFactory> modulesImplementing = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(DeviceMessageFactory.class);
        if(!modulesImplementing.isEmpty()){
            return modulesImplementing.get(0);
        }
        return null;
    }

    public DeviceProtocolPluggableClass getDeviceProtocolPluggableClass() {
        return deviceProtocolPluggableClass;
    }

    @Override
    public DeviceProtocolCache getDeviceProtocolCache() {
        return deviceProtocolCache;
    }

    private void setDeviceProtocolPluggableClass(DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        this.deviceProtocolPluggableClass = deviceProtocolPluggableClass;
    }
}
