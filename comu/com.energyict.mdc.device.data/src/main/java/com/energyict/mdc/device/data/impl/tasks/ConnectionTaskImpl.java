package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.ConnectionTaskFields;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.exceptions.CannotDeleteUsedDefaultConnectionTaskException;
import com.energyict.mdc.device.data.exceptions.CannotUpdateObsoleteConnectionTaskException;
import com.energyict.mdc.device.data.exceptions.ConnectionTaskIsAlreadyObsoleteException;
import com.energyict.mdc.device.data.exceptions.ConnectionTaskIsExecutingAndCannotBecomeObsoleteException;
import com.energyict.mdc.device.data.exceptions.DuplicateConnectionTaskException;
import com.energyict.mdc.device.data.exceptions.IncompatiblePartialConnectionTaskException;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.exceptions.NestedRelationTransactionException;
import com.energyict.mdc.device.data.exceptions.PartialConnectionTaskNotPartOfDeviceConfigurationException;
import com.energyict.mdc.device.data.exceptions.RelationIsAlreadyObsoleteException;
import com.energyict.mdc.device.data.impl.CreateEventType;
import com.energyict.mdc.device.data.impl.DeleteEventType;
import com.energyict.mdc.device.data.impl.EventType;
import com.energyict.mdc.device.data.impl.PersistentIdObject;
import com.energyict.mdc.device.data.impl.PropertyCache;
import com.energyict.mdc.device.data.impl.PropertyFactory;
import com.energyict.mdc.device.data.impl.RelationTransactionExecutor;
import com.energyict.mdc.device.data.impl.ServerComTaskExecution;
import com.energyict.mdc.device.data.impl.SimpleRelationTransactionExecutor;
import com.energyict.mdc.device.data.impl.UpdateEventType;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskProperty;
import com.energyict.mdc.device.data.tasks.ConnectionTaskPropertyProvider;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.TaskExecutionSummary;
import com.energyict.mdc.dynamic.relation.CanLock;
import com.energyict.mdc.dynamic.relation.DefaultRelationParticipant;
import com.energyict.mdc.dynamic.relation.Relation;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.pluggable.PluggableClassWithRelationSupport;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.util.time.Interval;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import static com.elster.jupiter.util.Checks.is;
import static com.energyict.mdc.protocol.pluggable.ConnectionTypePropertyRelationAttributeTypeNames.CONNECTION_TASK_ATTRIBUTE_NAME;

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
    extends PersistentIdObject<ConnectionTask>
    implements
        ServerConnectionTask<CPPT, PCTT>,
        ConnectionTaskPropertyProvider,
        CanLock,
        DefaultRelationParticipant,
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
    private long pluggableClassId;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.CONNECTION_TASK_PLUGGABLE_CLASS_REQUIRED + "}")
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
    private final ServerConnectionTaskService connectionTaskService;
    private final ServerCommunicationTaskService communicationTaskService;
    private final DeviceService deviceService;
    private final ProtocolPluggableService protocolPluggableService;
    private final RelationService relationService;

    private boolean allowIncomplete = true;

    protected ConnectionTaskImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, ServerConnectionTaskService connectionTaskService, ServerCommunicationTaskService communicationTaskService, DeviceService deviceService, ProtocolPluggableService protocolPluggableService, RelationService relationService) {
        super(ConnectionTask.class, dataModel, eventService, thesaurus);
        this.cache = new PropertyCache<>(this);
        this.clock = clock;
        this.connectionTaskService = connectionTaskService;
        this.communicationTaskService = communicationTaskService;
        this.deviceService = deviceService;
        this.protocolPluggableService = protocolPluggableService;
        this.relationService = relationService;
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
        if (partialConnectionTask.isDefault() && !this.device.get().getConnectionTasks().stream().filter(connectionTask -> connectionTask.isDefault()).findAny().isPresent()) {
            this.isDefault = partialConnectionTask.isDefault();
        }
    }


    @Override
    public void lock() {
        this.getDataMapper().lock(this.getId());
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
            throw new IncompatiblePartialConnectionTaskException(this.getThesaurus(), partialConnectionTask, partialConnectionTaskType);
        }
    }

    private void validateConstraint(PCTT partialConnectionTask, Device device) {
        Optional<ConnectionTask> result = this.connectionTaskService.findConnectionTaskForPartialOnDevice(partialConnectionTask, device);
        if (result.isPresent()) {
            ConnectionTask connectionTaskWithSamePartialConnectionTaskDeviceCombination = result.get();
            if (this.getId() != connectionTaskWithSamePartialConnectionTaskDeviceCombination.getId()) {
                throw new DuplicateConnectionTaskException(
                        this.getThesaurus(),
                        device,
                        partialConnectionTask,
                        connectionTaskWithSamePartialConnectionTaskDeviceCombination);
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
            throw new PartialConnectionTaskNotPartOfDeviceConfigurationException(this.getThesaurus(), partialConnectionTask, device);
        }
    }

    private long getDeviceConfigurationId(Device device) {
        return device.getDeviceConfiguration().getId();
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.CONNECTIONTASK;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.CONNECTIONTASK;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.CONNECTIONTASK;
    }

    @Override
    protected void doDelete() {
        this.deleteAllProperties();
        this.deleteDependents();
        this.getDataMapper().remove(this);
    }

    /**
     * Deletes the {@link Relation}s that hold the values of
     * all the {@link com.energyict.mdc.pluggable.PluggableClass properties}.
     */
    private void deleteAllProperties() {
        this.obsoleteAllProperties();
    }

    protected void deleteDependents() {
        this.unRegisterConnectionTaskFromComTasks();
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

    @Override
    public void save() {
        this.validateNotObsolete();
        super.save();
        this.saveAllProperties();
    }

    protected void saveAllProperties() {
        if (this.cache.isDirty()) {
            if (this.getTypedProperties().localSize() == 0) {
                this.removeAllProperties();
            } else {
                this.saveAllProperties(
                        this.getAllProperties(),
                        new SimpleRelationTransactionExecutor<>(
                                this,
                                clock.instant(),
                                this.findRelationType(),
                                this.getThesaurus()));
            }
        }
    }

    protected void removeAllProperties() {
        Relation relation = getDefaultRelation();
        if (relation != null) {
            try {
                relation.makeObsolete();
            } catch (BusinessException e) {
                throw new NestedRelationTransactionException(this.getThesaurus(), e, this.findRelationType().getName());
            }
            // Cannot collapse catch blocks because of the constructor
            catch (SQLException e) {
                throw new NestedRelationTransactionException(this.getThesaurus(), e, this.findRelationType().getName());
            }
        }
    }

    private void saveAllProperties(List<ConnectionTaskProperty> properties, RelationTransactionExecutor<ConnectionType> transactionExecutor) {
        properties.forEach(transactionExecutor::add);
        transactionExecutor.execute();
        this.clearPropertyCache();
    }

    protected Instant now() {
        return clock.instant();
    }

    protected void validateNotObsolete() {
        if (this.obsoleteDate != null) {
            throw new CannotUpdateObsoleteConnectionTaskException(this.getThesaurus(), this);
        }
    }

    @Override
    public void makeObsolete() {
        this.reloadComServerAndObsoleteDate();
        this.validateMakeObsolete();
        this.obsoleteDate = this.clock.instant();
        this.makeDependentsObsolete();
        this.unRegisterConnectionTaskFromComTasks();
        this.validateAndUpdate();
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
        this.obsoleteAllProperties();
    }

    /**
     * Makes the {@link Relation}s that hold the values of
     * all the {@link ConnectionTaskProperty ConnectionTaskProperties} obsolete.
     */
    protected void obsoleteAllProperties() {
        List<Relation> relations = this.getPluggableClass().getRelations(this, Range.all());
        for (Relation relation : relations) {
            try {
                relation.makeObsolete();
            } catch (BusinessException | SQLException e) {
                throw new RelationIsAlreadyObsoleteException(this.getThesaurus(), relation.getRelationType().getName());
            }
        }
    }

    private void validateMakeObsolete() {
        if (this.isObsolete()) {
            throw new ConnectionTaskIsAlreadyObsoleteException(this.getThesaurus(), this);
        } else if (this.comServer.isPresent()) {
            throw new ConnectionTaskIsExecutingAndCannotBecomeObsoleteException(this.getThesaurus(), this, this.getExecutingComServer());
        }
    }

    protected abstract Class<PCTT> getPartialConnectionTaskType();

    @Override
    protected void validateDelete() {
        if (this.isDefault()) {
            this.validateNoDependentComTaskExecutions();
        }
    }

    private void validateNoDependentComTaskExecutions() {
        List<ComTaskExecution> dependents = this.findDependentComTaskExecutions();
        if (!dependents.isEmpty()) {
            throw new CannotDeleteUsedDefaultConnectionTaskException(this.getThesaurus(), this);
        }
    }

    private List<ComTaskExecution> findDependentComTaskExecutions() {
        return this.communicationTaskService.findComTaskExecutionsByConnectionTask(this).find();
    }

    public void executionStarted(ComServer comServer) {
        List<String> updatedColumns = new ArrayList<>();
        this.doExecutionStarted(comServer, updatedColumns);
        this.update(updatedColumns);
    }

    protected void doExecutionStarted(ComServer comServer, List<String> updatedColumns) {
        this.setExecutingComServer(comServer);
        updatedColumns.add(ConnectionTaskFields.COM_SERVER.fieldName());
        this.lastCommunicationStart = this.clock.instant();
        updatedColumns.add(ConnectionTaskFields.LAST_COMMUNICATION_START.fieldName());
    }

    public void executionCompleted() {
        this.doExecutionCompleted();
        this.update();
    }

    protected void doExecutionCompleted() {
        this.setExecutingComServer(null);
        this.lastSuccessfulCommunicationEnd = clock.instant();
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

    public ConnectionTypePluggableClass getPluggableClass() {
        if (this.pluggableClass == null) {
            this.loadPluggableClass();
        }
        return pluggableClass;
    }

    private void loadPluggableClass() {
        this.pluggableClass = this.findConnectionTypePluggableClass(this.pluggableClassId).get();
    }

    private RelationType findRelationType() {
        return this.getPluggableClass().findRelationType();
    }

    private Optional<ConnectionTypePluggableClass> findConnectionTypePluggableClass(long connectionTypePluggableClassId) {
        return this.protocolPluggableService.findConnectionTypePluggableClass(connectionTypePluggableClassId);
    }

    @Override
    public Relation getDefaultRelation() {
        return this.getDefaultRelation(clock.instant());
    }

    @Override
    public Relation getDefaultRelation(Instant date) {
        return this.getPluggableClass().getRelation(this, date);
    }

    @Override
    public RelationAttributeType getDefaultAttributeType() {
        return this.getPluggableClass().getDefaultAttributeType();
    }

    @Override
    public RelationType getDefaultRelationType() {
        return this.getPluggableClass().findRelationType();
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
        Relation defaultRelation = this.getDefaultRelation(date);
        /* defaultRelation is null when the pluggable class has no properties.
         * In that case, no relation type was created. */
        if (defaultRelation != null) {
            return this.toProperties(defaultRelation);
        } else {
            return new ArrayList<>(0);
        }
    }

    @Override
    public List<ConnectionTaskProperty> loadProperties(Range<Instant> interval) {
        List<ConnectionTaskProperty> properties = new ArrayList<>();
        RelationAttributeType defaultAttributeType = this.getDefaultAttributeType();
        /* defaultAttributeType is null when the pluggable class has no properties.
         * In that case, no relation type was created. */
        if (defaultAttributeType != null) {
            List<Relation> relations = this.getRelations(defaultAttributeType, interval, false);
            for (Relation relation : relations) {
                properties.addAll(this.toProperties(relation));
            }
        }
        return properties;
    }

    public ConnectionTaskProperty getProperty(String propertyName) {
        for (ConnectionTaskProperty property : this.getAllProperties()) {
            if (property.getName().equals(propertyName)) {
                return property;
            }
        }
        return null;
    }

    protected List<ConnectionTaskProperty> toProperties(Relation relation) {
        List<ConnectionTaskProperty> properties = new ArrayList<>();
        for (RelationAttributeType attributeType : relation.getRelationType().getAttributeTypes()) {
            if (!isDefaultAttribute(attributeType) && this.attributeHasValue(relation, attributeType)) {
                properties.add(this.newPropertyFor(relation, attributeType));
            }
        }
        return properties;
    }

    private boolean attributeHasValue(Relation relation, RelationAttributeType attributeType) {
        return relation.get(attributeType) != null;
    }

    private boolean isDefaultAttribute(RelationAttributeType attributeType) {
        return this.getDefaultAttributeName().equals(attributeType.getName());
    }

    private String getDefaultAttributeName() {
        return CONNECTION_TASK_ATTRIBUTE_NAME;
    }

    private ConnectionTaskProperty newPropertyFor(Relation relation, RelationAttributeType attributeType) {
        return new ConnectionTaskPropertyImpl(this, relation, attributeType.getName(), this.getPluggableClass());
    }

    @Override
    public ConnectionTaskProperty newProperty(String name, Object value, Instant activeDate) {
        ConnectionTaskPropertyImpl property = new ConnectionTaskPropertyImpl(this, name);
        property.setValue(value);
        property.setActivePeriod(Interval.startAt(activeDate));
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
    public Object get(String propertyName, Instant date) {
        PluggableClassWithRelationSupport pluggableClass = this.getPluggableClass();
        if (pluggableClass.findRelationType().hasAttribute(propertyName)) {
            // Should in fact be at most one since this is the default relation
            Relation relation = this.getDefaultRelation(date);
            if (relation == null) {
                // No relation active on the specified Date, therefore no value
                return null;
            } else {
                return relation.get(propertyName);
            }
        }
        // Either no properties configured on the PluggableClass or not one of my properties
        return null;
    }

    @Override
    public Object get(String attributeName) {
        return this.get(attributeName, clock.instant());
    }

    @Override
    public Object get(RelationAttributeType attributeType, Instant date) {
        return this.get(attributeType.getName(), date);
    }

    @Override
    public Object get(RelationAttributeType attributeType) {
        return this.get(attributeType, clock.instant());
    }

    @Override
    public List<Relation> getRelations(RelationAttributeType attrib, Instant when, boolean includeObsolete) {
        return attrib.getRelations(this, when, includeObsolete, 0, 0);
    }

    @Override
    public List<Relation> getRelations(RelationAttributeType attrib, Instant date, boolean includeObsolete, int fromRow, int toRow) {
        return attrib.getRelations(this, date, includeObsolete, fromRow, toRow);
    }

    @Override
    public List<Relation> getAllRelations(RelationAttributeType attrib) {
        return attrib.getAllRelations(this);
    }

    @Override
    public List<Relation> getRelations(RelationAttributeType defaultAttribute, Range<Instant> period, boolean includeObsolete) {
        return defaultAttribute.getRelations(this, period, includeObsolete);
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
/*      Bug in the DataModel that does not support foreign key columns in the update method
        this.getDataModel()
                .update(this,
                        ConnectionTaskFields.LAST_SESSION.fieldName(),
                        ConnectionTaskFields.LAST_SESSION_SUCCESS_INDICATOR.fieldName(),
                        ConnectionTaskFields.LAST_SESSION_STATUS.fieldName());
*/
        this.update();
    }

    private void setLastSession(ComSession session) {
        this.lastSession.set(session);
        this.lastSessionSuccessIndicator = session.getSuccessIndicator();
        this.lastSessionStatus = session.wasSuccessful();
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

    // To be used by the ConnectionTaskServiceImpl only that now has the responsibility to switch defaults
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
        List<ConnectionTaskProperty> allProperties = new ArrayList<>();
        TypedProperties partialProperties = this.getPartialConnectionTask().getTypedProperties();
        for (String propertyName : partialProperties.propertyNames()) {
            allProperties.add(
                    new ConnectionTaskPropertyImpl(
                            this, propertyName,
                            partialProperties.getProperty(propertyName),
                            Range.all(),
                            this.getPartialConnectionTask().getPluggableClass())
            );
        }
        return this.merge(allProperties, this.getAllProperties(date));
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

    protected List<ConnectionProperty> toConnectionProperties(List<ConnectionTaskProperty> properties) {
        List<ConnectionProperty> connectionProperties = new ArrayList<>(properties.size());
        for (ConnectionTaskProperty property : properties) {
            connectionProperties.add(property);
        }
        return connectionProperties;
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
                Save.UPDATE.save(this.getDataModel(), this);
            }
            catch (ConstraintViolationException e) {
                /* Assumption: no changes on this ConnectionTask
                 * therefore: exception relates to missing required properties
                 * so set the status to Incomplete and apply change. */
                this.setStatus(ConnectionTaskLifecycleStatus.INCOMPLETE);
                this.getDataModel().update(this, ConnectionTaskFields.STATUS.fieldName());
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

    boolean isActive() {
        return this.status.equals(ConnectionTaskLifecycleStatus.ACTIVE);
    }

    @Override
    protected void validateAndUpdate() {
        if (this.pluggableClass == null) {
            this.loadPluggableClass();
        }
        super.validateAndUpdate();
    }

    @Override
    protected void update() {
        if (this.pluggableClass == null) {
            this.loadPluggableClass();
        }
        super.update();
    }

    @Override
    public boolean isExecuting() {
        return this.comServer.isPresent();
    }

    @Override
    public ComServer getExecutingComServer() {
        return comServer.orNull();
    }

    public void setExecutingComServer(ComServer comServer) {
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

    /**
     * This will use the validation framework to detect if there are any Validation errors.
     *
     * @return true if everything is valid, false otherwise
     */
    boolean isValidConnectionTask() {
        try {
            Save.CREATE.validate(this.getDataModel(), this, Save.Update.class);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Validates that {@link ConnectionTaskProperty connection task properties}
     * that are provided at connection time, were originally returned by
     * the connection task that is connecting.
     */
    protected interface ConnectionTaskPropertyValidator {

        public void validate(List<ConnectionTaskProperty> properties);

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