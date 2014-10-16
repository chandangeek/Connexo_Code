package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import java.util.Optional;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ComTaskEnablementBuilder;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceMessageEnablement;
import com.energyict.mdc.device.config.DeviceMessageEnablementBuilder;
import com.energyict.mdc.device.config.DeviceMessageUserAction;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.PartialConnectionInitiationTask;
import com.energyict.mdc.device.config.PartialConnectionInitiationTaskBuilder;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTaskBuilder;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTaskBuilder;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.SecurityPropertySetBuilder;
import com.energyict.mdc.device.config.exceptions.CannotDisableComTaskThatWasNotEnabledException;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.ComTask;

import javax.inject.Inject;
import javax.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Provides an implementation for the {@link DeviceCommunicationConfiguration} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-02-15 (11:02)
 */
public class DeviceCommunicationConfigurationImpl extends PersistentIdObject<DeviceCommunicationConfiguration> implements DeviceCommunicationConfiguration {

    private final SchedulingService schedulingService;
    private final ThreadPrincipalService threadPrincipalService;
    private final DeviceConfigurationService deviceConfigurationService;

    enum Fields {
        COM_TASK_ENABLEMENTS("comTaskEnablements"),
        SECURITY_PROPERTY_SETS("securityPropertySets"),
        DEVICE_MESSAGE_ENABLEMENTS("deviceMessageEnablements");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    private Reference<DeviceConfiguration> deviceConfiguration = ValueReference.absent();
    private List<SecurityPropertySet> securityPropertySets = new ArrayList<>();
    private List<ComTaskEnablement> comTaskEnablements = new ArrayList<>();
    private List<DeviceMessageEnablement> deviceMessageEnablements = new ArrayList<>();
    private boolean supportsAllProtocolMessages;
    private long supportsAllProtocolMessagesUserActionsBitVector = 0L;
    @Valid
    private List<PartialConnectionTask> partialConnectionTasks = new ArrayList<>();
    @Valid
    private List<ProtocolDialectConfigurationProperties> configurationPropertiesList = new ArrayList<>();

    @Inject
    DeviceCommunicationConfigurationImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, SchedulingService schedulingService, ThreadPrincipalService threadPrincipalService, DeviceConfigurationService deviceConfigurationService) {
        super(DeviceCommunicationConfiguration.class, dataModel, eventService, thesaurus);
        this.schedulingService = schedulingService;
        this.threadPrincipalService = threadPrincipalService;
        this.deviceConfigurationService = deviceConfigurationService;
    }

    static DeviceCommunicationConfigurationImpl from(DataModel dataModel, DeviceConfiguration deviceConfiguration) {
        return dataModel.getInstance(DeviceCommunicationConfigurationImpl.class).init(deviceConfiguration);
    }

    private Optional<User> getCurrentUser() {
        Principal principal = threadPrincipalService.getPrincipal();
        if (!(principal instanceof User)) {
            return Optional.empty();
        }
        return Optional.of((User) principal);
    }

    private boolean isUserAuthorizedForAction(DeviceMessageUserAction action, User user) {
        Optional<Privilege> privilege = ((DeviceConfigurationServiceImpl) deviceConfigurationService).findPrivilege(action.getPrivilege());
        return privilege.isPresent() && user.hasPrivilege(privilege.get().getName());
    }

    @Override
    public boolean isAuthorized(DeviceMessageId deviceMessageId) {
        Optional<User> currentUser = getCurrentUser();
        if (currentUser.isPresent()) {
            User user = currentUser.get();
            if (isSupportsAllProtocolMessages()) {
                if(getDeviceConfiguration().getDeviceType().getDeviceProtocolPluggableClass().getDeviceProtocol().getSupportedMessages().contains(deviceMessageId)){
                    return getAllProtocolMessagesUserActions().stream().anyMatch(deviceMessageUserAction -> isUserAuthorizedForAction(deviceMessageUserAction, user));
                }
            }
            java.util.Optional<DeviceMessageEnablement> deviceMessageEnablementOptional = getDeviceMessageEnablements().stream().filter(deviceMessageEnablement -> deviceMessageEnablement.getDeviceMessageId().equals(deviceMessageId)).findAny();
            if (deviceMessageEnablementOptional.isPresent()) {
                return deviceMessageEnablementOptional.get().getUserActions().stream().anyMatch(deviceMessageUserAction -> isUserAuthorizedForAction(deviceMessageUserAction, user));
            }
        }
        return false;
    }

    @Override
    public DeviceConfiguration getDeviceConfiguration() {
        return this.deviceConfiguration.get();
    }

    @Override
    public List<SecurityPropertySet> getSecurityPropertySets() {
        return Collections.unmodifiableList(securityPropertySets);
    }

    @Override
    public List<DeviceMessageEnablement> getDeviceMessageEnablements() {
        return Collections.unmodifiableList(deviceMessageEnablements);
    }

    @Override
    public List<PartialScheduledConnectionTask> getPartialOutboundConnectionTasks() {
        return this.filter(this.findAllPartialConnectionTasks(), new PartialOutboundConnectionTaskFilterPredicate());
    }

    public List<PartialInboundConnectionTask> getPartialInboundConnectionTasks() {
        return this.filter(this.findAllPartialConnectionTasks(), new PartialInboundConnectionTaskFilterPredicate());
    }

    @Override
    public List<PartialConnectionInitiationTask> getPartialConnectionInitiationTasks() {
        return this.filter(this.findAllPartialConnectionTasks(), new PartialConnectionInitiationTaskFilterPredicate());
    }

    @Override
    public List<ProtocolDialectConfigurationProperties> getProtocolDialectConfigurationPropertiesList() {
        return Collections.unmodifiableList(configurationPropertiesList);
    }

    @Override
    public List<PartialConnectionTask> getPartialConnectionTasks() {
        return this.findAllPartialConnectionTasks();
    }

    @Override
    public void remove(PartialConnectionTask partialConnectionTask) {
        ((PersistentIdObject<?>) partialConnectionTask).validateDelete();
        if (partialConnectionTasks.remove(partialConnectionTask) && getId() > 0) {
            eventService.postEvent(((PersistentIdObject) partialConnectionTask).deleteEventType().topic(), partialConnectionTask);
        }
    }

    private List<PartialConnectionTask> findAllPartialConnectionTasks() {
        return Collections.unmodifiableList(this.partialConnectionTasks);
    }

    private <T extends PartialConnectionTask> List<T> filter(List<PartialConnectionTask> needsFiltering, PartialConnectionTaskFilterPredicate filterPredicate) {
        List<T> filtered = new ArrayList<>(needsFiltering.size());  // At most all tasks are retained by the filter
        for (PartialConnectionTask partialConnectionTask : needsFiltering) {
            if (filterPredicate.retain(partialConnectionTask)) {
                filtered.add((T) partialConnectionTask);
            }
        }
        return filtered;
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.DEVICE_COMMUNICATION_CONFIGURATION;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.DEVICE_COMMUNICATION_CONFIGURATION;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.DEVICE_COMMUNICATION_CONFIGURATION;
    }

    @Override
    protected void doDelete() {
        configurationPropertiesList.clear();
        dataModel.mapper(DeviceCommunicationConfiguration.class).remove(this);
    }

    @Override
    protected void validateDelete() {
        //TODO automatically generated method body, provide implementation.
    }

    private DeviceCommunicationConfigurationImpl init(DeviceConfiguration deviceConfiguration) {
        this.deviceConfiguration.set(deviceConfiguration);
        for (DeviceProtocolDialect deviceProtocolDialect : deviceConfiguration.getDeviceType().getDeviceProtocolPluggableClass().getDeviceProtocol().getDeviceProtocolDialects()) {
            findOrCreateProtocolDialectConfigurationProperties(deviceProtocolDialect);
        }
        return this;
    }

    private interface PartialConnectionTaskFilterPredicate<T extends PartialConnectionTask> {

        public boolean retain(PartialConnectionTask task);

    }

    private class PartialOutboundConnectionTaskFilterPredicate implements PartialConnectionTaskFilterPredicate<PartialScheduledConnectionTask> {

        @Override
        public boolean retain(PartialConnectionTask task) {
            return task instanceof PartialScheduledConnectionTask;
        }
    }

    private class PartialInboundConnectionTaskFilterPredicate implements PartialConnectionTaskFilterPredicate<PartialInboundConnectionTask> {

        @Override
        public boolean retain(PartialConnectionTask task) {
            return task instanceof PartialInboundConnectionTaskImpl;
        }
    }

    private class PartialConnectionInitiationTaskFilterPredicate implements PartialConnectionTaskFilterPredicate<PartialConnectionInitiationTask> {

        @Override
        public boolean retain(PartialConnectionTask task) {
            return task instanceof PartialConnectionInitiationTaskImpl;
        }
    }

    @Override
    public void setSupportsAllProtocolMessagesWithUserActions(boolean supportAllProtocolMessages, DeviceMessageUserAction... deviceMessageUserActions) {
        this.supportsAllProtocolMessages = supportAllProtocolMessages;
        if (this.supportsAllProtocolMessages) {
            this.deviceMessageEnablements.clear();
            if (deviceMessageUserActions.length > 0) {
                this.supportsAllProtocolMessagesUserActionsBitVector = toDatabaseValue(EnumSet.copyOf(Arrays.asList(deviceMessageUserActions)));
            }
        } else {
            this.supportsAllProtocolMessagesUserActionsBitVector = 0;
        }
    }

    @Override
    public boolean isSupportsAllProtocolMessages() {
        return supportsAllProtocolMessages;
    }

    @Override
    public Set<DeviceMessageUserAction> getAllProtocolMessagesUserActions() {
        return fromDatabaseValue(this.supportsAllProtocolMessagesUserActionsBitVector);
    }

    /**
     * Returns an appropriate database value for the specified set of enum values.
     *
     * @param enumValues The set of enum values
     * @return The database value that is ready to be set on a PreparedStatement
     */
    private long toDatabaseValue(EnumSet<DeviceMessageUserAction> enumValues) {
        long dbValue = 0;
        long bitValue = 1;
        for (DeviceMessageUserAction enumValue : DeviceMessageUserAction.values()) {
            if (enumValues.contains(enumValue)) {
                dbValue = dbValue + bitValue;
            }
            bitValue = bitValue * 2;
        }
        return dbValue;
    }

    /**
     * Returns the set of enum values represented by the bit vector
     * that was read from a ResultSet. It is assumed that the bit vector
     * was produced by this same class.
     *
     * @param dbValue The bit vector that was stored as a database value earlier
     * @return The set of enum values
     */
    private EnumSet<DeviceMessageUserAction> fromDatabaseValue(long dbValue) {
        long bitPattern = 1;
        EnumSet<DeviceMessageUserAction> enumValues = EnumSet.noneOf(DeviceMessageUserAction.class);
        for (DeviceMessageUserAction enumValue : DeviceMessageUserAction.values()) {
            if ((dbValue & bitPattern) != 0) {
                // The bit for this enum value is set
                enumValues.add(enumValue);
            }
            bitPattern = bitPattern * 2;
        }
        return enumValues;
    }

    @Override
    public void addSecurityPropertySet(SecurityPropertySet securityPropertySet) {
        Save.CREATE.validate(dataModel, securityPropertySet);
        securityPropertySets.add(securityPropertySet);
    }

    @Override
    public PartialScheduledConnectionTaskBuilder newPartialScheduledConnectionTask(String name, ConnectionTypePluggableClass connectionType, TimeDuration rescheduleRetryDelay, ConnectionStrategy connectionStrategy) {
        return new PartialScheduledConnectionTaskBuilderImpl(dataModel, this, schedulingService, eventService).name(name)
                .pluggableClass(connectionType)
                .rescheduleDelay(rescheduleRetryDelay)
                .connectionStrategy(connectionStrategy);
    }

    @Override
    public PartialInboundConnectionTaskBuilder newPartialInboundConnectionTask(String name, ConnectionTypePluggableClass connectionType) {
        return new PartialInboundConnectionTaskBuilderImpl(dataModel, this)
                .name(name)
                .pluggableClass(connectionType);
    }

    @Override
    public PartialConnectionInitiationTaskBuilder newPartialConnectionInitiationTask(String name, ConnectionTypePluggableClass connectionType, TimeDuration rescheduleRetryDelay) {
        return new PartialConnectionInitiationTaskBuilderImpl(dataModel, this, schedulingService, eventService)
                .name(name)
                .pluggableClass(connectionType)
                .rescheduleDelay(rescheduleRetryDelay);
    }

    public void addPartialConnectionTask(PartialConnectionTask partialConnectionTask) {
        Save.CREATE.validate(dataModel, partialConnectionTask);
        partialConnectionTasks.add(partialConnectionTask);
    }

    @Override
    public ProtocolDialectConfigurationProperties findOrCreateProtocolDialectConfigurationProperties(DeviceProtocolDialect protocolDialect) {
        for (ProtocolDialectConfigurationProperties candidate : configurationPropertiesList) {
            if (candidate.getDeviceProtocolDialect().getDeviceProtocolDialectName().equals(protocolDialect.getDeviceProtocolDialectName())) {
                return candidate;
            }
        }
        ProtocolDialectConfigurationProperties props = ProtocolDialectConfigurationPropertiesImpl.from(dataModel, this, protocolDialect);
        configurationPropertiesList.add(props);
        return props;
    }

    @Override
    public SecurityPropertySetBuilder createSecurityPropertySet(String name) {
        return new InternalSecurityPropertySetBuilder(name);
    }

    @Override
    public void removeSecurityPropertySet(SecurityPropertySet propertySet) {
        if (propertySet != null) {
            ((SecurityPropertySetImpl) propertySet).validateDelete();
            securityPropertySets.remove(propertySet);
        }
    }

    @Override
    public DeviceMessageEnablementBuilder createDeviceMessageEnablement(DeviceMessageId deviceMessageId) {
        return new InternalDeviceMessageEnablementBuilder(deviceMessageId);
    }

    @Override
    public boolean removeDeviceMessageEnablement(DeviceMessageId deviceMessageId) {
        return this.deviceMessageEnablements.removeIf(deviceMessageEnablement -> deviceMessageEnablement.getDeviceMessageId().equals(deviceMessageId));
    }

    @Override
    public void save() {
        boolean creating = getId() == 0;
        super.save();
        if (creating) {
            for (PartialConnectionTask partialConnectionTask : partialConnectionTasks) {
                eventService.postEvent(((PersistentIdObject) partialConnectionTask).createEventType().topic(), partialConnectionTask);
            }
        }
    }

    @Override
    public List<ComTaskEnablement> getComTaskEnablements() {
        return Collections.unmodifiableList(this.comTaskEnablements);
    }

    @Override
    public Optional<ComTaskEnablement> getComTaskEnablementFor(ComTask comTask) {
        for (ComTaskEnablement comTaskEnablement : this.comTaskEnablements) {
            if (comTask.getId() == comTaskEnablement.getComTask().getId()) {
                return Optional.of(comTaskEnablement);
            }
        }
        return Optional.empty();
    }

    @Override
    public ComTaskEnablementBuilder enableComTask(ComTask comTask, SecurityPropertySet securityPropertySet) {
        ComTaskEnablementImpl underConstruction = dataModel.getInstance(ComTaskEnablementImpl.class).initialize(DeviceCommunicationConfigurationImpl.this, comTask, securityPropertySet);
        return new ComTaskEnablementBuilderImpl(underConstruction);
    }

    @Override
    public void disableComTask(ComTask comTask) {
        Iterator<ComTaskEnablement> comTaskEnablementIterator = this.comTaskEnablements.iterator();
        while (comTaskEnablementIterator.hasNext()) {
            ComTaskEnablement comTaskEnablement = comTaskEnablementIterator.next();
            if (comTaskEnablement.getComTask().getId() == comTask.getId()) {
                ComTaskEnablementImpl each = (ComTaskEnablementImpl) comTaskEnablement;
                each.validateDelete();
                comTaskEnablementIterator.remove();
                return;
            }
        }
        throw new CannotDisableComTaskThatWasNotEnabledException(this.getThesaurus(), this.getDeviceConfiguration(), comTask);
    }

    private void addComTaskEnablement(ComTaskEnablementImpl comTaskEnablement) {
        comTaskEnablement.adding();
        Save.CREATE.validate(this.dataModel, comTaskEnablement);
        this.comTaskEnablements.add(comTaskEnablement);
        comTaskEnablement.added();
    }

    private class InternalDeviceMessageEnablementBuilder implements DeviceMessageEnablementBuilder {

        private final DeviceMessageEnablement underConstruction;

        private InternalDeviceMessageEnablementBuilder(DeviceMessageId deviceMessageId) {
            this.underConstruction = DeviceMessageEnablementImpl.from(dataModel, DeviceCommunicationConfigurationImpl.this, deviceMessageId);
        }

        @Override
        public DeviceMessageEnablementBuilder addUserAction(DeviceMessageUserAction deviceMessageUserAction) {
            this.underConstruction.addDeviceMessageUserAction(deviceMessageUserAction);
            return this;
        }

        @Override
        public DeviceMessageEnablementBuilder addUserActions(DeviceMessageUserAction... deviceMessageUserActions) {
            Arrays.asList(deviceMessageUserActions).stream().forEach(this.underConstruction::addDeviceMessageUserAction);
            return this;
        }

        @Override
        public DeviceMessageEnablement build() {
            DeviceCommunicationConfigurationImpl.this.addDeviceMessageEnablement(underConstruction);
            return underConstruction;
        }
    }

    private void addDeviceMessageEnablement(DeviceMessageEnablement singleDeviceMessageEnablement) {
        Save.CREATE.validate(dataModel, singleDeviceMessageEnablement);
        this.deviceMessageEnablements.add(singleDeviceMessageEnablement);
        this.setSupportsAllProtocolMessagesWithUserActions(false);
        this.save();
    }

    private class InternalSecurityPropertySetBuilder implements SecurityPropertySetBuilder {

        private final SecurityPropertySetImpl underConstruction;

        private InternalSecurityPropertySetBuilder(String name) {
            this.underConstruction = SecurityPropertySetImpl.from(dataModel, DeviceCommunicationConfigurationImpl.this, name);
        }

        @Override
        public SecurityPropertySetBuilder authenticationLevel(int level) {
            underConstruction.setAuthenticationLevel(level);
            return this;
        }

        @Override
        public SecurityPropertySetBuilder encryptionLevel(int level) {
            underConstruction.setEncryptionLevelId(level);
            return this;
        }

        @Override
        public SecurityPropertySetBuilder addUserAction(DeviceSecurityUserAction userAction) {
            underConstruction.addUserAction(userAction);
            return this;
        }

        @Override
        public SecurityPropertySet build() {
            DeviceCommunicationConfigurationImpl.this.addSecurityPropertySet(underConstruction);
            return underConstruction;
        }
    }

    private enum ComTaskEnablementBuildingMode {
        UNDERCONSTRUCTION {
            @Override
            protected void verify() {
                // All calls are fine as long as we are under construction
            }
        },
        COMPLETE {
            @Override
            protected void verify() {
                throw new IllegalStateException("The communication task enablement building process is already complete");
            }
        };

        protected abstract void verify();
    }

    private class ComTaskEnablementBuilderImpl implements ComTaskEnablementBuilder {

        private ComTaskEnablementBuildingMode mode;
        private ComTaskEnablementImpl underConstruction;

        private ComTaskEnablementBuilderImpl(ComTaskEnablementImpl underConstruction) {
            super();
            this.mode = ComTaskEnablementBuildingMode.UNDERCONSTRUCTION;
            this.underConstruction = underConstruction;
        }

        @Override
        public ComTaskEnablementBuilder setIgnoreNextExecutionSpecsForInbound(boolean flag) {
            this.mode.verify();
            this.underConstruction.setIgnoreNextExecutionSpecsForInbound(flag);
            return this;
        }

        @Override
        public ComTaskEnablementBuilder setPartialConnectionTask(PartialConnectionTask partialConnectionTask) {
            this.mode.verify();
            this.underConstruction.setPartialConnectionTask(partialConnectionTask);
            return this;
        }

        @Override
        public ComTaskEnablementBuilder setProtocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties properties) {
            this.mode.verify();
            this.underConstruction.setProtocolDialectConfigurationProperties(properties);
            return this;
        }

        @Override
        public ComTaskEnablementBuilder useDefaultConnectionTask(boolean flagValue) {
            this.mode.verify();
            this.underConstruction.useDefaultConnectionTask(flagValue);
            return this;
        }

        @Override
        public ComTaskEnablementBuilder setPriority(int priority) {
            this.mode.verify();
            this.underConstruction.setPriority(priority);
            return this;
        }

        @Override
        public ComTaskEnablement add() {
            this.mode.verify();
            addComTaskEnablement(this.underConstruction);
            this.mode = ComTaskEnablementBuildingMode.COMPLETE;
            return this.underConstruction;
        }
    }
}