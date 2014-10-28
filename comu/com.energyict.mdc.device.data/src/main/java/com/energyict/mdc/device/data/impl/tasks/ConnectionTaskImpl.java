package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.PartialConnectionTask;
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
import com.energyict.mdc.device.data.impl.PersistentIdObject;
import com.energyict.mdc.device.data.impl.PropertyCache;
import com.energyict.mdc.device.data.impl.PropertyFactory;
import com.energyict.mdc.device.data.impl.RelationTransactionExecutor;
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
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.pluggable.PluggableClassWithRelationSupport;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.google.common.collect.ImmutableMap;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
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
        ConnectionTask<CPPT, PCTT>,
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

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.CONNECTION_TASK_DEVICE_REQUIRED_KEY + "}")
    private Reference<Device> device = ValueReference.absent();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.CONNECTION_TASK_PARTIAL_CONNECTION_TASK_REQUIRED_KEY + "}")
    private Reference<PCTT> partialConnectionTask = ValueReference.absent();
    private boolean isDefault = false;
    private ConnectionTaskLifecycleStatus status = ConnectionTaskLifecycleStatus.INCOMPLETE;
    private Instant obsoleteDate;
    private Instant lastCommunicationStart;
    private Instant lastSuccessfulCommunicationEnd;
    private transient PropertyCache<ConnectionType, ConnectionTaskProperty> cache;
    private long pluggableClassId;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.CONNECTION_TASK_PLUGGABLE_CLASS_REQUIRED_KEY + "}")
    private ConnectionTypePluggableClass pluggableClass;
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.CONNECTION_TASK_COMPORT_POOL_REQUIRED_KEY + "}")
    private Reference<CPPT> comPortPool = ValueReference.absent();
    private Reference<ComServer> comServer = ValueReference.absent();
    private Reference<ComSession> lastSession = ValueReference.absent();
    private boolean lastSessionStatus; // Redundant copy from lastSession to improve query performance
    private ComSession.SuccessIndicator lastSessionSuccessIndicator;   // Redundant copy from lastSession to improve query performance
    private Instant modificationDate;

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
            ((ComTaskExecutionImpl) comTaskExecution).connectionTaskRemoved();
        }
    }

    @Override
    public void save() {
        this.validateNotObsolete();
        this.modificationDate = clock.instant();
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
                        new SimpleRelationTransactionExecutor<ConnectionType>(
                                this,
                                Date.from(clock.instant()),
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
        for (ConnectionTaskProperty property : properties) {
            transactionExecutor.add(property);
        }
        transactionExecutor.execute();
        this.clearPropertyCache();
    }

    protected Date now() {
        return Date.from(clock.instant());
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
        this.obsoleteDate = clock.instant();
        this.makeDependentsObsolete();
        this.unRegisterConnectionTaskFromComTasks();
        this.post();
    }

    /**
     * We need to check if this task is currently running or someone else made it obsolete.
     * We are already in a Transaction so we don't wrap it again.
     */
    private void reloadComServerAndObsoleteDate() {
        ConnectionTask updatedVersionOfMyself = this.connectionTaskService.findConnectionTask(this.getId()).get();
        this.comServer.set(updatedVersionOfMyself.getExecutingComServer());
        this.obsoleteDate = updatedVersionOfMyself.getObsoleteDate() == null ? null : updatedVersionOfMyself.getObsoleteDate().toInstant();
    }

    protected void makeDependentsObsolete() {
        this.obsoleteAllProperties();
    }

    /**
     * Makes the {@link Relation}s that hold the values of
     * all the {@link ConnectionTaskProperty ConnectionTaskProperties} obsolete.
     */
    protected void obsoleteAllProperties() {
        List<Relation> relations = this.getPluggableClass().getRelations(this, new Interval(null, null));
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

    protected Device findDevice(long deviceId) {
        if (deviceId != 0) {
            return this.deviceService.findDeviceById(deviceId);
        }
        return null;
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

    // Keep as reference for ConnectionTaskExecutionAspects implementation in the mdc.engine bundle
    public void executionStarted(final ComServer comServer) {
        this.doExecutionStarted(comServer);
        this.post();
    }

    protected void doExecutionStarted(ComServer comServer) {
        this.setExecutingComServer(comServer);
        this.lastCommunicationStart = clock.instant();
    }

    // Keep as reference for ConnectionTaskExecutionAspects implementation in the mdc.engine bundle
    public void executionCompleted() {
        this.doExecutionCompleted();
        this.post();
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
        this.pluggableClass = this.findConnectionTypePluggableClass(this.pluggableClassId);
    }

    private RelationType findRelationType() {
        return this.getPluggableClass().findRelationType();
    }

    private ConnectionTypePluggableClass findConnectionTypePluggableClass(long connectionTypePluggableClassId) {
        return this.protocolPluggableService.findConnectionTypePluggableClass(connectionTypePluggableClassId);
    }

    private List<PropertySpec> getPluggablePropetySpecs() {
        return this.getPluggableClass().getConnectionType().getPropertySpecs();
    }

    @Override
    public Relation getDefaultRelation() {
        return this.getDefaultRelation(Date.from(clock.instant()));
    }

    @Override
    public Relation getDefaultRelation(Date date) {
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
        return this.getAllProperties(Date.from(clock.instant()));
    }

    public List<ConnectionTaskProperty> getAllProperties(Date date) {
        return this.getAllLocalProperties(date);
    }

    private List<ConnectionTaskProperty> getAllLocalProperties(Date date) {
        return this.cache.get(date);
    }

    @Override
    public List<ConnectionTaskProperty> loadProperties(Date date) {
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
    public List<ConnectionTaskProperty> loadProperties(Interval interval) {
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

    private ConnectionTaskProperty newInheritedPropertyFor(String propertyName, Object propertyValue) {
        return new ConnectionTaskPropertyImpl(this, propertyName, propertyValue, this.always(), this.getPluggableClass());
    }

    @Override
    public ConnectionTaskProperty newProperty(String name, Object value, Date activeDate) {
        ConnectionTaskPropertyImpl property = new ConnectionTaskPropertyImpl(this, name);
        property.setValue(value);
        property.setActivePeriod(new Interval(activeDate, null));
        return property;
    }

    @Override
    public void setProperty(String propertyName, Object value) {
        Date now = Date.from(clock.instant());
        this.getAllProperties(now); // Make sure the cache is loaded to avoid that writing to the cache is reverted when the client will call getTypedProperties right after this call
        this.cache.put(now, propertyName, value);
    }

    @Override
    public void removeProperty(String propertyName) {
        Date now = Date.from(clock.instant());
        this.getAllProperties(now); // Make sure the cache is loaded to avoid that writing to the cache is reverted when the client will call getTypedProperties right after this call
        this.cache.remove(now, propertyName);
    }

    @Override
    public Object get(String propertyName, Date date) {
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
        return this.get(attributeName, Date.from(clock.instant()));
    }

    @Override
    public Object get(RelationAttributeType attributeType, Date date) {
        return this.get(attributeType.getName(), date);
    }

    @Override
    public Object get(RelationAttributeType attributeType) {
        return this.get(attributeType, Date.from(clock.instant()));
    }

    @Override
    public List<RelationType> getAvailableRelationTypes() {
        return this.relationService.findRelationTypesByParticipant(this);
    }

    @Override
    public List<Relation> getRelations(RelationAttributeType attrib, Date date, boolean includeObsolete) {
        return attrib.getRelations(this, date, includeObsolete, 0, 0);
    }

    @Override
    public List<Relation> getRelations(RelationAttributeType attrib, Date date, boolean includeObsolete, int fromRow, int toRow) {
        return attrib.getRelations(this, date, includeObsolete, fromRow, toRow);
    }

    @Override
    public List<Relation> getAllRelations(RelationAttributeType attrib) {
        return attrib.getAllRelations(this);
    }

    @Override
    public List<Relation> getRelations(RelationAttributeType defaultAttribute, Interval period, boolean includeObsolete) {
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
                this.setLastSession(session);
                this.post();
            }
        } else {
            this.setLastSession(session);
            this.post();
        }
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
    public Date getObsoleteDate() {
        return obsoleteDate == null ? null : Date.from(obsoleteDate);
    }

    @Override
    @XmlAttribute
    public ConnectionTaskLifecycleStatus getStatus() {
        return this.status;
    }

    @Override
    public boolean isDefault() {
        return this.isDefault;
    }

    // To be used by the DeviceServiceImpl only that now has the responsibility to switch defaults
    public void setAsDefault() {
        this.doSetAsDefault();
        this.post();
    }

    protected void doSetAsDefault() {
        this.isDefault = true;
    }

    // To be used by the DeviceServiceImpl only that now has the responsibility to switch defaults
    public void clearDefault() {
        this.isDefault = false;
        this.post();
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

    public List<ConnectionTaskProperty> getProperties(Date date) {
        List<ConnectionTaskProperty> allProperties = new ArrayList<>();
        TypedProperties partialProperties = this.getPartialConnectionTask().getTypedProperties();
        for (String propertyName : partialProperties.propertyNames()) {
            allProperties.add(
                    new ConnectionTaskPropertyImpl(
                            this, propertyName,
                            partialProperties.getProperty(propertyName),
                            this.always(),
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

    private Interval always() {
        return new Interval(null, null);
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
    public void deactivate() {
        this.status = ConnectionTaskLifecycleStatus.INACTIVE;
        post();
    }

    @Override
    public void activate() {
        this.status = ConnectionTaskLifecycleStatus.ACTIVE;
        post();
    }

    boolean isActive() {
        return this.status.equals(ConnectionTaskLifecycleStatus.ACTIVE);
    }

    @Override
    protected void post() {
        if (this.pluggableClass == null) {
            this.loadPluggableClass();
        }
        super.post();
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

    @Override
    public Date getModificationDate() {
        return modificationDate == null ? null : Date.from(modificationDate);
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

    void setStatus(ConnectionTaskLifecycleStatus status) {
        this.status = status;
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