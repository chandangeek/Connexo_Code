package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.energyict.mdc.device.config.*;
import com.energyict.mdc.device.config.events.EventType;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.tasks.ComTask;
import org.hibernate.validator.constraints.Range;

import javax.inject.Inject;
import javax.validation.Valid;
import java.time.Instant;
import java.util.Optional;

import static com.elster.jupiter.util.Checks.is;

/**
 * Provides an implementation for the {@link ComTaskEnablement} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-21 (11:41)
 */
@ComTaskIsEnabledOnlyOncePerConfiguration(groups = {Save.Create.class})
@NoPartialConnectionTaskWhenDefaultIsUsed(groups = {Save.Create.class, Save.Update.class})
@ProtocolDialectConfigurationPropertiesMustBeFromSameConfiguration(groups = {Save.Create.class, Save.Update.class})
@SecurityPropertySetMustBeFromSameConfiguration(groups = {Save.Create.class, Save.Update.class})
public class ComTaskEnablementImpl extends PersistentIdObject<ComTaskEnablement> implements ServerComTaskEnablement, PersistenceAware {

    enum Fields {
        COM_TASK("comTask"),
        CONFIGURATION("deviceConfiguration"),
        SECURITY_PROPERTY_SET("securityPropertySet"),
        NEXT_EXECUTION_SPECS("nextExecutionSpecs"),
        IGNORE_NEXT_EXECUTION_SPECS_FOR_INBOUND("ignoreNextExecutionSpecsForInbound"),
        USE_DEFAULT_CONNECTION_TASK("usesDefaultConnectionTask"),
        PARTIAL_CONNECTION_TASK("partialConnectionTask"),
        PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES("protocolDialectConfigurationProperties"),
        SUSPENDED("suspended"),
        PRIORITY("priority");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<ComTask> comTask = ValueReference.absent();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<DeviceConfiguration> deviceConfiguration = ValueReference.absent();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<SecurityPropertySet> securityPropertySet = ValueReference.absent();
    private SaveStrategy saveStrategy = new NoopAtCreationTime();
    private boolean ignoreNextExecutionSpecsForInbound;
    @Range(min = HIGHEST_PRIORITY, max = LOWEST_PRIORITY, message = "{" + MessageSeeds.Keys.COM_TASK_ENABLEMENT_PRIORITY_RANGE + "}")
    private int priority = DEFAULT_PRIORITY;
    private boolean usesDefaultConnectionTask = true;
    @Valid
    private Reference<PartialConnectionTask> partialConnectionTask = ValueReference.absent();
    private boolean suspended;
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<ProtocolDialectConfigurationProperties> protocolDialectConfigurationProperties = ValueReference.absent();
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    @Inject
    protected ComTaskEnablementImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(ComTaskEnablement.class, dataModel, eventService, thesaurus);
    }

    ComTaskEnablementImpl initialize(DeviceConfiguration deviceConfiguration, ComTask comTask, SecurityPropertySet securityPropertySet, ProtocolDialectConfigurationProperties configurationProperties) {
        this.deviceConfiguration.set(deviceConfiguration);
        this.comTask.set(comTask);
        this.securityPropertySet.set(securityPropertySet);
        this.protocolDialectConfigurationProperties.set(configurationProperties);
        return this;
    }

    @Override
    public void postLoad() {
        this.cleanSaveStrategy();
    }

    private void cleanSaveStrategy() {
        this.saveStrategy = new NoopAtUpdateTime();
    }

    /**
     * Notifies this ComTaskEnablement that it is about to be added to the owning DeviceCommunicationConfiguration.
     */
    void adding() {
        this.saveStrategy.prepare();
    }

    /**
     * Notifies this ComTaskEnablement that it was added to the owning DeviceCommunicationConfiguration.
     */
    void added() {
        this.cleanSaveStrategy();
    }

    @Override
    protected void postNew() {
        this.saveStrategy.prepare();
        super.postNew();
        this.saveStrategy.complete();
        this.cleanSaveStrategy();
    }

    @Override
    protected void post() {
        this.saveStrategy.prepare();
        super.post();
        this.saveStrategy.complete();
        this.cleanSaveStrategy();
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.COMTASKENABLEMENT;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.COMTASKENABLEMENT;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.COMTASKENABLEMENT;
    }

    @Override
    protected void doDelete() {
        this.getDataModel().remove(this);
    }

    @Override
    public ProtocolDialectConfigurationProperties getProtocolDialectConfigurationProperties() {
        /* Since this is a required property,
         * we could actually use get() but we
         * want the javax.validation components
         * to validate that this property is not null
         * so we are using orElse(null) instead. */
        return this.protocolDialectConfigurationProperties.orElse(null);
    }

    @Override
    public void setProtocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties properties) {
        this.protocolDialectConfigurationProperties.set(properties);
    }

    @Override
    public boolean isSuspended() {
        return this.suspended;
    }

    @Override
    public void suspend() {
        this.suspended = true;
        this.getEventService().postEvent(EventType.COMTASKENABLEMENT_SUSPEND.topic(), this);
        this.post();
    }

    @Override
    public void resume() {
        this.suspended = false;
        this.getEventService().postEvent(EventType.COMTASKENABLEMENT_RESUME.topic(), this);
        this.post();
    }

    @Override
    public ComTask getComTask() {
        return this.comTask.isPresent() ? this.comTask.get() : null;
    }

    @Override
    public DeviceConfiguration getDeviceConfiguration() {
        return this.deviceConfiguration.get();
    }

    @Override
    public SecurityPropertySet getSecurityPropertySet() {
        return this.securityPropertySet.isPresent() ? this.securityPropertySet.get() : null;
    }

    @Override
    public void setSecurityPropertySet(SecurityPropertySet securityPropertySet) {
        this.securityPropertySet.set(securityPropertySet);
    }

    @Override
    public boolean isIgnoreNextExecutionSpecsForInbound() {
        return ignoreNextExecutionSpecsForInbound;
    }

    @Override
    public void setIgnoreNextExecutionSpecsForInbound(boolean flag) {
        this.ignoreNextExecutionSpecsForInbound = flag;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public void setPriority(int priority) {
        this.saveStrategy = this.saveStrategy.setPriority(priority);
    }

    private void doSetPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public boolean usesDefaultConnectionTask() {
        return this.usesDefaultConnectionTask;
    }

    @Override
    public void useDefaultConnectionTask(boolean flagValue) {
        this.saveStrategy = this.saveStrategy.useDefaultConnectionTask(flagValue);
    }

    private void setUsesDefaultConnectionTask(boolean flagValue) {
        this.usesDefaultConnectionTask = flagValue;
    }

    @Override
    public boolean hasPartialConnectionTask() {
        return this.partialConnectionTask.isPresent();
    }

    @Override
    public Optional<PartialConnectionTask> getPartialConnectionTask() {
        if (this.usesDefaultConnectionTask) {
            return getDeviceConfiguration().getPartialConnectionTasks().stream().filter(PartialConnectionTask::isDefault).findFirst();
        }
        return this.partialConnectionTask.getOptional();
    }

    @Override
    public void setPartialConnectionTask(PartialConnectionTask partialConnectionTask) {
        this.saveStrategy = this.saveStrategy.setPartialConnectionTask(partialConnectionTask);
    }

    private void doSetPartialConnectionTask(PartialConnectionTask partialConnectionTask) {
        this.partialConnectionTask.set(partialConnectionTask);
    }

    @Override
    protected void validateDelete() {
        this.getEventService().postEvent(EventType.COMTASKENABLEMENT_VALIDATEDELETE.topic(), this);
    }

    private interface SaveStrategy {

        void prepare();

        void complete();

        SaveStrategy setPartialConnectionTask(PartialConnectionTask partialConnectionTask);

        SaveStrategy useDefaultConnectionTask(boolean flagValue);

        SaveStrategy setPriority(int priority);

    }

    /**
     * The No-Operation strategy that will be executed when
     * no attribute that requires a strategy was updated.
     */
    private abstract class Noop implements SaveStrategy {

        @Override
        public void prepare() {
            // No implementation required
        }

        @Override
        public void complete() {
            // No implementation required
        }

        @Override
        public SaveStrategy setPartialConnectionTask(PartialConnectionTask partialConnectionTask) {
            return new ChangeConnectionStrategy(partialConnectionTask, this);
        }

        @Override
        public SaveStrategy useDefaultConnectionTask(boolean flagValue) {
            return new ChangeConnectionStrategy(flagValue, this);
        }

        @Override
        public SaveStrategy setPriority(int priority) {
            return new ChangePriorityStrategy(priority, this);
        }
    }

    /**
     * The No-Operation @ creation time strategy applies to new objects
     * and will be executed when no attribute that requires a strategy was set.
     */
    private class NoopAtCreationTime extends Noop {

    }

    /**
     * The No-Operation @ update time strategy applies to existing objects
     * and will be executed when no attribute that requires a strategy was updated.
     */
    private class NoopAtUpdateTime extends Noop {

    }

    private class ChangePriorityStrategy implements SaveStrategy {
        private SaveStrategy remainder;
        private int oldPriority;
        private int newPriority;

        private ChangePriorityStrategy(int newPriority, SaveStrategy remainder) {
            this.remainder = remainder;
            this.oldPriority = ComTaskEnablementImpl.this.getPriority();
            this.newPriority = newPriority;
        }

        @Override
        public SaveStrategy setPartialConnectionTask(PartialConnectionTask partialConnectionTask) {
            this.remainder = this.remainder.setPartialConnectionTask(partialConnectionTask);
            return this;
        }

        @Override
        public SaveStrategy useDefaultConnectionTask(boolean flagValue) {
            this.remainder = this.remainder.useDefaultConnectionTask(flagValue);
            return this;
        }

        @Override
        public SaveStrategy setPriority(int priority) {
            this.newPriority = priority;
            return this;
        }

        @Override
        public void prepare() {
            ComTaskEnablementImpl.this.doSetPriority(this.newPriority);
            this.remainder.prepare();
        }

        @Override
        public void complete() {
            this.postEvent();
            this.remainder.complete();
        }

        private void postEvent() {
            if (this.newPriority != this.oldPriority) {
                new PriorityChangeEventData(ComTaskEnablementImpl.this, this.oldPriority, this.newPriority).publish(getEventService());
            } else {
                // Same priority, no event to post
            }
        }
    }

    private class ChangeConnectionStrategy implements SaveStrategy {
        private SaveStrategy remainder;
        private boolean usedDefaultPreviously;
        private boolean useDefaultNow;
        private Optional<PartialConnectionTask> previousPartialConnectionTask;
        private Optional<PartialConnectionTask> newPartialConnectionTask;

        private ChangeConnectionStrategy(boolean useDefault, SaveStrategy remainder) {
            super();
            this.remainder = remainder;
            this.useDefaultNow = useDefault;
            this.usedDefaultPreviously = ComTaskEnablementImpl.this.usesDefaultConnectionTask();
            this.previousPartialConnectionTask = ComTaskEnablementImpl.this.partialConnectionTask.getOptional();
            this.newPartialConnectionTask = Optional.empty();
        }

        private ChangeConnectionStrategy(PartialConnectionTask partialConnectionTask, SaveStrategy remainder) {
            super();
            this.remainder = remainder;
            this.useDefaultNow = false;
            this.usedDefaultPreviously = ComTaskEnablementImpl.this.usesDefaultConnectionTask();
            this.previousPartialConnectionTask = ComTaskEnablementImpl.this.partialConnectionTask.getOptional();
            this.newPartialConnectionTask = Optional.ofNullable(partialConnectionTask);
        }

        @Override
        public SaveStrategy setPriority(int priority) {
            this.remainder = this.remainder.setPriority(priority);
            return this;
        }

        @Override
        public SaveStrategy setPartialConnectionTask(PartialConnectionTask partialConnectionTask) {
            this.useDefaultNow = false;
            this.newPartialConnectionTask = Optional.of(partialConnectionTask);
            return this;
        }

        @Override
        public SaveStrategy useDefaultConnectionTask(boolean flagValue) {
            this.useDefaultNow = flagValue;
            return this;
        }

        @Override
        public void prepare() {
            ComTaskEnablementImpl.this.setUsesDefaultConnectionTask(this.useDefaultNow);
            ComTaskEnablementImpl.this.doSetPartialConnectionTask(this.newPartialConnectionTask.orElse(null));
            this.remainder.prepare();
        }

        @Override
        public void complete() {
            this.postEvent();
            this.remainder.complete();
        }

        private void postEvent() {
            if (this.usedDefaultPreviously) {
                this.postEventWhenUsingDefaultBefore();
            } else {
                this.postEventWhenNotUsingDefaultBefore();
            }
        }

        private void postEventWhenUsingDefaultBefore() {
            if (!this.useDefaultNow) {
                // Are we using a specific task or not?
                if (this.newPartialConnectionTask.isPresent()) {
                    // Using default before but using a specific task now
                    this.postEvent(new SwitchFromDefaultConnectionToPartialConnectionTaskEventData(ComTaskEnablementImpl.this, this.newPartialConnectionTask.get()));
                } else {
                    // Simply switching off using the default
                    this.postEvent(new SwitchOffUsingDefaultConnectionEventData(ComTaskEnablementImpl.this));
                }
            } else {
                // Still using the default, no changes, no event to post
            }
        }

        private void postEventWhenNotUsingDefaultBefore() {
            if (this.useDefaultNow) {
                // Not using default before but we are now
                this.postEventWhenNotUsingDefaultBeforeButWeAreNow();
            } else {
                // Not using default before and we are not using default now
                this.postEventWhenStillNotUsingDefault();
            }
        }

        private void postEventWhenNotUsingDefaultBeforeButWeAreNow() {
            // Was there a preferred connection task before?
            if (this.previousPartialConnectionTask.isPresent()) {
                // Switching off usage of preferred connection task and using default
                this.postEvent(new SwitchFromPartialConnectionTaskToDefaultConnectionEventData(ComTaskEnablementImpl.this, this.previousPartialConnectionTask.get()));
            } else {
                // Simply switching on usage of default connection task
                this.postEvent(new SwitchOnUsingDefaultConnectionEventData(ComTaskEnablementImpl.this));
            }
        }

        private void postEventWhenStillNotUsingDefault() {
            // Must have a new PartialConnectionTask
            if (this.previousPartialConnectionTask.isPresent()) {
                if (this.newPartialConnectionTask.isPresent()) {
                    if (this.differentPartialConnectionTasks(this.previousPartialConnectionTask.get(), this.newPartialConnectionTask.get())) {
                        // switching between different preferred connection tasks
                        this.postEvent(
                                new SwitchBetweenPartialConnectionTasksEventData(
                                        ComTaskEnablementImpl.this,
                                        this.previousPartialConnectionTask.get(),
                                        this.newPartialConnectionTask.get()));
                    } else {
                        // Still using the same partial connection task, no changes, no event to post
                        return;
                    }
                } else {
                    // Switching off usage of preferred connection task and using default
                    this.postEvent(new RemovePartialConnectionTaskEventData(ComTaskEnablementImpl.this, this.previousPartialConnectionTask.get()));
                }
            } else {
                if (this.newPartialConnectionTask.isPresent()) {
                    // Starting to use preferred connection task without changes to the default flag
                    this.postEvent(new StartUsingPartialConnectionTaskEventData(ComTaskEnablementImpl.this, this.newPartialConnectionTask.get()));
                } else {
                    // Still not using the default, no changes, no event to post
                    return;
                }
            }
        }

        private boolean differentPartialConnectionTasks(PartialConnectionTask previousPartialConnectionTask, PartialConnectionTask newPartialConnectionTask) {
            return !is(previousPartialConnectionTask.getId()).equalTo(newPartialConnectionTask.getId());
        }

        private void postEvent(ConnectionStrategyChangeEventData eventData) {
            eventData.publish(getEventService());
        }

    }

    @Override
    public ComTaskEnablement cloneForDeviceConfig(DeviceConfiguration deviceConfiguration) {
        ComTaskEnablementBuilder builder = deviceConfiguration.enableComTask(getComTask(), getCorrespondingSecuritySetFromOtherDeviceConfig(deviceConfiguration), getCorrespondingProtocolDialectConfigPropertiesFromOtherDeviceConfig(deviceConfiguration));
        builder.setIgnoreNextExecutionSpecsForInbound(isIgnoreNextExecutionSpecsForInbound());
        builder.setPriority(getPriority());
        if(usesDefaultConnectionTask()){
            builder.useDefaultConnectionTask(usesDefaultConnectionTask());
        } else {
            builder.setPartialConnectionTask(getCorrespondingPartialConnectionTaskFromOtherDeviceConfig(deviceConfiguration));
        }
        return builder.add();
    }

    private PartialConnectionTask getCorrespondingPartialConnectionTaskFromOtherDeviceConfig(DeviceConfiguration deviceConfiguration) {
        PartialConnectionTask correspondingPartialConnectionTask = null;
        if(getPartialConnectionTask().isPresent()){
            correspondingPartialConnectionTask = deviceConfiguration.getPartialConnectionTasks().stream().filter(pct ->
            pct.getName().equals(getPartialConnectionTask().get().getName())).findFirst().orElse(null);
        }
        return correspondingPartialConnectionTask;
    }

    private ProtocolDialectConfigurationProperties getCorrespondingProtocolDialectConfigPropertiesFromOtherDeviceConfig(DeviceConfiguration deviceConfiguration) {
        ProtocolDialectConfigurationProperties correspondingDialectConfigProperties = null;
        if (getProtocolDialectConfigurationProperties() != null) {
            correspondingDialectConfigProperties = deviceConfiguration.getProtocolDialectConfigurationPropertiesList().stream().filter(pdcp ->
            pdcp.getDeviceProtocolDialectName().equals(getProtocolDialectConfigurationProperties().getDeviceProtocolDialectName())).findFirst().orElse(null);
        }
        return correspondingDialectConfigProperties;
    }

    private SecurityPropertySet getCorrespondingSecuritySetFromOtherDeviceConfig(DeviceConfiguration deviceConfiguration) {
        SecurityPropertySet correspondingSecurityPropertySet = null;
        if (getSecurityPropertySet() != null) {
            correspondingSecurityPropertySet = deviceConfiguration.getSecurityPropertySets().stream().filter(sps ->
                            sps.getName().equals(getSecurityPropertySet().getName())
                                    && sps.getEncryptionDeviceAccessLevel().getId() == getSecurityPropertySet().getEncryptionDeviceAccessLevel().getId()
                                    && sps.getAuthenticationDeviceAccessLevel().getId() == getSecurityPropertySet().getAuthenticationDeviceAccessLevel().getId()
            ).findFirst().orElse(null);
        }
        return correspondingSecurityPropertySet;
    }
}
