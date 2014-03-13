package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.TemporalReference;
import com.elster.jupiter.orm.associations.Temporals;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.exception.MessageSeeds;
import com.energyict.mdc.device.data.impl.constraintvalidators.UniqueName;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.Channel;
import com.energyict.mdc.protocol.api.device.DeviceMultiplier;
import com.energyict.mdc.protocol.api.device.LoadProfile;
import com.energyict.mdc.protocol.api.device.LogBook;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceContext;
import com.google.common.base.Optional;
import com.google.inject.Inject;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{"+ MessageSeeds.Constants.DUPLICATE_DEVICE_EXTERNAL_KEY +"}")
public class DeviceImpl implements Device {

    private final DataModel dataModel;
    private final EventService eventService;
    private final Thesaurus thesaurus;
    private final Clock clock;
    private final MeteringService meteringService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final Reference<DeviceConfiguration> deviceConfiguration = ValueReference.absent();
    private long id;

    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    @Size(min = 1, groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    private String name;

    private String serialNumber;

    private String timeZoneId;
    private TimeZone timeZone;
    private String externalName;
    private Date modificationDate;
    private TemporalReference<CommunicationGatewayReference> communicationGatewayReferenceDevice = Temporals.absent();
    private TemporalReference<PhysicalGatewayReference> physicalGatewayReferenceDevice = Temporals.absent();

    @Inject
    public DeviceImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, MeteringService meteringService, DeviceConfigurationService deviceConfigurationService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.thesaurus = thesaurus;
        this.clock = clock;
        this.meteringService = meteringService;
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Override
    public void save() {
        this.modificationDate = this.clock.now();
        Save.action(this.getId()).save(dataModel, this);
        if (this.id > 0) {
            this.notifyUpdated();
        }
        else {
            this.notifyCreated();
        }
    }

    private void notifyUpdated() {
        this.eventService.postEvent(UpdateEventType.DEVICE.topic(), this);
    }

    private void notifyCreated() {
        this.eventService.postEvent(CreateEventType.DEVICE.topic(), this);
    }

    private void notifyDeleted() {
        this.eventService.postEvent(DeleteEventType.DEVICE.topic(), this);
    }

    @Override
    public void delete() {
        this.validateDelete();
        this.doDelete();
        this.notifyDeleted();
    }

    private void doDelete() {
        this.getDataMapper().remove(this);
    }

    private void validateDelete() {
        // nothing to validate yet
    }

    DeviceImpl initialize(DeviceConfiguration deviceConfiguration, String name){
        this.deviceConfiguration.set(deviceConfiguration);
        setName(name);
        return this;
    }

    @Override
    public DeviceConfiguration getDeviceConfiguration() {
        return this.deviceConfiguration.get();
    }

    @Override
    public DeviceType getDeviceType() {
        return this.getDeviceConfiguration().getDeviceType();
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TimeZone getTimeZone() {
        if(this.timeZone == null){
            if(!Checks.is(timeZoneId).empty() && Arrays.asList(TimeZone.getAvailableIDs()).contains(this.timeZoneId)){
                this.timeZone = TimeZone.getTimeZone(timeZoneId);
            } else {
                return getSystemTimeZone();
            }
        }
        return this.timeZone;
    }

    private TimeZone getSystemTimeZone() {
        List<DefaultSystemTimeZoneFactory> modulesImplementing = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(DefaultSystemTimeZoneFactory.class);
        if (!modulesImplementing.isEmpty()) {
            return modulesImplementing.get(0).getDefaultTimeZone();
        }
        return TimeZone.getDefault();
    }

    @Override
    public void setTimeZone(TimeZone timeZone) {
        if(timeZone != null){
            this.timeZoneId = timeZone.getID();
        } else {
            this.timeZoneId = "";
        }
        this.timeZone = timeZone;
    }

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

    @Override
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Date getModDate() {
        return this.modificationDate;
    }

    @Override
    public List<Channel> getChannels() {
        return Collections.emptyList();
    }

    @Override
    public Channel getChannel(String name) {
        return null;
    }

    @Override
    public Channel getChannel(int index) {
        return null;
    }

    @Override
    public List<Register> getRegisters() {
        List<Register> registers = new ArrayList<>();
        for (RegisterSpec registerSpec : getDeviceConfiguration().getRegisterSpecs()) {
            registers.add(new RegisterImpl(registerSpec, this));
        }
        return registers;
    }

    @Override
    public Register getRegisterWithDeviceObisCode(ObisCode code) {
        for (RegisterSpec registerSpec : getDeviceConfiguration().getRegisterSpecs()) {
            if(registerSpec.getDeviceObisCode().equals(code)){
                return new RegisterImpl(registerSpec, this);
            }
        }
        return null;
    }

    @Override
    public List<BaseDevice> getDownstreamDevices() {
        //TODO search them via a service ...
        return Collections.emptyList();
    }

    @Override
    public Device getPhysicalGateway() {
        Optional<PhysicalGatewayReference> physicalGatewayReferenceOptional = this.physicalGatewayReferenceDevice.effective(clock.now());
        if(physicalGatewayReferenceOptional.isPresent()){
            return physicalGatewayReferenceOptional.get().getPhysicalGateway();
        }
        return null;
    }

    @Override
    public void setPhysicalGateway(BaseDevice gateway){
        if(gateway != null){
            Date currentTime = clock.now();
            terminateTemporal(currentTime, this.physicalGatewayReferenceDevice);
            PhysicalGatewayReferenceImpl physicalGatewayReference = this.dataModel.getInstance(PhysicalGatewayReferenceImpl.class).createFor(Interval.startAt(currentTime), (Device) gateway);
            this.physicalGatewayReferenceDevice.add(physicalGatewayReference);
//            this.dataModel.persist(physicalGatewayReferenceDevice);
        }
    }

    @Override
    public void clearPhysicalGateway() {
        terminateTemporal(clock.now(), this.physicalGatewayReferenceDevice);
    }

    private void terminateTemporal(Date currentTime, TemporalReference<? extends GatewayReference> temporalReference) {
        Optional<? extends GatewayReference> currentGateway = temporalReference.effective(currentTime);
        if(currentGateway.isPresent()){
            currentGateway.get().terminate(currentTime);
            // TODO do we need to do this?
            this.dataModel.update(temporalReference);
        }
    }

    @Override
    public void setCommunicationGateway(Device gateway) {
        if(gateway != null){
            Date currentTime = clock.now();
            terminateTemporal(currentTime, this.communicationGatewayReferenceDevice);
            CommunicationGatewayReferenceImpl communicationGatewayReference = this.dataModel.getInstance(CommunicationGatewayReferenceImpl.class).createFor(Interval.startAt(currentTime), gateway);
            this.communicationGatewayReferenceDevice.add(communicationGatewayReference);
//            this.dataModel.persist(physicalGatewayReferenceDevice);
        }
    }

    @Override
    public void clearCommunicationGateway() {
        terminateTemporal(clock.now(), this.communicationGatewayReferenceDevice);
    }

    @Override
    public Device getCommunicationGateway() {
        Optional<CommunicationGatewayReference> communicationGatewayReferenceOptional = this.communicationGatewayReferenceDevice.effective(clock.now());
        if(communicationGatewayReferenceOptional.isPresent()){
            return communicationGatewayReferenceOptional.get().getCommunicationGateway();
        }
        return null;
    }

    @Override
    public boolean isLogicalSlave() {
        return getDeviceType().isLogicalSlave();
    }

    @Override
    public List<DeviceMessage> getMessages() {
        return Collections.emptyList();
    }

    @Override
    public List<DeviceMessage> getMessagesByState(DeviceMessageStatus status) {
        return Collections.emptyList();
    }

    @Override
    public DeviceProtocolPluggableClass getDeviceProtocolPluggableClass() {
        return getDeviceType().getDeviceProtocolPluggableClass();
    }

    @Override
    public List<LogBook> getLogBooks() {
        //TODO
        return Collections.emptyList();
    }

    @Override
    public List<LoadProfile<Channel>> getLoadProfiles() {
        //TODO
        return Collections.emptyList();
    }

    public void loadProfilesChanged(){

    }

    public TypedProperties getProtocolProperties() {
        TypedProperties properties = TypedProperties.inheritingFrom(this.getDeviceProtocolPluggableClass().getProperties());
        TypedProperties localProperties = getProtocolPropertyPersister().get(this);
        properties.setAllProperties(localProperties);
        return properties;
    }

//    public DeviceProtocolPropertyPersister getProtocolPropertyPersister() {
//        return new DeviceProtocolPropertyPersister(this, "eisrtuprotocolinfo", "rtuid");
//    }

    @Override
    public void store(MeterReading meterReading) {
        Optional<AmrSystem> amrSystem = this.meteringService.findAmrSystem(1);
        if(amrSystem.isPresent()){
            Optional<Meter> holder = amrSystem.get().findMeter(String.valueOf(getId()));
            Meter meter;
            if(!holder.isPresent()){
                // create meter
                if(getExternalName() != null){
                    meter = amrSystem.get().newMeter(String.valueOf(getId()), getExternalName());
                } else {
                    meter = amrSystem.get().newMeter(String.valueOf(getId()));
                }
                meter.save();
            } else {
                meter = holder.get();
            }
            meter.store(meterReading);
        }
    }

    public List<DeviceMultiplier> getDeviceMultipliers() {
        return Collections.emptyList();
    }

    public DeviceMultiplier getDeviceMultiplier(Date date) {
        return null;
    }

    public DeviceMultiplier getDeviceMultiplier() {
        return null;
    }

    @Override
    public OfflineDevice goOffline() {
        return null;
    }

    @Override
    public OfflineDevice goOffline(OfflineDeviceContext context) {
        return null;
    }

    public DataMapper<DeviceImpl> getDataMapper() {
        return this.dataModel.mapper(DeviceImpl.class);
    }

    @Override
    public String getExternalName() {
        return externalName;
    }

    @Override
    public void setExternalName(String externalName) {
        this.externalName = externalName;
    }


}
