package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exceptions.CannotDeleteUsedDefaultConnectionTaskException;
import com.energyict.mdc.device.data.exceptions.ConnectionTaskIsExecutingAndCannotBecomeObsoleteException;
import com.energyict.mdc.device.data.exceptions.DuplicateConnectionTaskException;
import com.energyict.mdc.device.data.exceptions.IncompatiblePartialConnectionTaskException;
import com.energyict.mdc.device.data.exceptions.PartialConnectionTaskNotPartOfDeviceConfigurationException;
import com.energyict.mdc.device.data.impl.ConnectionTaskSuccessIndicatorTranslationKeys;
import com.energyict.mdc.device.data.impl.CreateEventType;
import com.energyict.mdc.device.data.impl.EventType;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.impl.PropertyCache;
import com.energyict.mdc.device.data.impl.PropertyFactory;
import com.energyict.mdc.device.data.impl.ServerComTaskExecution;
import com.energyict.mdc.device.data.impl.UpdateEventType;
import com.energyict.mdc.device.data.impl.ValidPluggableClassId;
import com.energyict.mdc.device.data.impl.configchange.ServerConnectionTaskForConfigChange;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFields;
import com.energyict.mdc.device.data.tasks.ConnectionTaskProperty;
import com.energyict.mdc.device.data.tasks.ConnectionTaskPropertyProvider;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.TaskExecutionSummary;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

import javax.validation.ConstraintViolationException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.Checks.is;

/**
 * Provides an implementation for the {@link ConnectionTask} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-16 (09:08)
 */
@XmlRootElement
@HasValidProperties(groups = {Save.Create.class, Save.Update.class})
@ComPortPoolIsCompatibleWithConnectionType(groups = {Save.Create.class, Save.Update.class})
public abstract class ConnectionTaskImpl<PCTT extends PartialConnectionTask, CPPT extends ComPortPool>
    implements
        ServerConnectionTask<CPPT, PCTT>,
        ServerConnectionTaskForConfigChange<CPPT, PCTT>,
        ConnectionTaskPropertyProvider,
        PropertyFactory<ConnectionType, ConnectionTaskProperty>,
        HasLastComSession,
        PersistenceAware {

    public static final String INITIATOR_DISCRIMINATOR = "0";
    public static final String INBOUND_DISCRIMINATOR = "1";
    public static final String SCHEDULED_DISCRIMINATOR = "2";
    public static final Map<String, Class<? extends ConnectionTask>> IMPLEMENTERS =
            ImmutableMap.<String, Class<? extends ConnectionTask>>of(
                    INITIATOR_DISCRIMINATOR, ConnectionInitiationTaskImpl.class,
                    INBOUND_DISCRIMINATOR, InboundConnectionTaskImpl.class,
                    SCHEDULED_DISCRIMINATOR, ScheduledConnectionTaskImpl.class);

    @SuppressWarnings("unused")
    private long id;
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.CONNECTION_TASK_DEVICE_REQUIRED + "}")
    private Reference<Device> device = ValueReference.absent();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.CONNECTION_TASK_PARTIAL_CONNECTION_TASK_REQUIRED + "}")
    private Reference<PCTT> partialConnectionTask = ValueReference.absent();
    private boolean isDefault = false;
    private ConnectionTaskLifecycleStatus status = ConnectionTaskLifecycleStatus.INCOMPLETE;
    private Instant obsoleteDate;
    private Instant lastCommunicationStart;
    private Instant lastSuccessfulCommunicationEnd;
    private transient PropertyCache<ConnectionType, ConnectionTaskProperty> cache;
    @ValidPluggableClassId(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.CONNECTION_TASK_PLUGGABLE_CLASS_REQUIRED + "}")
    private long pluggableClassId;
    private ConnectionTypePluggableClass pluggableClass;
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.CONNECTION_TASK_COMPORT_POOL_REQUIRED + "}")
    private Reference<CPPT> comPortPool = ValueReference.absent();
    private Reference<ComServer> comServer = ValueReference.absent();
    private Reference<ComSession> lastSession = ValueReference.absent();
    @SuppressWarnings("unused")
    private boolean lastSessionStatus; // Redundant copy from lastSession to improve query performance
    @SuppressWarnings("unused")
    private ComSession.SuccessIndicator lastSessionSuccessIndicator;   // Redundant copy from lastSession to improve query performance
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    private final Clock clock;
    private DataModel dataModel;
    private EventService eventService;
    private Thesaurus thesaurus;
    private final ServerConnectionTaskService connectionTaskService;
    private final ServerCommunicationTaskService communicationTaskService;
    private final ProtocolPluggableService protocolPluggableService;

    private boolean allowIncomplete = true;
    private boolean doNotTouchParentDevice = true;

    protected ConnectionTaskImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, ServerConnectionTaskService connectionTaskService, ServerCommunicationTaskService communicationTaskService, ProtocolPluggableService protocolPluggableService) {
        super();
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.thesaurus = thesaurus;
        this.cache = new PropertyCache<>(this);
        this.clock = clock;
        this.connectionTaskService = connectionTaskService;
        this.communicationTaskService = communicationTaskService;
        this.protocolPluggableService = protocolPluggableService;
    }

    public void initialize(Device device, PCTT partialConnectionTask, CPPT comPortPool) {
        this.setDevice(device);
        this.status = ConnectionTaskLifecycleStatus.INCOMPLETE;
        this.validatePartialConnectionTaskType(partialConnectionTask);
        this.validateConstraint(partialConnectionTask, device);
        this.validateSameConfiguration(partialConnectionTask, device);
        this.partialConnectionTask.set(partialConnectionTask);
        this.pluggableClass = partialConnectionTask.getPluggableClass();
        this.pluggableClassId = this.pluggableClass.getId();
        this.comPortPool.set(comPortPool);
        if (partialConnectionTask.isDefault() && !this.device.get().getConnectionTasks().stream().filter(ConnectionTask::isDefault).findAny().isPresent()) {
            this.isDefault = partialConnectionTask.isDefault();
        }
    }

    @Override
    public long getId() {
        return id;
    }

    public boolean hasDirtyProperties() {
        return cache.isDirty();
    }

    private DataMapper<ConnectionTask> getDataMapper() {
        return this.dataModel.mapper(ConnectionTask.class);
    }

    protected DataModel getDataModel() {
        return dataModel;
    }

    @Override
    public void postLoad() {
        this.allowIncomplete = this.status.equals(ConnectionTaskLifecycleStatus.INCOMPLETE);
    }

    boolean isAllowIncomplete() {
        return this.allowIncomplete;
    }

    private void validatePartialConnectionTaskType(PCTT partialConnectionTask) {
        Class<PCTT> partialConnectionTaskType = this.getPartialConnectionTaskType();
        if (!partialConnectionTaskType.isAssignableFrom(partialConnectionTask.getClass())) {
            throw new IncompatiblePartialConnectionTaskException(
                    partialConnectionTask,
                    partialConnectionTaskType,
                    thesaurus,
                    MessageSeeds.CONNECTION_TASK_INCOMPATIBLE_PARTIAL);
        }
    }

    private void validateConstraint(PCTT partialConnectionTask, Device device) {
        Optional<ConnectionTask> result = this.connectionTaskService.findConnectionTaskForPartialOnDevice(partialConnectionTask, device);
        if (result.isPresent()) {
            ConnectionTask connectionTaskWithSamePartialConnectionTaskDeviceCombination = result.get();
            if (this.getId() != connectionTaskWithSamePartialConnectionTaskDeviceCombination.getId()) {
                throw new DuplicateConnectionTaskException(
                        device, partialConnectionTask, connectionTaskWithSamePartialConnectionTaskDeviceCombination, thesaurus,
                        MessageSeeds.DUPLICATE_CONNECTION_TASK);
            }
        }
    }

    /**
     * Validates that the specified {@link PartialConnectionTask}
     * is part of the Device's configuration.
     *
     * @param partialConnectionTask The PartialConnectionTask
     * @param device                The Device
     */
    private void validateSameConfiguration(PCTT partialConnectionTask, Device device) {
        if (!is(this.getDeviceConfigurationId(device)).equalTo(partialConnectionTask.getConfiguration().getId())) {
            throw new PartialConnectionTaskNotPartOfDeviceConfigurationException(partialConnectionTask, device, thesaurus, MessageSeeds.CONNECTION_TASK_PARTIAL_CONNECTION_TASK_NOT_IN_CONFIGURATION);
        }
    }

    private long getDeviceConfigurationId(Device device) {
        return device.getDeviceConfiguration().getId();
    }

    public void notifyCreated() {
        this.eventService.postEvent(CreateEventType.CONNECTIONTASK.topic(), this);
    }

    public void notifyUpdated() {
        this.eventService.postEvent(UpdateEventType.CONNECTIONTASK.topic(), this);
    }

    /**
     * Notifies all ComTaskExecution (including obsoletes) which refer to this ConnectionTask
     * that their ConnectionTask is going to be deleted or made obsolete.
     */
    private void unRegisterConnectionTaskFromComTasks() {
        for (ComTaskExecution comTaskExecution : this.findDependentComTaskExecutions()) {
            ((ServerComTaskExecution)comTaskExecution).connectionTaskRemoved();
        }
    }

    public void saveAllProperties() {
        if (this.cache.isDirty()) {
            if (this.getTypedProperties().localSize() == 0) {
                Instant currentTime = clock.instant();
                CustomPropertySetValues currentValues = this.getPluggableClass().getPropertiesFor(this, currentTime);
                if (!currentValues.isEmpty() && !currentValues.getEffectiveRange().lowerEndpoint().equals(currentTime)) {
                    this.getPluggableClass().setPropertiesFor(this, CustomPropertySetValues.emptyFrom(currentTime), currentTime);
                }
            }
            else {
                this.saveAllProperties(this.getAllProperties());
            }
        }
    }

    public void removeAllProperties() {
        this.getPluggableClass().removePropertiesFor(this);
    }

    private void saveAllProperties(List<ConnectionTaskProperty> properties) {
        Instant now = this.now();
        this.getPluggableClass().setPropertiesFor(this, this.toCustomPropertySetValues(properties, now), now);
        this.clearPropertyCache();
    }

    protected Instant now() {
        return clock.instant();
    }

    @Override
    public void save () {
        if (this.id > 0) {
            this.validateAndUpdate();
            this.notifyUpdated();
        }
        else {
            this.validateAndCreate();
            this.notifyCreated();
        }
    }

    /**
     * Validates and saves this object for the first time.
     */
    private void validateAndCreate() {
        Save.CREATE.save(this.dataModel, this);
    }

    /**
     * Validates and updates the changes made to this object.
     */
    private void validateAndUpdate() {
        Save.UPDATE.save(this.dataModel, this);
    }

    @Override
    public void makeObsolete() {
        Save.UPDATE.validate(this.dataModel, this, Save.Update.class);
        this.reloadComServerAndObsoleteDate();
        this.validateMakeObsolete();
        this.obsoleteDate = this.clock.instant();
        this.dataModel.update(this);
        this.makeDependentsObsolete();
        this.unRegisterConnectionTaskFromComTasks();
        this.notifyUpdated();
    }

    public void update() {
        Save.UPDATE.save(this.dataModel, this, Save.Create.class, Save.Update.class);
        if (!doNotTouchParentDevice) {
            this.dataModel.touch(device.get());
        }
        this.notifyUpdated();
    }

    protected void update(String... fieldNames) {
        this.dataModel.update(this, fieldNames);
        this.notifyUpdated();
    }

    /**
     * We need to check if this task is currently running or someone else made it obsolete.
     * We are already in a Transaction so we don't wrap it again.
     */
    private void reloadComServerAndObsoleteDate() {
        ConnectionTask updatedVersionOfMyself = this.connectionTaskService.findConnectionTask(this.getId()).get();
        this.comServer.set(updatedVersionOfMyself.getExecutingComServer());
        this.obsoleteDate = updatedVersionOfMyself.getObsoleteDate() == null ? null : updatedVersionOfMyself.getObsoleteDate();
    }

    protected void makeDependentsObsolete() {
        this.removeAllProperties();
    }

    private void validateMakeObsolete() {
        if (this.comServer.isPresent()) {
            throw new ConnectionTaskIsExecutingAndCannotBecomeObsoleteException(this, this.getExecutingComServer(), thesaurus, MessageSeeds.CONNECTION_TASK_IS_EXECUTING_AND_CANNOT_OBSOLETE);
        }
    }

    protected abstract Class<PCTT> getPartialConnectionTaskType();

    protected void validateDelete() {
        if (this.isDefault()) {
            this.validateNoDependentComTaskExecutions();
        }
    }

    private void validateNoDependentComTaskExecutions() {
        List<ComTaskExecution> dependents = this.findDependentComTaskExecutions();
        if (!dependents.isEmpty()) {
            throw new CannotDeleteUsedDefaultConnectionTaskException(this, thesaurus, MessageSeeds.DEFAULT_CONNECTION_TASK_IS_INUSE_AND_CANNOT_DELETE);
        }
    }

    private List<ComTaskExecution> findDependentComTaskExecutions() {
        return this.communicationTaskService.findComTaskExecutionsByConnectionTask(this).find();
    }

    public void executionStarted(ComServer comServer) {
        List<String> updatedColumns = new ArrayList<>();
        this.doExecutionStarted(comServer, updatedColumns);
        this.update(updatedColumns.toArray(new String[updatedColumns.size()]));
    }

    protected void doExecutionStarted(ComServer comServer, List<String> updatedColumns) {
        this.setExecutingComServer(comServer);
        updatedColumns.add(ConnectionTaskFields.COM_SERVER.fieldName());
        this.lastCommunicationStart = this.clock.instant();
        updatedColumns.add(ConnectionTaskFields.LAST_COMMUNICATION_START.fieldName());
    }

    public void executionCompleted() {
        this.doNotTouchParentDevice();
        List<String> updatedFields = new ArrayList<>();
        this.doExecutionCompleted(updatedFields);
        this.update(updatedFields.toArray(new String[updatedFields.size()]));

    }

    protected void doExecutionCompleted(List<String> updatedFields) {
        this.setExecutingComServer(null);
        updatedFields.add(ConnectionTaskFields.COM_SERVER.fieldName());
        this.lastSuccessfulCommunicationEnd = clock.instant();
        updatedFields.add(ConnectionTaskFields.LAST_SUCCESSFUL_COMMUNICATION_END.fieldName());
    }

    public String getName() {
        return getPartialConnectionTask().getName();
    }

    @Override
    public Device getDevice() {
        return this.device.get();
    }

    private void setDevice(Device device) {
        this.device.set(device);
    }

    @Override
    public ConnectionType getType() {
        return this.getPluggableClass().getConnectionType();
    }

    public ConnectionTypePluggableClass getPluggableClass() {
        if (this.pluggableClass == null) {
            this.loadPluggableClass();
        }
        return pluggableClass;
    }

    private void loadPluggableClass() {
        this.pluggableClass = this.findConnectionTypePluggableClass(this.pluggableClassId).get();
    }

    private Optional<ConnectionTypePluggableClass> findConnectionTypePluggableClass(long connectionTypePluggableClassId) {
        return this.protocolPluggableService.findConnectionTypePluggableClass(connectionTypePluggableClassId);
    }

    private void clearPropertyCache() {
        this.cache.clear();
    }

    public List<ConnectionTaskProperty> getAllProperties() {
        return this.getAllProperties(clock.instant());
    }

    @Override
    public List<ConnectionTaskProperty> getAllProperties(Instant date) {
        return this.getAllLocalProperties(date);
    }

    private List<ConnectionTaskProperty> getAllLocalProperties(Instant date) {
        return this.cache.get(date);
    }

    @Override
    public List<ConnectionTaskProperty> loadProperties(Instant date) {
        return this.toConnectionProperties(this.getPluggableClass().getPropertiesFor(this, date));
    }

    public ConnectionTaskProperty getProperty(String propertyName) {
        for (ConnectionTaskProperty property : this.getAllProperties()) {
            if (property.getName().equals(propertyName)) {
                return property;
            }
        }
        return null;
    }

    private CustomPropertySetValues toCustomPropertySetValues(List<ConnectionTaskProperty> properties, Instant effectiveTimestamp) {
        CustomPropertySetValues values = CustomPropertySetValues.emptyFrom(effectiveTimestamp);
        properties.forEach(property -> values.setProperty(property.getName(), property.getValue()));
        return values;
    }

    protected List<ConnectionTaskProperty> toConnectionProperties(CustomPropertySetValues values) {
        return values.propertyNames()
                .stream()
                .map(propertyName -> this.newProperty(propertyName, values))
                .collect(Collectors.toList());
    }

    private ConnectionTaskProperty newProperty(String name, CustomPropertySetValues values) {
        ConnectionTaskPropertyImpl property = new ConnectionTaskPropertyImpl(this, name);
        property.setValue(values.getProperty(name));
        property.setActivePeriod(values.getEffectiveRange());
        return property;
    }

    @Override
    public ConnectionTaskProperty newProperty(String name, Object value, Instant activeDate) {
        ConnectionTaskPropertyImpl property = new ConnectionTaskPropertyImpl(this, name);
        property.setValue(value);
        property.setActivePeriod(Range.atLeast(activeDate));
        return property;
    }

    @Override
    public void setProperty(String propertyName, Object value) {
        Instant now = clock.instant();
        this.getAllProperties(now); // Make sure the cache is loaded to avoid that writing to the cache is reverted when the client will call getTypedProperties right after this call
        this.cache.put(now, propertyName, value);
    }

    @Override
    public void removeProperty(String propertyName) {
        Instant now = clock.instant();
        this.getAllProperties(now); // Make sure the cache is loaded to avoid that writing to the cache is reverted when the client will call getTypedProperties right after this call
        this.cache.remove(now, propertyName);
    }

    @Override
    public boolean allowsSimultaneousConnections() {
        return this.getPluggableClass().getConnectionType().allowsSimultaneousConnections();
    }

    @Override
    public CPPT getComPortPool() {
        return this.comPortPool.orNull();
    }

    @Override
    public void setComPortPool(CPPT comPortPool) {
        this.comPortPool.set(comPortPool);
    }

    @Override
    public boolean hasComPortPool() {
        return this.comPortPool.isPresent();
    }

    @Override
    public PCTT getPartialConnectionTask() {
        return this.partialConnectionTask.get();
    }

    @Override
    public Instant getLastCommunicationStart() {
        return this.lastCommunicationStart;
    }

    @Override
    public Instant getLastSuccessfulCommunicationEnd() {
        return this.lastSuccessfulCommunicationEnd;
    }

    @Override
    public Optional<ComSession> getLastComSession() {
        return this.lastSession.getOptional();
    }

    @Override
    public void sessionCreated(ComSession session) {
        if (this.lastSession.isPresent()) {
            if (session.endsAfter(this.lastSession.get())) {
                this.setLastSessionAndUpdate(session);
            }
        } else {
            this.setLastSessionAndUpdate(session);
        }
    }

    private void setLastSessionAndUpdate(ComSession session) {
        this.setLastSession(session);
        this.update(ConnectionTaskFields.LAST_SESSION.fieldName(),
                    ConnectionTaskFields.LAST_SESSION_SUCCESS_INDICATOR.fieldName(),
                    ConnectionTaskFields.LAST_SESSION_STATUS.fieldName(),
                    ConnectionTaskFields.LAST_COMMUNICATION_START.fieldName());
    }

    private void setLastSession(ComSession session) {
        this.lastSession.set(session);
        this.lastSessionSuccessIndicator = session.getSuccessIndicator();
        this.lastSessionStatus = session.wasSuccessful();
        this.lastCommunicationStart = session.getStartDate();
    }

    @Override
    public SuccessIndicator getSuccessIndicator() {
        Optional<ComSession> lastComSession = this.getLastComSession();
        if (lastComSession.isPresent()) {
            if (lastComSession.get().wasSuccessful()) {
                return ConnectionTask.SuccessIndicator.SUCCESS;
            } else {
                return ConnectionTask.SuccessIndicator.FAILURE;
            }
        } else {
            return ConnectionTask.SuccessIndicator.NOT_APPLICABLE;
        }
    }

    @Override
    public String getSuccessIndicatorDisplayName() {
        return ConnectionTaskSuccessIndicatorTranslationKeys.translationFor(getSuccessIndicator(), thesaurus);
    }

    @Override
    public Optional<ComSession.SuccessIndicator> getLastSuccessIndicator() {
        return this.getLastComSession().map(ComSession::getSuccessIndicator);
    }

    @Override
    public Optional<TaskExecutionSummary> getLastTaskExecutionSummary() {
        Optional<ComSession> lastComSession = this.getLastComSession();
        return lastComSession.map(TaskExecutionSummary.class::cast);
    }

    @Override
    @XmlAttribute
    public Instant getObsoleteDate() {
        return this.obsoleteDate;
    }

    @Override
    public boolean isDefault() {
        return this.isDefault;
    }

    // To be used by the DeviceServiceImpl only that now has the responsibility to switch defaults
    public void setAsDefault() {
        this.doSetAsDefault();
        this.update();
    }

    protected void doSetAsDefault() {
        this.isDefault = true;
        this.postEvent(EventType.CONNECTIONTASK_SETASDEFAULT);
    }

    private void postEvent(EventType eventType) {
        this.eventService.postEvent(eventType.topic(), this);
    }

    // Only to be used by the ConnectionTaskServiceImpl that now has the responsibility to switch defaults.
    void clearDefault() {
        this.isDefault = false;
        this.update();
    }

    @Override
    public boolean isObsolete() {
        return this.obsoleteDate != null;
    }

    @Override
    public String toString() {
        return "ConnectionTask (" + this.getId() + ")";
    }

    protected ConnectionTaskProperty getPropertyByName(String propertyName) {
        for (ConnectionTaskProperty property : this.getProperties()) {
            if (propertyName.equals(property.getName())) {
                return property;
            }
        }
        return null;
    }

    @Override
    public List<ConnectionTaskProperty> getProperties() {
        return this.getProperties(this.now());
    }

    @Override
    public List<ConnectionTaskProperty> getProperties(Instant date) {
        TypedProperties partialProperties = this.getPartialConnectionTask().getTypedProperties();
        List<ConnectionTaskProperty> allProperties =
                partialProperties
                        .propertyNames()
                        .stream()
                        .map(propertyName -> toConnectionTaskProperty(partialProperties, propertyName))
                        .collect(Collectors.toList());
        return this.merge(allProperties, this.getAllProperties(date));
    }

    private ConnectionTaskPropertyImpl toConnectionTaskProperty(TypedProperties partialProperties, String propertyName) {
        return new ConnectionTaskPropertyImpl(
                this, propertyName,
                partialProperties.getProperty(propertyName),
                Range.all(),
                this.getPartialConnectionTask().getPluggableClass());
    }

    private List<ConnectionTaskProperty> merge(List<ConnectionTaskProperty> inheritedProperties, List<ConnectionTaskProperty> localProperties) {
        List<ConnectionTaskProperty> merged = new ArrayList<>(localProperties); // Anything that is locally defined overrules what is inherited
        Map<String, ConnectionTaskProperty> localPropertiesByName = new HashMap<>();
        for (ConnectionTaskProperty property : localProperties) {
            localPropertiesByName.put(property.getName(), property);
        }
        for (ConnectionTaskProperty inheritedProperty : inheritedProperties) {
            ConnectionTaskProperty localProperty = localPropertiesByName.get(inheritedProperty.getName());
            if (localProperty == null) {
                merged.add(inheritedProperty);
            }
        }
        return merged;
    }

    protected List<ConnectionProperty> castToConnectionProperties(List<ConnectionTaskProperty> properties) {
        return new ArrayList<>(properties);
    }

    @Override
    public TypedProperties getTypedProperties() {
        TypedProperties inheritedProperties = this.getPartialConnectionTask().getTypedProperties();
        TypedProperties typedProperties = TypedProperties.inheritingFrom(inheritedProperties);
        for (ConnectionTaskProperty property : this.getAllProperties(this.now())) {
            if (property.isInherited()) {
                if (!inheritedProperties.hasValueFor(property.getName())) {
                    inheritedProperties.setProperty(property.getName(), property.getValue());
                }
            } else {
                typedProperties.setProperty(property.getName(), property.getValue());
            }
        }
        return typedProperties;
    }

    @Override
    @XmlAttribute
    public ConnectionTaskLifecycleStatus getStatus() {
        return this.status;
    }

    void setStatus(ConnectionTaskLifecycleStatus status) {
        this.status = status;
    }

    @Override
    public void revalidatePropertiesAndAdjustStatus() {
        if (this.getId() > 0 && this.isActive()) {
            try {
                Save.UPDATE.save(this.dataModel, this);
            }
            catch (ConstraintViolationException e) {
                /* Assumption: no changes on this ConnectionTask
                 * therefore: exception relates to missing required properties
                 * so set the status to Incomplete and apply change. */
                this.setStatus(ConnectionTaskLifecycleStatus.INCOMPLETE);
                this.dataModel.update(this, ConnectionTaskFields.STATUS.fieldName());
            }
        }
    }

    @Override
    public void deactivate() {
        this.status = ConnectionTaskLifecycleStatus.INACTIVE;
        this.update();
    }

    @Override
    public void activate() {
        this.status = ConnectionTaskLifecycleStatus.ACTIVE;
        this.update();
    }

    @Override
    public boolean isActive() {
        return this.status.equals(ConnectionTaskLifecycleStatus.ACTIVE);
    }



    @Override
    public boolean isExecuting() {
        return this.comServer.isPresent();
    }

    @Override
    public ComServer getExecutingComServer() {
        return comServer.orNull();
    }

    void setExecutingComServer(ComServer comServer) {
        this.comServer.set(comServer);
    }

    public void updateExecutingComServer(ComServer comServer) {
        this.comServer.set(comServer);
        this.update(ConnectionTaskFields.COM_SERVER.fieldName());
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getSimpleName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }

    @Override
    @XmlElement
    public ConnectionType getConnectionType() {
        return this.getPluggableClass().getConnectionType();
    }

    protected TimeZone getClocksTimeZone() {
        return TimeZone.getTimeZone(this.clock.getZone());
    }

    @Override
    public void setNewPartialConnectionTask(PCTT partialConnectionTask) {
        this.partialConnectionTask.set(partialConnectionTask);
        this.pluggableClass = partialConnectionTask.getPluggableClass();
        this.pluggableClassId = this.pluggableClass.getId();
        getDataModel().update(this, "partialConnectionTask", "pluggableClassId");
    }

    /**
     * This will use the validation framework to detect if there are any Validation errors.
     *
     * @return true if everything is valid, false otherwise
     */
    boolean isValidConnectionTask() {
        try {
            Save.CREATE.validate(this.dataModel, this, Save.Update.class);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public long getVersion() {
        return this.version;
    }

    /**
     * Sometimes it is necessary to avoid touching the parent device during an update process,
     * when touching would introduce an optimistic lock exception (for example when topology handler updates all com task executions).
     */
    void doNotTouchParentDevice() {
        this.doNotTouchParentDevice = true;
    }

    @Override
    public void notifyDelete() {
        this.removeAllProperties();
    }

    /**
     * Validates that {@link ConnectionTaskProperty connection task properties}
     * that are provided at connection time, were originally returned by
     * the connection task that is connecting.
     */
    protected interface ConnectionTaskPropertyValidator {

        void validate(List<ConnectionTaskProperty> properties);

    }

    /**
     * Provides an implementation for the {@link ConnectionTaskPropertyValidator} interface
     * that is created by the connection task, knowing for sure that the {@link ConnectionTaskProperty connection task properties}
     * originate from itself.
     */
    protected class TrustingConnectionTaskPropertyValidator implements ConnectionTaskPropertyValidator {
        @Override
        public void validate(List<ConnectionTaskProperty> properties) {
            // This class trusts that the properties come from the connection task
        }
    }

    /**
     * Provides an implementation for the {@link ConnectionTaskPropertyValidator} interface
     * for {@link ConnectionTaskProperty connection task properties} that were provided
     * by an external party and can therefore NOT be trusted.
     */
    protected class MistrustingConnectionTaskPropertyValidator implements ConnectionTaskPropertyValidator {

        @Override
        public void validate(List<ConnectionTaskProperty> properties) {
            for (ConnectionTaskProperty property : properties) {
                this.validate(property);
            }
        }

        private void validate(ConnectionTaskProperty property) {
            try {
                this.validate((ConnectionTaskPropertyImpl) property);
            } catch (ClassCastException e) {
                // property is not of the correct class, cannot have been created by this connection task
                throw illegalArgumentException(property);
            }
        }

        private void validate(ConnectionTaskPropertyImpl property) {
            if (!property.relatesTo(ConnectionTaskImpl.this)) {
                throw illegalArgumentException(property);
            }
        }

        private IllegalArgumentException illegalArgumentException(ConnectionTaskProperty property) {
            return new IllegalArgumentException("Connection task property " + property.getName() + " was not created by this connection task");
        }

    }
}