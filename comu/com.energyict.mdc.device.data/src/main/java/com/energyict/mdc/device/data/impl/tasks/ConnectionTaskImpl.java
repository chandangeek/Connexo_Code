package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.exceptions.CannotDeleteUsedDefaultConnectionTaskException;
import com.energyict.mdc.device.data.exceptions.CannotUpdateObsoleteConnectionTaskException;
import com.energyict.mdc.device.data.exceptions.ConnectionTaskIsAlreadyObsoleteException;
import com.energyict.mdc.device.data.exceptions.ConnectionTaskIsExecutingAndCannotBecomeObsoleteException;
import com.energyict.mdc.device.data.exceptions.DuplicateConnectionTaskException;
import com.energyict.mdc.device.data.exceptions.IncompatiblePartialConnectionTaskException;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.exceptions.PartialConnectionTaskNotPartOfDeviceConfigurationException;
import com.energyict.mdc.device.data.impl.CreateEventType;
import com.energyict.mdc.device.data.impl.DeleteEventType;
import com.energyict.mdc.device.data.impl.PersistentIdObject;
import com.energyict.mdc.device.data.impl.UpdateEventType;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskProperty;
import com.energyict.mdc.dynamic.relation.Relation;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.inject.Provider;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import static com.elster.jupiter.util.Checks.is;

/**
 * Provides an implementation for the {@link ConnectionTask} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-16 (09:08)
 */
@XmlRootElement
@HasValidProperties(groups = {Save.Create.class, Save.Update.class})
public abstract class ConnectionTaskImpl<PCTT extends PartialConnectionTask, CPPT extends ComPortPool>
        extends PersistentIdObject<ConnectionTask>
        implements ConnectionTask<CPPT, PCTT>, PersistenceAware {

    public static final String INITIATOR_DISCRIMINATOR = "0";
    public static final String INBOUND_DISCRIMINATOR = "1";
    public static final String SCHEDULED_DISCRIMINATOR = "2";
    public static final Map<String, Class<? extends ConnectionTask>> IMPLEMENTERS =
            ImmutableMap.<String, Class<? extends ConnectionTask>>of(
                    INITIATOR_DISCRIMINATOR, ConnectionInitiationTaskImpl.class,
                    INBOUND_DISCRIMINATOR, InboundConnectionTaskImpl.class,
                    SCHEDULED_DISCRIMINATOR, ScheduledConnectionTaskImpl.class);

    private long deviceId;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.CONNECTION_TASK_DEVICE_REQUIRED_KEY + "}")
    private Device device;
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.CONNECTION_TASK_PARTIAL_CONNECTION_TASK_REQUIRED_KEY + "}")
    private Reference<PCTT> partialConnectionTask = ValueReference.absent();
    private boolean isDefault = false;
    private ConnectionTaskLifecycleStatus status = ConnectionTaskLifecycleStatus.INCOMPLETE;
    private Date obsoleteDate;
    private Date lastCommunicationStart;
    private Date lastSuccessfulCommunicationEnd;
    private Reference<ConnectionMethod> connectionMethod = ValueReference.absent();
    // Redundant copy of the ConnectionMethod's com port pool for query purposes to avoid extra join
    // Need the validator to combine the validations for the frontend
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.CONNECTION_METHOD_COMPORT_POOL_REQUIRED_KEY + "}")
    private Reference<CPPT> comPortPool = ValueReference.absent();
    private Reference<ComServer> comServer = ValueReference.absent();
    private Date modificationDate;

    private final Clock clock;
    private final DeviceDataService deviceDataService;

    private Provider<ConnectionMethodImpl> connectionMethodProvider;
    private boolean allowIncomplete = true;

    protected ConnectionTaskImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, DeviceDataService deviceDataService, Provider<ConnectionMethodImpl> connectionMethodProvider) {
        super(ConnectionTask.class, dataModel, eventService, thesaurus);
        this.clock = clock;
        this.deviceDataService = deviceDataService;
        this.connectionMethodProvider = connectionMethodProvider;
    }

    public void initialize(Device device, PCTT partialConnectionTask, CPPT comPortPool, ConnectionTaskLifecycleStatus status) {
        this.device = device;
        this.status = status;
        this.deviceId = device.getId();
        this.validatePartialConnectionTaskType(partialConnectionTask);
        this.validateConstraint(partialConnectionTask, device);
        this.validateSameConfiguration(partialConnectionTask, device);
        this.partialConnectionTask.set(partialConnectionTask);
        this.comPortPool.set(comPortPool);
        this.connectionMethod.set(this.connectionMethodProvider.get().initialize(this, partialConnectionTask.getPluggableClass(), comPortPool));
    }

    @Override
    public void postLoad() {
        if(this.connectionMethod.isPresent()){
            ((ConnectionMethodImpl) getConnectionMethod()).loadConnectionTask(this);
        }
        this.allowIncomplete = this.status.equals(ConnectionTaskLifecycleStatus.INCOMPLETE);
    }

    boolean isAllowIncomplete(){
        return this.allowIncomplete;
    }

    private void validatePartialConnectionTaskType(PCTT partialConnectionTask) {
        Class<PCTT> partialConnectionTaskType = this.getPartialConnectionTaskType();
        if (!partialConnectionTaskType.isAssignableFrom(partialConnectionTask.getClass())) {
            throw new IncompatiblePartialConnectionTaskException(this.getThesaurus(), partialConnectionTask, partialConnectionTaskType);
        }
    }

    private void validateConstraint(PCTT partialConnectionTask, Device device) {
        Optional<ConnectionTask> result = this.deviceDataService.findConnectionTaskForPartialOnDevice(partialConnectionTask, device);
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
        if (this.getConnectionMethod() != null) {
            this.getConnectionMethod().delete(); // this will delete all the relationProperties as well
        }
        this.deleteDependents();
        this.getDataMapper().remove(this);
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
    public void activateAndSave() {
        this.status = ConnectionTaskLifecycleStatus.ACTIVE;
        save();
    }

    @Override
    public void save() {
        if (device == null) {
            loadDevice();
        }
        this.deviceId = device.getId();
        this.validateNotObsolete();
        this.modificationDate = this.now();

        /**
         * Need to validate the ConnectionTask FIRST, then save the ConnectionMethod, then save the ConnectionTask ...
         */
        if(getId() == 0){
            Save.CREATE.validate(this.getDataModel(), this);
        } else {
            Save.UPDATE.validate(this.getDataModel(), this);
        }

        this.getConnectionMethod().save();
        super.save();
        this.getConnectionMethod().saveAllProperties();
    }

    protected Date now() {
        return this.clock.now();
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
        this.obsoleteDate = this.now();
        this.makeDependentsObsolete();
        this.unRegisterConnectionTaskFromComTasks();
        this.post();
    }

    /**
     * We need to check if this task is currently running or someone else made it obsolete.
     * We are already in a Transaction so we don't wrap it again.
     */
    private void reloadComServerAndObsoleteDate() {
        ConnectionTask updatedVersionOfMyself = this.deviceDataService.findConnectionTask(this.getId()).get();
        this.comServer.set(updatedVersionOfMyself.getExecutingComServer());
        this.obsoleteDate = updatedVersionOfMyself.getObsoleteDate();
    }

    protected void makeDependentsObsolete() {
        this.obsoleteAllProperties();
    }

    /**
     * Makes the {@link Relation}s that hold the values of
     * all the {@link ConnectionTaskProperty ConnectionTaskProperties} obsolete.
     */
    protected void obsoleteAllProperties() {
        // The ConnectionTaskProperties are actually stored on the ConnectionMethod
        if (getConnectionMethod() != null) {
            getConnectionMethod().makeObsolete();
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
            return this.deviceDataService.findDeviceById(deviceId);
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
        return this.deviceDataService.findComTaskExecutionsByConnectionTask(this);
    }

    // Keep as reference for ConnectionTaskExecutionAspects implementation in the mdc.engine bundle
    public void executionStarted(final ComServer comServer) {
        this.doExecutionStarted(comServer);
        this.post();
    }

    protected void doExecutionStarted(ComServer comServer) {
        this.setExecutingComServer(comServer);
        this.lastCommunicationStart = this.now();
    }

    // Keep as reference for ConnectionTaskExecutionAspects implementation in the mdc.engine bundle
    public void executionCompleted() {
        this.doExecutionCompleted();
        this.post();
    }

    protected void doExecutionCompleted() {
        this.setExecutingComServer(null);
        this.lastSuccessfulCommunicationEnd = this.now();
    }

    public String getName() {
        return getPartialConnectionTask().getName();
    }

    @Override
    public Device getDevice() {
        if (this.device == null) {
            this.loadDevice();
        }
        return this.device;
    }

    private void loadDevice() {
        this.device = this.findDevice(this.deviceId);
    }

    @Override
    public CPPT getComPortPool() {
        return this.comPortPool.orNull();
    }

    @Override
    public void setComPortPool(CPPT comPortPool) {
        this.comPortPool.set(comPortPool);
        this.getConnectionMethod().setComPortPool(comPortPool);
    }

    @Override
    public PCTT getPartialConnectionTask() {
        return this.partialConnectionTask.get();
    }

    @Override
    public Date getLastCommunicationStart() {
        return lastCommunicationStart;
    }

    @Override
    public Date getLastSuccessfulCommunicationEnd() {
        return lastSuccessfulCommunicationEnd;
    }

    @Override
    @XmlAttribute
    public Date getObsoleteDate() {
        return this.obsoleteDate;
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

    // To be used by the DeviceDataServiceImpl only that now has the responsibility to switch defaults
    public void setAsDefault() {
        this.doSetAsDefault();
        this.post();
    }

    protected void doSetAsDefault() {
        this.isDefault = true;
    }

    // To be used by the DeviceDataServiceImpl only that now has the responsibility to switch defaults
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
                            propertyName,
                            partialProperties.getProperty(propertyName),
                            this.always(),
                            this.getPartialConnectionTask().getPluggableClass())
            );
        }
        if (this.getConnectionMethod() != null) {
            return this.merge(allProperties, this.getConnectionMethod().getAllProperties(date));
        } else {
            return allProperties;
        }
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
        for (ConnectionTaskProperty property : this.getConnectionMethod().getAllProperties(this.now())) {
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
    public void setProperty(String propertyName, Object value) {
        // Properties are acutally persisted on the ConnectionMethod
        this.getConnectionMethod().setProperty(propertyName, value);
    }

    @Override
    public void removeProperty(String propertyName) {
        // Properties are acutally persisted on the ConnectionMethod
        this.getConnectionMethod().removeProperty(propertyName);
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

    boolean isActive(){
        return this.status.equals(ConnectionTaskLifecycleStatus.ACTIVE);
    }

    @Override
    protected void post() {
        if (device == null) {
            loadDevice();
        }
        super.post();
    }

    public ConnectionMethod getConnectionMethod() {
        return connectionMethod.get();
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
        return getConnectionMethod().getPluggableClass().getConnectionType();
    }

    @Override
    public Date getModificationDate() {
        return this.modificationDate;
    }


    protected TimeZone getClocksTimeZone() {
        return this.clock.getTimeZone();
    }


    /**
     * This will use the validation framework to detect if there are any Validation errors
     * @return true if everything is valid, false otherwise
     */
    boolean isValidConnectionTask(){
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
}