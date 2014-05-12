package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.TemporalReference;
import com.elster.jupiter.orm.associations.Temporals;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.PartialConnectionInitiationTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialOutboundConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.DefaultSystemTimeZoneFactory;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceCacheFactory;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.DeviceDependant;
import com.energyict.mdc.device.data.DeviceInComSchedule;
import com.energyict.mdc.device.data.DeviceProtocolProperty;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.exceptions.CannotDeleteComTaskExecutionWhichIsNotFromThisDevice;
import com.energyict.mdc.device.data.exceptions.CannotDeleteConnectionTaskWhichIsNotFromThisDevice;
import com.energyict.mdc.device.data.exceptions.DeviceProtocolPropertyException;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.exceptions.ProtocolDialectConfigurationPropertiesIsRequiredException;
import com.energyict.mdc.device.data.exceptions.StillGatewayException;
import com.energyict.mdc.device.data.impl.constraintvalidators.UniqueMrid;
import com.energyict.mdc.device.data.impl.offline.DeviceOffline;
import com.energyict.mdc.device.data.impl.offline.OfflineDeviceImpl;
import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionInitiationTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.InboundConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ScheduledConnectionTaskImpl;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionInitiationTask;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.BaseChannel;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseLogBook;
import com.energyict.mdc.protocol.api.device.DeviceMultiplier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceContext;
import com.energyict.mdc.scheduling.TemporalExpression;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import javax.inject.Provider;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@UniqueMrid(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.DUPLICATE_DEVICE_MRID + "}")
public class DeviceImpl implements Device, PersistenceAware {

    enum Fields {
        COM_SCHEDULE_USAGES("comScheduleUsages");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    private final DataModel dataModel;
    private final EventService eventService;
    private final Thesaurus thesaurus;
    private final Clock clock;
    private final MeteringService meteringService;
    private final DeviceDataService deviceDataService;
    private final List<LoadProfile> loadProfiles = new ArrayList<>();
    private final List<LogBook> logBooks = new ArrayList<>();
    private final Reference<DeviceConfiguration> deviceConfiguration = ValueReference.absent();
    private final List<DeviceInComSchedule> comScheduleUsages = new ArrayList<>();
    private long id;

    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    @Size(min = 1, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    private String name;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.MRID_REQUIRED_KEY + "}")
    @Size(min = 1, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.MRID_REQUIRED_KEY + "}")
    private String mRID;

    private String serialNumber;
    private String timeZoneId;
    private TimeZone timeZone;
    private Date modificationDate;
    private Date yearOfCertification;

    @Valid
    private TemporalReference<CommunicationGatewayReference> communicationGatewayReferenceDevice = Temporals.absent();
    @Valid
    private TemporalReference<PhysicalGatewayReference> physicalGatewayReferenceDevice = Temporals.absent();
    @Valid
    private List<DeviceProtocolProperty> deviceProperties = new ArrayList<>();
    @Valid
    private List<ConnectionTaskImpl<?,?>> connectionTasks = new ArrayList<>();
    @Valid
    private List<ComTaskExecutionImpl> comTaskExecutions = new ArrayList<>();
    @Valid
    private List<ProtocolDialectProperties> dialectPropertiesList = new ArrayList<>();
    private List<ProtocolDialectProperties> newDialectProperties = new ArrayList<>();
    private List<ProtocolDialectProperties> dirtyDialectProperties = new ArrayList<>();

    private final Provider<ScheduledConnectionTaskImpl> scheduledConnectionTaskProvider;
    private final Provider<InboundConnectionTaskImpl> inboundConnectionTaskProvider;
    private final Provider<ConnectionInitiationTaskImpl> connectionInitiationTaskProvider;
    private final Provider<ComTaskExecutionImpl> comTaskExecutionProvider;

    @Inject
    public DeviceImpl(DataModel dataModel,
                      EventService eventService,
                      Thesaurus thesaurus,
                      Clock clock,
                      MeteringService meteringService,
                      DeviceDataService deviceDataService,
                      Provider<ScheduledConnectionTaskImpl> scheduledConnectionTaskProvider,
                      Provider<InboundConnectionTaskImpl> inboundConnectionTaskProvider,
                      Provider<ConnectionInitiationTaskImpl> connectionInitiationTaskProvider,
                      Provider<ComTaskExecutionImpl> comTaskExecutionProvider) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.thesaurus = thesaurus;
        this.clock = clock;
        this.meteringService = meteringService;
        this.deviceDataService = deviceDataService;
        this.scheduledConnectionTaskProvider = scheduledConnectionTaskProvider;
        this.inboundConnectionTaskProvider = inboundConnectionTaskProvider;
        this.connectionInitiationTaskProvider = connectionInitiationTaskProvider;
        this.comTaskExecutionProvider = comTaskExecutionProvider;
    }

    @Override
    public void save() {
        this.modificationDate = this.clock.now();
        if (this.id > 0) {
            Save.UPDATE.save(dataModel, this);
            this.saveNewAndDirtyDialectProperties();
            this.notifyUpdated();
        } else {
            Save.CREATE.save(dataModel, this);
            this.saveNewDialectProperties();
            this.notifyCreated();
        }
        this.saveAllConnectionTasks();
        this.saveAllComTaskExecutions();
    }

    private void saveAllComTaskExecutions() {
        for (ComTaskExecutionImpl comTaskExecution : comTaskExecutions) {
            comTaskExecution.save();
        }
    }

    private void saveAllConnectionTasks() {
        for (ConnectionTaskImpl<?,?> connectionTask : connectionTasks) {
            connectionTask.save();
        }
    }

    private void saveNewAndDirtyDialectProperties() {
        this.saveNewDialectProperties();
        this.saveDirtyDialectProperties();
    }

    private void saveDirtyDialectProperties() {
        this.saveDialectProperties(this.dirtyDialectProperties);
        this.dirtyDialectProperties = new ArrayList<>();
    }

    private void saveNewDialectProperties() {
        this.saveDialectProperties(this.newDialectProperties);
        this.dialectPropertiesList.addAll(this.newDialectProperties);
        this.newDialectProperties = new ArrayList<>();
    }

    private void saveDialectProperties(List<ProtocolDialectProperties> dialectProperties) {
        for (ProtocolDialectProperties newDialectProperty : dialectProperties) {
            this.save((ProtocolDialectPropertiesImpl) newDialectProperty);
        }
    }

    private void save (ProtocolDialectPropertiesImpl dialectProperties) {
        dialectProperties.save();
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
        this.notifyDeviceIsGoingToBeDeleted();
        this.doDelete();
        this.notifyDeleted();
    }

    private void notifyDeviceIsGoingToBeDeleted() {
        // todo create event instead!
        List<DeviceDependant> modulesImplementing = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(DeviceDependant.class);
        for (DeviceDependant deviceDependant : modulesImplementing) {
            deviceDependant.notifyDeviceDelete(this);
        }
    }

    private void doDelete() {
        deleteProperties();
        deleteCache();
        deleteLoadProfiles();
        deleteLogBooks();
        deleteConnectionTasks();
        deleteComTaskExecutions();
        // TODO delete messages
        this.getDataMapper().remove(this);
    }

    private void deleteComTaskExecutions() {
        for (ComTaskExecution comTaskExecution : this.deviceDataService.findAllComTaskExecutionsIncludingObsoleteForDevice(this)) {
            ((ComTaskExecutionImpl) comTaskExecution).delete();
        }
    }

    private void deleteConnectionTasks() {
        for (ConnectionTaskImpl<?,?> connectionTask : connectionTasks) {
            connectionTask.delete();
        }
    }

    private void deleteLogBooks() {
        this.logBooks.clear();
    }

    private void deleteLoadProfiles() {
        this.loadProfiles.clear();
    }

    private void deleteCache() {
        List<DeviceCacheFactory> deviceCacheFactories = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(DeviceCacheFactory.class);
        if (!deviceCacheFactories.isEmpty()) {
            deviceCacheFactories.get(0).removeDeviceCacheFor(getId());
        }
    }

    private void deleteProperties() {
        this.deviceProperties.clear();
    }

    private void validateDelete() {
        validateGatewayUsage();
    }

    private void validateGatewayUsage() {
        List<BaseDevice<Channel, LoadProfile, Register>> physicalConnectedDevices = getPhysicalConnectedDevices();
        if (!physicalConnectedDevices.isEmpty()) {
            throw StillGatewayException.forPhysicalGateway(thesaurus, this, physicalConnectedDevices.toArray(new Device[physicalConnectedDevices.size()]));
        }
        List<BaseDevice<Channel, LoadProfile, Register>> communicationReferencingDevices = getCommunicationReferencingDevices();
        if (!communicationReferencingDevices.isEmpty()) {
            throw StillGatewayException.forCommunicationGateway(thesaurus, this, communicationReferencingDevices.toArray(new Device[communicationReferencingDevices.size()]));

        }
    }

    DeviceImpl initialize(DeviceConfiguration deviceConfiguration, String name, String mRID) {
        this.deviceConfiguration.set(deviceConfiguration);
        setName(name);
        this.mRID = mRID;
        createLoadProfiles();
        createLogBooks();
        return this;
    }

    private void createLoadProfiles() {
        for (LoadProfileSpec loadProfileSpec : this.getDeviceConfiguration().getLoadProfileSpecs()) {
            this.loadProfiles.add(this.dataModel.getInstance(LoadProfileImpl.class).initialize(loadProfileSpec, this));
        }
    }

    private void createLogBooks() {
        for (LogBookSpec logBookSpec : this.getDeviceConfiguration().getLogBookSpecs()) {
            this.logBooks.add(this.dataModel.getInstance(LogBookImpl.class).initialize(logBookSpec, this));
        }
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
        if (this.timeZone == null) {
            if (!Checks.is(timeZoneId).empty() && Arrays.asList(TimeZone.getAvailableIDs()).contains(this.timeZoneId)) {
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
        if (timeZone != null) {
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

    @Override
    public void setYearOfCertification(Date yearOfCertification) {
        this.yearOfCertification = yearOfCertification;
    }

    @Override
    public Date getYearOfCertification() {
        return yearOfCertification;
    }

    public Date getModDate() {
        return this.modificationDate;
    }

    @Override
    public List<Channel> getChannels() {
        List<Channel> channels = new ArrayList<>();
        for (LoadProfile loadProfile : loadProfiles) {
            channels.addAll(loadProfile.getChannels());
        }
        return channels;
    }

    @Override
    public BaseChannel getChannel(String name) {
        for (Channel channel : getChannels()) {
            if (channel.getChannelSpec().getName().equals(name)) {
                return channel;
            }
        }
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
            if (registerSpec.getDeviceObisCode().equals(code)) {
                return new RegisterImpl(registerSpec, this);
            }
        }
        return null;
    }

    @Override
    public List<BaseDevice<Channel, LoadProfile, Register>> getPhysicalConnectedDevices() {
        return this.deviceDataService.findPhysicalConnectedDevicesFor(this);
    }

    @Override
    public Device getPhysicalGateway() {
        Optional<PhysicalGatewayReference> physicalGatewayReferenceOptional = this.physicalGatewayReferenceDevice.effective(clock.now());
        if (physicalGatewayReferenceOptional.isPresent()) {
            return physicalGatewayReferenceOptional.get().getPhysicalGateway();
        }
        return null;
    }

    private void topologyChanged() {
        List<ComTaskExecution> comTasksForDefaultConnectionTask = this.deviceDataService.findComTasksByDefaultConnectionTask(this);
        Device gateway = this.getPhysicalGateway();
            if (gateway != null) {
                updateComTasksToUseNewDefaultConnectionTask(comTasksForDefaultConnectionTask);
            } else {
                updateComTasksToUseNonExistingDefaultConnectionTask(comTasksForDefaultConnectionTask);
            }

    }

    private void updateComTasksToUseNonExistingDefaultConnectionTask(List<ComTaskExecution> comTasksForDefaultConnectionTask) {
        for (ComTaskExecution comTaskExecution : comTasksForDefaultConnectionTask) {
            ComTaskExecution.ComTaskExecutionUpdater comTaskExecutionUpdater = getComTaskExecutionUpdater(comTaskExecution);
            comTaskExecutionUpdater.setConnectionTask(null);
            comTaskExecutionUpdater.setUseDefaultConnectionTaskFlag(true);
            comTaskExecutionUpdater.update();
        }
    }

    private void updateComTasksToUseNewDefaultConnectionTask(List<ComTaskExecution> comTasksForDefaultConnectionTask){
        ConnectionTask<?,?> defaultConnectionTaskForGateway = getDefaultConnectionTask();
        for (ComTaskExecution comTaskExecution : comTasksForDefaultConnectionTask) {
            ComTaskExecution.ComTaskExecutionUpdater comTaskExecutionUpdater = getComTaskExecutionUpdater(comTaskExecution);
            comTaskExecutionUpdater.setUseDefaultConnectionTask(defaultConnectionTaskForGateway);
            comTaskExecutionUpdater.update();
        }
    }

    private ConnectionTask<?, ?> getDefaultConnectionTask() {
        for (ConnectionTaskImpl<?, ?> connectionTask : connectionTasks) {
            if(connectionTask.isDefault()){
                return connectionTask;
            }
        }
        return null;
    }

    @Override
    public void setPhysicalGateway(BaseDevice gateway) {
        if (gateway != null) {
            Date currentTime = clock.now();
            terminateTemporal(currentTime, this.physicalGatewayReferenceDevice);
            PhysicalGatewayReferenceImpl physicalGatewayReference = this.dataModel.getInstance(PhysicalGatewayReferenceImpl.class).createFor(Interval.startAt(currentTime), (Device) gateway, this);
            savePhysicalGateway(physicalGatewayReference);
            topologyChanged();
        }
    }

    private void savePhysicalGateway(PhysicalGatewayReferenceImpl physicalGatewayReference) {
        Save.action(getId()).validate(this.dataModel, physicalGatewayReference);
        this.physicalGatewayReferenceDevice.add(physicalGatewayReference);
    }

    @Override
    public void clearPhysicalGateway() {
        terminateTemporal(clock.now(), this.physicalGatewayReferenceDevice);
        topologyChanged();
    }

    private void terminateTemporal(Date currentTime, TemporalReference<? extends GatewayReference> temporalReference) {
        Optional<? extends GatewayReference> currentGateway = temporalReference.effective(currentTime);
        if (currentGateway.isPresent()) {
            GatewayReference gateway = currentGateway.get();
            gateway.terminate(currentTime);
            this.dataModel.update(gateway);
        }
    }

    @Override
    public void setCommunicationGateway(Device gateway) {
        if (gateway != null) {
            Date currentTime = clock.now();
            terminateTemporal(currentTime, this.communicationGatewayReferenceDevice);
            CommunicationGatewayReferenceImpl communicationGatewayReference = this.dataModel.getInstance(CommunicationGatewayReferenceImpl.class).createFor(Interval.startAt(currentTime), gateway, this);
            saveCommunicationGateway(communicationGatewayReference);
            topologyChanged();
        }
    }

    private void saveCommunicationGateway(CommunicationGatewayReferenceImpl communicationGatewayReference) {
        Save.action(getId()).validate(this.dataModel, communicationGatewayReference);
        this.communicationGatewayReferenceDevice.add(communicationGatewayReference);
    }

    @Override
    public void clearCommunicationGateway() {
        terminateTemporal(clock.now(), this.communicationGatewayReferenceDevice);
        topologyChanged();
    }

    @Override
    public List<BaseDevice<Channel, LoadProfile, Register>> getCommunicationReferencingDevices() {
        return this.deviceDataService.findCommunicationReferencingDevicesFor(this);
    }

    @Override
    public Device getCommunicationGateway() {
        Optional<CommunicationGatewayReference> communicationGatewayReferenceOptional = this.communicationGatewayReferenceDevice.effective(clock.now());
        if (communicationGatewayReferenceOptional.isPresent()) {
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
    public List<BaseLogBook> getLogBooks() {
        return Collections.<BaseLogBook>unmodifiableList(this.logBooks);
    }

    @Override
    public LogBook.LogBookUpdater getLogBookUpdaterFor(LogBook logBook) {
        return new LogBookUpdaterForDevice((LogBookImpl) logBook);
    }

    @Override
    public void postLoad() {
        loadConnectionTasks();
        loadComTaskExecutions();
    }

    private void loadComTaskExecutions() {
        this.comTaskExecutions = getComTAskExecutionImpls();
    }

    private List<ComTaskExecutionImpl> getComTAskExecutionImpls() {
        List<ComTaskExecutionImpl> comTaskExecutionImpls = new ArrayList<>();
        for (ComTaskExecution comTaskExecution : this.deviceDataService.findComTaskExecutionsByDevice(this)) {
            comTaskExecutionImpls.add((ComTaskExecutionImpl) comTaskExecution);
        }
        return comTaskExecutionImpls;
    }

    private void loadConnectionTasks() {
        this.connectionTasks = getConnectionTaskImpls();
    }

    private List<ConnectionTaskImpl<?,?>> getConnectionTaskImpls() {
        List<ConnectionTaskImpl<?,?>> connectionTaskImpls = new ArrayList<>();
        for (ConnectionTask connectionTask : this.deviceDataService.findConnectionTasksByDevice(this)) {
            connectionTaskImpls.add((ConnectionTaskImpl<?,?>) connectionTask);
        }
        return connectionTaskImpls;
    }

    class LogBookUpdaterForDevice extends LogBookImpl.LogBookUpdater {

        protected LogBookUpdaterForDevice(LogBookImpl logBook) {
            super(logBook);
        }
    }

    public List<LoadProfile> getLoadProfiles() {
        return Collections.unmodifiableList(this.loadProfiles);
    }

    @Override
    public LoadProfile.LoadProfileUpdater getLoadProfileUpdaterFor(LoadProfile loadProfile) {
        return new LoadProfileUpdaterForDevice((LoadProfileImpl) loadProfile);
    }

    class LoadProfileUpdaterForDevice extends LoadProfileImpl.LoadProfileUpdater {

        protected LoadProfileUpdaterForDevice(LoadProfileImpl loadProfile) {
            super(loadProfile);
        }
    }

    public TypedProperties getDeviceProtocolProperties() {
        TypedProperties properties = TypedProperties.inheritingFrom(this.getDeviceProtocolPluggableClass().getProperties());
        TypedProperties localProperties = getLocalProperties(this.getDeviceProtocolPluggableClass().getDeviceProtocol().getPropertySpecs());
        properties.setAllProperties(localProperties);
        return properties;
    }


    private String getPropertyValue(String name, Object value) {
        String propertyValue = null;
        for (PropertySpec propertySpec : this.getDeviceProtocolPluggableClass().getDeviceProtocol().getPropertySpecs()) {
            if (propertySpec.getName().equals(name)) {
                propertyValue = propertySpec.getValueFactory().toStringValue(value);
            }
        }
        return propertyValue;
    }

    private boolean propertyExistsOnDeviceProtocol(String name) {
        for (PropertySpec propertySpec : this.getDeviceProtocolPluggableClass().getDeviceProtocol().getPropertySpecs()) {
            if (propertySpec.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<ProtocolDialectProperties> getProtocolDialectPropertiesList() {
        List<ProtocolDialectProperties> all = new ArrayList<>(this.dialectPropertiesList.size() + this.newDialectProperties.size());
        all.addAll(this.dialectPropertiesList);
        all.addAll(this.newDialectProperties);
        return all;
    }

    @Override
    public ProtocolDialectProperties getProtocolDialectProperties(String dialectName) {
        ProtocolDialectProperties dialectProperties = this.getProtocolDialectPropertiesFrom(dialectName, this.dialectPropertiesList);
        if (dialectProperties != null) {
            return dialectProperties;
        }
        else {
            // Attempt to find the dialect properties in the list of new ones that have not been saved yet
            return this.getProtocolDialectPropertiesFrom(dialectName, this.newDialectProperties);
        }
    }

    private ProtocolDialectProperties getProtocolDialectPropertiesFrom(String dialectName, List<ProtocolDialectProperties> propertiesList) {
        for (ProtocolDialectProperties properties : propertiesList) {
            if (properties.getDeviceProtocolDialectName().equals(dialectName)) {
                return properties;
            }
        }
        return null;
    }

    @Override
    public void setProtocolDialectProperty(String dialectName, String propertyName, Object value) {
        ProtocolDialectProperties dialectProperties = this.getProtocolDialectProperties(dialectName);
        if (dialectProperties == null) {
            ProtocolDialectConfigurationProperties configurationProperties = this.getProtocolDialectConfigurationProperties(dialectName);
            if (configurationProperties != null) {
                dialectProperties = this.dataModel.getInstance(ProtocolDialectPropertiesImpl.class).initialize(this, configurationProperties);
                dialectProperties.setProperty(propertyName, value);
                this.newDialectProperties.add(dialectProperties);
            }
            else {
                throw new ProtocolDialectConfigurationPropertiesIsRequiredException(this.thesaurus);
            }
        }
        else {
            dialectProperties.setProperty(propertyName, value);
            this.dirtyDialectProperties.add(dialectProperties);
        }
    }

    private ProtocolDialectConfigurationProperties getProtocolDialectConfigurationProperties(String dialectName) {
        List<ProtocolDialectConfigurationProperties> allConfigurationProperties =
                this.getDeviceConfiguration().getCommunicationConfiguration().getProtocolDialectConfigurationPropertiesList();
        for (ProtocolDialectConfigurationProperties configurationProperties : allConfigurationProperties) {
            if (configurationProperties.getDeviceProtocolDialectName().equals(dialectName)) {
                return configurationProperties;
            }
        }
        return null;
    }

    @Override
    public void removeProtocolDialectProperty(String dialectName, String propertyName) {
        ProtocolDialectProperties dialectProperties = this.getProtocolDialectProperties(dialectName);
        if (dialectProperties != null) {
            dialectProperties.removeProperty(propertyName);
            if (dialectProperties.getTypedProperties().localSize() == 0) {
                // No properties left
                this.removeProtocolDialectProperty(dialectProperties);
            }
        }
    }

    private void removeProtocolDialectProperty(ProtocolDialectProperties dialectProperties) {
        if (dialectProperties.getId() == 0) {
            // Not persistent, must be in the list of new properties
            this.newDialectProperties.remove(dialectProperties);
        }
        else {
            // Persistent, remove if from the managed list of properties, which will delete it from the database
            this.dialectPropertiesList.remove(dialectProperties);
        }
    }

    @Override
    public void setProperty(String name, Object value) {
        if (propertyExistsOnDeviceProtocol(name)) {
            String propertyValue = getPropertyValue(name, value);
            boolean updated = updatePropertyIfExists(name, propertyValue);
            if (!updated) {
                addDeviceProperty(name, propertyValue);
            }
        } else {
            throw DeviceProtocolPropertyException.propertyDoesNotExistForDeviceProtocol(thesaurus, name, this.getDeviceProtocolPluggableClass().getDeviceProtocol(), this);
        }
    }

    private void addDeviceProperty(String name, String propertyValue) {
        if (propertyValue != null) {
            InfoType infoType = this.deviceDataService.findInfoType(name);
            DeviceProtocolPropertyImpl deviceProtocolProperty = this.dataModel.getInstance(DeviceProtocolPropertyImpl.class).initialize(this, infoType, propertyValue);
            Save.CREATE.validate(dataModel, deviceProtocolProperty);
            this.deviceProperties.add(deviceProtocolProperty);
        }
    }

    private boolean updatePropertyIfExists(String name, String propertyValue) {
        for (DeviceProtocolProperty deviceProperty : deviceProperties) {
            if (deviceProperty.getName().equals(name)) {
                deviceProperty.setValue(propertyValue);
                deviceProperty.update();
                return true;
            }
        }
        return false;
    }

    @Override
    public void removeProperty(String name) {
        for (DeviceProtocolProperty deviceProtocolProperty : deviceProperties) {
            if (deviceProtocolProperty.getName().equals(name)) {
                this.deviceProperties.remove(deviceProtocolProperty);
                break;
            }
        }
    }

    private TypedProperties getLocalProperties(List<PropertySpec> propertySpecs) {
        TypedProperties properties = TypedProperties.empty();
        for (PropertySpec propertySpec : propertySpecs) {
            DeviceProtocolProperty deviceProtocolProperty = findDevicePropertyFor(propertySpec);
            if (deviceProtocolProperty != null) {
                properties.setProperty(deviceProtocolProperty.getName(), propertySpec.getValueFactory().fromStringValue(deviceProtocolProperty.getPropertyValue()));
            }
        }
        return properties;
    }

    private DeviceProtocolProperty findDevicePropertyFor(PropertySpec propertySpec) {
        for (DeviceProtocolProperty deviceProperty : this.deviceProperties) {
            if (deviceProperty.getName().equals(propertySpec.getName())) {
                return deviceProperty;
            }
        }
        return null;
    }

    @Override
    public void store(MeterReading meterReading) {
        Optional<AmrSystem> amrSystem = getMdcAmrSystem();
        if (amrSystem.isPresent()) {
            Meter meter = findOrCreateMeterInKore(amrSystem);
            meter.store(meterReading);
        }
    }

    private Meter findOrCreateMeterInKore(Optional<AmrSystem> amrSystem) {
        Optional<Meter> holder = amrSystem.get().findMeter(String.valueOf(getId()));
        Meter meter;
        if (!holder.isPresent()) {
            // create meter
            meter = amrSystem.get().newMeter(String.valueOf(getId()), getmRID());
            meter.save();
        } else {
            meter = holder.get();
        }
        return meter;
    }

    private Optional<AmrSystem> getMdcAmrSystem() {
        return this.meteringService.findAmrSystem(1);
    }

    List<ReadingRecord> getReadingsFor(Register register, Interval interval) {
        Optional<AmrSystem> amrSystem = getMdcAmrSystem();
        if (amrSystem.isPresent()) {
            Meter meter = findOrCreateMeterInKore(amrSystem);
            List<? extends BaseReadingRecord> readings = meter.getReadings(interval, register.getRegisterSpec().getRegisterMapping().getReadingType());
            List<ReadingRecord> readingRecords = new ArrayList<>(readings.size());
            for (BaseReadingRecord reading : readings) {
                readingRecords.add((ReadingRecord) reading);
            }
            return readingRecords;
        }
        return Collections.emptyList();
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
        return new OfflineDeviceImpl(this, DeviceOffline.needsEverything);
    }

    @Override
    public OfflineDevice goOffline(OfflineDeviceContext context) {
        return new OfflineDeviceImpl(this, context);
    }

    public DataMapper<DeviceImpl> getDataMapper() {
        return this.dataModel.mapper(DeviceImpl.class);
    }

    @Override
    public String getmRID() {
        return mRID;
    }

    @Override
    public ScheduledConnectionTaskBuilder getScheduledConnectionTaskBuilder(PartialScheduledConnectionTask partialScheduledConnectionTask) {
        return new ScheduledConnectionTaskBuilderForDevice(this, partialScheduledConnectionTask);
    }

    @Override
    public InboundConnectionTaskBuilder getInboundConnectionTaskBuilder(PartialInboundConnectionTask partialInboundConnectionTask) {
        return new InboundConnectionTaskBuilderForDevice(this, partialInboundConnectionTask);
    }

    @Override
    public ConnectionInitiationTaskBuilder getConnectionInitiationTaskBuilder(PartialConnectionInitiationTask partialConnectionInitiationTask) {
        return new ConnectionInitiationTaskBuilderForDevice(this, partialConnectionInitiationTask);
    }

    @Override
    public List<ConnectionTask<?, ?>> getConnectionTasks() {
        return new ArrayList<ConnectionTask<?,?>>(connectionTasks);
    }

    @Override
    public void removeConnectionTask(ConnectionTask<?, ?> connectionTask) {
        Iterator<ConnectionTaskImpl<?,?>> connectionTaskIterator = this.connectionTasks.iterator();
        boolean removedNone = true;
        while(connectionTaskIterator.hasNext() && removedNone){
            ConnectionTaskImpl<?,?> connectionTaskToRemove = connectionTaskIterator.next();
            if(connectionTaskToRemove.getId() == connectionTask.getId()){
                connectionTask.makeObsolete();
                connectionTaskIterator.remove();
                removedNone = false;
            }
        }
        if(removedNone){
            throw new CannotDeleteConnectionTaskWhichIsNotFromThisDevice(this.thesaurus, connectionTask, this);

        }
    }

    @Override
    public List<ComTaskExecution> getComTaskExecutions() {
        return new ArrayList<ComTaskExecution>(this.comTaskExecutions);
    }

    @Override
    public ComTaskExecution.ComTaskExecutionBuilder getComTaskExecutionBuilder(ComTaskEnablement comTaskEnablement) {
        return new AbstractComTaskExecutionBuilderForDevice(comTaskExecutionProvider, this, comTaskEnablement);
    }

    @Override
    public ComTaskExecution.ComTaskExecutionUpdater getComTaskExecutionUpdater(ComTaskExecution comTaskExecution) {
        return new ComTaskExecutionUpdaterForDevice((ComTaskExecutionImpl) comTaskExecution);
    }

    @Override
    public void removeComTaskExecution(ComTaskExecution comTaskExecution) {
        Iterator<ComTaskExecutionImpl> comTaskExecutionIterator = this.comTaskExecutions.iterator();
        boolean removedNone = true;
        while (comTaskExecutionIterator.hasNext() && removedNone){
            ComTaskExecution comTaskExecutionToRemove = comTaskExecutionIterator.next();
            if(comTaskExecutionToRemove.getId() == comTaskExecution.getId()){
                comTaskExecution.makeObsolete();
                comTaskExecutionIterator.remove();
                removedNone = false;
            }
        }
        if(removedNone){
            throw new CannotDeleteComTaskExecutionWhichIsNotFromThisDevice(thesaurus, comTaskExecution, this);
        }
    }

    private class ComTaskExecutionUpdaterForDevice extends ComTaskExecutionImpl.ComTaskExecutionUpdater {

        private ComTaskExecutionUpdaterForDevice(ComTaskExecutionImpl comTaskExecution) {
            super(comTaskExecution);
        }

        @Override
        public ComTaskExecution update() {
            //TODO validate unique ComTaskEnablement?
            return super.update();
        }
    }

    private class AbstractComTaskExecutionBuilderForDevice extends ComTaskExecutionImpl.AbstractComTaskExecutionBuilder {

        private AbstractComTaskExecutionBuilderForDevice(Provider<ComTaskExecutionImpl> comTaskExecutionProvider, Device device, ComTaskEnablement comTaskEnablement) {
            super(comTaskExecutionProvider, device, comTaskEnablement);
        }

        @Override
        public ComTaskExecution add() {
            ComTaskExecution comTaskExecution = super.add();
            DeviceImpl.this.comTaskExecutions.add((ComTaskExecutionImpl) comTaskExecution);
            return comTaskExecution;
        }
    }

    @Override
    public int countNumberOfEndDeviceEvents(List<EndDeviceEventType> eventTypes, Interval interval) {
        int eventCounter = 0;
        Optional<AmrSystem> amrSystem = this.getMdcAmrSystem();
        if (amrSystem.isPresent()) {
            for (BaseDevice<Channel, LoadProfile, Register> slaveDevice : this.getPhysicalConnectedDevices()) {
                Optional<Meter> slaveMeter = amrSystem.get().findMeter(String.valueOf(slaveDevice.getId()));
                if (slaveMeter.isPresent()) {
                    eventCounter = eventCounter + this.countUniqueEndDeviceEvents(slaveMeter.get(), eventTypes, interval);
                }
            }
        }
        return eventCounter;
    }

    private int countUniqueEndDeviceEvents(Meter slaveMeter, List<EndDeviceEventType> eventTypes, Interval interval) {
        Set<String> deviceEventTypes = new HashSet<>();
        for (EndDeviceEventRecord endDeviceEvent : slaveMeter.getDeviceEvents(interval, eventTypes)) {
            deviceEventTypes.add(endDeviceEvent.getMRID());
        }
        return deviceEventTypes.size();
    }

    private class ConnectionInitiationTaskBuilderForDevice implements ConnectionInitiationTaskBuilder {

        private final ConnectionInitiationTaskImpl connectionInitiationTask;

        private ConnectionInitiationTaskBuilderForDevice(Device device, PartialConnectionInitiationTask partialConnectionInitiationTask) {
            this.connectionInitiationTask = connectionInitiationTaskProvider.get();
            this.connectionInitiationTask.initialize(device, partialConnectionInitiationTask, partialConnectionInitiationTask.getComPortPool());
        }

        @Override
        public ConnectionInitiationTaskBuilder setComPortPool(OutboundComPortPool comPortPool) {
            this.connectionInitiationTask.setComPortPool(comPortPool);
            return this;
        }

        @Override
        public ConnectionInitiationTaskBuilder setProperty(String propertyName, Object value) {
            this.connectionInitiationTask.setProperty(propertyName, value);
            return this;
        }

        @Override
        public ConnectionInitiationTask add() {
            DeviceImpl.this.connectionTasks.add(this.connectionInitiationTask);
            return this.connectionInitiationTask;
        }
    }

    private class InboundConnectionTaskBuilderForDevice implements InboundConnectionTaskBuilder {

        private final InboundConnectionTaskImpl inboundConnectionTask;

        private InboundConnectionTaskBuilderForDevice(Device device, PartialInboundConnectionTask partialInboundConnectionTask) {
            this.inboundConnectionTask = inboundConnectionTaskProvider.get();
            this.inboundConnectionTask.initialize(device, partialInboundConnectionTask, partialInboundConnectionTask.getComPortPool());
        }

        @Override
        public InboundConnectionTaskBuilder setComPortPool(InboundComPortPool comPortPool) {
            this.inboundConnectionTask.setComPortPool(comPortPool);
            return this;
        }

        @Override
        public InboundConnectionTaskBuilder setProperty(String propertyName, Object value) {
            this.inboundConnectionTask.setProperty(propertyName, value);
            return this;
        }

        @Override
        public InboundConnectionTask add() {
            DeviceImpl.this.connectionTasks.add(this.inboundConnectionTask);
            return this.inboundConnectionTask;
        }
    }

    private class ScheduledConnectionTaskBuilderForDevice implements ScheduledConnectionTaskBuilder {

        private final ScheduledConnectionTaskImpl scheduledConnectionTask;

        private ScheduledConnectionTaskBuilderForDevice(Device device, PartialOutboundConnectionTask partialOutboundConnectionTask) {
            this.scheduledConnectionTask = scheduledConnectionTaskProvider.get();
            this.scheduledConnectionTask.initialize(device, (PartialScheduledConnectionTask) partialOutboundConnectionTask, partialOutboundConnectionTask.getComPortPool());
            this.scheduledConnectionTask.setNextExecutionSpecsFrom(partialOutboundConnectionTask.getNextExecutionSpecs().getTemporalExpression());
            this.scheduledConnectionTask.setConnectionStrategy(((PartialScheduledConnectionTask) partialOutboundConnectionTask).getConnectionStrategy());
        }

        @Override
        public ScheduledConnectionTaskBuilder setCommunicationWindow(ComWindow communicationWindow) {
            this.scheduledConnectionTask.setCommunicationWindow(communicationWindow);
            return this;
        }

        @Override
        public ScheduledConnectionTaskBuilder setComPortPool(OutboundComPortPool comPortPool) {
            this.scheduledConnectionTask.setComPortPool(comPortPool);
            return this;
        }

        @Override
        public ScheduledConnectionTaskBuilder setConnectionStrategy(ConnectionStrategy connectionStrategy) {
            this.scheduledConnectionTask.setConnectionStrategy(connectionStrategy);
            return this;
        }

        @Override
        public ScheduledConnectionTaskBuilder setInitiatorTask(ConnectionInitiationTask connectionInitiationTask) {
            this.scheduledConnectionTask.setInitiatorTask(connectionInitiationTask);
            return this;
        }

        @Override
        public ScheduledConnectionTaskBuilder setNextExecutionSpecsFrom(TemporalExpression temporalExpression) {
            this.scheduledConnectionTask.setNextExecutionSpecsFrom(temporalExpression);
            return this;
        }

        @Override
        public ScheduledConnectionTaskBuilder setProperty(String propertyName, Object value) {
            this.scheduledConnectionTask.setProperty(propertyName, value);
            return this;
        }

        @Override
        public ScheduledConnectionTaskBuilder setSimultaneousConnectionsAllowed(boolean allowSimultaneousConnections) {
            this.scheduledConnectionTask.setSimultaneousConnectionsAllowed(allowSimultaneousConnections);
            return this;
        }

        @Override
        public ScheduledConnectionTask add() {
            DeviceImpl.this.connectionTasks.add(this.scheduledConnectionTask);
            return scheduledConnectionTask;
        }
    }
}
