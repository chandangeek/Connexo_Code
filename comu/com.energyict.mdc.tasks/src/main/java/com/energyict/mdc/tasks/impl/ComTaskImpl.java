/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ClockTask;
import com.energyict.mdc.tasks.ClockTaskType;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.FirmwareManagementTask;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.LogBooksTask;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.RegistersTask;
import com.energyict.mdc.tasks.StatusInformationTask;
import com.energyict.mdc.tasks.TopologyTask;
import com.energyict.mdc.upl.tasks.TopologyAction;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Provider;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * An implementation for a User defined ComTask
 *
 * @author gna
 * @since 2/05/12 - 16:10
 */
@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DUPLICATE_COMTASK_NAME + "}")
abstract class ComTaskImpl implements ComTask {

    protected static final String USER_DEFINED_COMTASK = "1";
    protected static final String SYSTEM_DEFINED_COMTASK = "2";

    private final DataModel dataModel;
    private final Thesaurus thesaurus;
    private final EventService eventService;
    private final Provider<BasicCheckTaskImpl> basicCheckTaskProvider;
    private final Provider<ClockTaskImpl> clockTaskProvider;
    private final Provider<LoadProfilesTaskImpl> loadProfilesTaskProvider;
    private final Provider<LogBooksTaskImpl> logBooksTaskProvider;
    private final Provider<MessagesTaskImpl> messagesTaskProvider;
    private final Provider<RegistersTaskImpl> registersTaskProvider;
    private final Provider<StatusInformationTaskImpl> statusInformationTaskProvider;
    private final Provider<TopologyTaskImpl> topologyTaskProvider;
    private final Provider<FirmwareManagementTaskImpl> firmwareManagementTaskProvider;

    static final Map<String, Class<? extends ComTask>> IMPLEMENTERS =
            ImmutableMap.of(
                    USER_DEFINED_COMTASK, ComTaskDefinedByUserImpl.class,
                    SYSTEM_DEFINED_COMTASK, ComTaskDefinedBySystemImpl.class);

    enum Fields {
        NAME("name"),
        PROTOCOL_TASKS("protocolTasks"),
        STORE_DATE("storeData"),
        MAX_NR_OF_TRIES("maxNrOfTries"),
        SYSTEM_TASK("systemTask");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    /**
     * Holds a list of all {@link com.energyict.mdc.tasks.ProtocolTask ProtocolTasks} which must be performed during the execution of this kind of ComTask
     */
    @Valid
    private final List<ProtocolTaskImpl> protocolTasks = new ArrayList<>();
    @SuppressWarnings("unused") // Managed by ORM
    private long id;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    private String name;
    private boolean storeData; // Indication whether to store the data which is read
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;
    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;
    @SuppressWarnings("unused") // Managed by ORM
    private boolean systemTask;

    /**
     * Keeps track of the maximum number of tries a ComTask may execute before failing
     */
    @Min(value = 1, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.VALUE_TOO_SMALL + "}")
    private int maxNrOfTries = 3;

    @Inject
    ComTaskImpl(Provider<LogBooksTaskImpl> logBooksTaskProvider,
                       DataModel dataModel,
                       Provider<StatusInformationTaskImpl> statusInformationTaskProvider,
                       Provider<MessagesTaskImpl> messagesTaskProvider,
                       Provider<BasicCheckTaskImpl> basicCheckTaskProvider,
                       Provider<RegistersTaskImpl> registersTaskProvider,
                       EventService eventService,
                       Provider<ClockTaskImpl> clockTaskProvider,
                       Provider<TopologyTaskImpl> topologyTaskProvider,
                       Thesaurus thesaurus,
                       Provider<LoadProfilesTaskImpl> loadProfilesTaskProvider,
                       Provider<FirmwareManagementTaskImpl> firmwareManagementTaskProvider) {
        this.logBooksTaskProvider = logBooksTaskProvider;
        this.dataModel = dataModel;
        this.statusInformationTaskProvider = statusInformationTaskProvider;
        this.messagesTaskProvider = messagesTaskProvider;
        this.basicCheckTaskProvider = basicCheckTaskProvider;
        this.registersTaskProvider = registersTaskProvider;
        this.eventService = eventService;
        this.clockTaskProvider = clockTaskProvider;
        this.topologyTaskProvider = topologyTaskProvider;
        this.thesaurus = thesaurus;
        this.loadProfilesTaskProvider = loadProfilesTaskProvider;
        this.firmwareManagementTaskProvider = firmwareManagementTaskProvider;
    }

    protected Provider<FirmwareManagementTaskImpl> getFirmwareManagementTaskProvider() {
        return firmwareManagementTaskProvider;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name.trim();
    }

    @Override
    public boolean storeData() {
        return storeData;
    }

    @Override
    public void setStoreData(boolean storeData) {
        this.storeData = storeData;
    }

    @Override
    public long getVersion() {
        return version;
    }

    public List<ProtocolTask> getProtocolTasks() {
        return Collections.unmodifiableList(this.protocolTasks);
    }

    void addProtocolTask(ProtocolTaskImpl protocolTask) {
        verifyUniqueProtocolTaskType(protocolTask.getClass());
        Save.CREATE.validate(dataModel, protocolTask); // explicit call for validation
        ComTaskImpl.this.protocolTasks.add(protocolTask);
    }

    @Override
    public int getMaxNrOfTries() {
        return maxNrOfTries;
    }

    @Override
    public void setMaxNrOfTries(int maxNrOfTries) {
        this.maxNrOfTries = maxNrOfTries;
    }

    @Override
    public void save() {
        Save.action(getId()).save(this.dataModel, this);
    }

    void touch() {
        this.dataModel.touch(this);
    }

    @Override
    public BasicCheckTask.BasicCheckTaskBuilder createBasicCheckTask() {
        return new BasicCheckTaskBuilderImpl(this);
    }

    @Override
    public ClockTask.ClockTaskBuilder createClockTask(ClockTaskType clockTaskType) {
        return new ClockTaskBuilderImpl(this, clockTaskType);
    }

    @Override
    public LoadProfilesTask.LoadProfilesTaskBuilder createLoadProfilesTask() {
        return new LoadProfilesTaskBuilderImpl(this);
    }

    @Override
    public LogBooksTask.LogBooksTaskBuilder createLogbooksTask() {
        return new LogBooksTaskBuilderImpl(this);
    }

    @Override
    public MessagesTask.MessagesTaskBuilder createMessagesTask() {
        return new MessagesTaskBuilderImpl(this);
    }

    @Override
    public RegistersTask.RegistersTaskBuilder createRegistersTask() {
        return new RegistersTaskBuilderImpl(this);
    }

    @Override
    public StatusInformationTask createStatusInformationTask() {
        StatusInformationTaskImpl statusInformationTask = statusInformationTaskProvider.get();
        statusInformationTask.ownedBy(this);
        addProtocolTask(statusInformationTask);
        return statusInformationTask;
    }

    @Override
    public TopologyTask createTopologyTask(TopologyAction topologyAction) {
        TopologyTaskImpl topologyTask = topologyTaskProvider.get();
        topologyTask.ownedBy(this);
        topologyTask.setTopologyAction(topologyAction);
        addProtocolTask(topologyTask);
        return topologyTask;
    }

    @Override
    public FirmwareManagementTask createFirmwareManagementTask() {
        FirmwareManagementTaskImpl firmwareManagementTask = firmwareManagementTaskProvider.get();
        firmwareManagementTask.ownedBy(this);
        addProtocolTask(firmwareManagementTask);
        return firmwareManagementTask;
    }

    @Override
    public void removeTask(ProtocolTask protocolTaskToDelete) {
        protocolTasks.removeIf(task -> task.getId() == protocolTaskToDelete.getId());
    }

    @Override
    public int getMaxNumberOfTries() {
        return maxNrOfTries;
    }

    private void verifyUniqueProtocolTaskType(Class<? extends ProtocolTask> taskClass) {
        for (ProtocolTask protocolTask : this.getProtocolTasks()) {
            if (protocolTask.getClass().equals(taskClass)) {
                throw new TranslatableApplicationException(thesaurus, MessageSeeds.DUPLICATE_PROTOCOL_TASK_TYPE_IN_COMTASK);
            }
        }
    }

    @Override
    public Boolean getSystemTask() {
        return systemTask;
    }

    @Override
    public void setSystemTask(Boolean systemTask) {
        this.systemTask = systemTask;
    }

    @Override
    public void delete() {
        this.eventService.postEvent(EventType.COMTASK_VALIDATE_DELETE.topic(), this);
        this.protocolTasks.forEach(ProtocolTaskImpl::deleteDependents);
        this.protocolTasks.clear(); // delete dependents
        this.dataModel.remove(this);
        this.eventService.postEvent(EventType.COMTASK_DELETED.topic(), this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ComTaskImpl comTask = (ComTaskImpl) o;
        return id == comTask.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    class BasicCheckTaskBuilderImpl implements BasicCheckTask.BasicCheckTaskBuilder {
        BasicCheckTaskImpl basicCheckTask;

        BasicCheckTaskBuilderImpl(ComTask comTask) {
            basicCheckTask = basicCheckTaskProvider.get();
            basicCheckTask.ownedBy(comTask);
        }

        @Override
        public BasicCheckTask.BasicCheckTaskBuilder verifyClockDifference(boolean verifyClockDifference) {
            basicCheckTask.setVerifyClockDifference(verifyClockDifference);
            return this;
        }

        @Override
        public BasicCheckTask.BasicCheckTaskBuilder maximumClockDifference(TimeDuration maximumClockDifference) {
            basicCheckTask.setMaximumClockDifference(maximumClockDifference);
            return this;
        }

        @Override
        public BasicCheckTask.BasicCheckTaskBuilder verifySerialNumber(boolean verifySerialNumber) {
            basicCheckTask.setVerifySerialNumber(verifySerialNumber);
            return this;
        }

        @Override
        public BasicCheckTask add() {
            ComTaskImpl.this.addProtocolTask(basicCheckTask);
            return basicCheckTask;
        }
    }

    class ClockTaskBuilderImpl implements ClockTask.ClockTaskBuilder {
        ClockTaskImpl clockTask = clockTaskProvider.get();

        ClockTaskBuilderImpl(ComTask comTask, ClockTaskType clockTaskType) {
            clockTask.ownedBy(comTask);
            clockTask.setClockTaskType(clockTaskType);
        }

        @Override
        public ClockTask.ClockTaskBuilder minimumClockDifference(TimeDuration minimumClockDiff) {
            clockTask.setMinimumClockDifference(minimumClockDiff);
            return this;
        }

        @Override
        public ClockTask.ClockTaskBuilder maximumClockDifference(TimeDuration maximumClockDiff) {
            clockTask.setMaximumClockDifference(maximumClockDiff);
            return this;
        }

        @Override
        public ClockTask.ClockTaskBuilder maximumClockShift(TimeDuration maximumClockShift) {
            clockTask.setMaximumClockShift(maximumClockShift);
            return this;
        }

        @Override
        public ClockTask add() {
            ComTaskImpl.this.addProtocolTask(clockTask);
            return clockTask;
        }
    }

    class LoadProfilesTaskBuilderImpl implements LoadProfilesTask.LoadProfilesTaskBuilder {
        LoadProfilesTaskImpl loadProfilesTask = loadProfilesTaskProvider.get();

        LoadProfilesTaskBuilderImpl(ComTask comTask) {
            loadProfilesTask.ownedBy(comTask);
        }

        @Override
        public LoadProfilesTask.LoadProfilesTaskBuilder loadProfileTypes(List<LoadProfileType> loadProfileTypes) {
            loadProfilesTask.setLoadProfileTypes(loadProfileTypes);
            return this;
        }

        @Override
        public LoadProfilesTask.LoadProfilesTaskBuilder failIfConfigurationMisMatch(boolean failIfConfigurationMisMatch) {
            loadProfilesTask.setFailIfConfigurationMisMatch(failIfConfigurationMisMatch);
            return this;
        }

        @Override
        public LoadProfilesTask.LoadProfilesTaskBuilder markIntervalsAsBadTime(boolean markIntervalsAsBadTime) {
            loadProfilesTask.setMarkIntervalsAsBadTime(markIntervalsAsBadTime);
            return this;
        }

        @Override
        public LoadProfilesTask.LoadProfilesTaskBuilder minClockDiffBeforeBadTime(TimeDuration minClockDiffBeforeBadTime) {
            loadProfilesTask.setMinClockDiffBeforeBadTime(minClockDiffBeforeBadTime);
            return this;
        }

        @Override
        public LoadProfilesTask.LoadProfilesTaskBuilder createMeterEventsFromFlags(boolean createMeterEventsFromFlags) {
            loadProfilesTask.setCreateMeterEventsFromStatusFlags(createMeterEventsFromFlags);
            return this;
        }

        @Override
        public LoadProfilesTask add() {
            ComTaskImpl.this.addProtocolTask(loadProfilesTask);
            return loadProfilesTask;
        }
    }

    class LogBooksTaskBuilderImpl implements LogBooksTask.LogBooksTaskBuilder {
        private LogBooksTaskImpl logBooksTask = logBooksTaskProvider.get();

        LogBooksTaskBuilderImpl(ComTask comTask) {
            logBooksTask.ownedBy(comTask);
        }

        @Override
        public LogBooksTask.LogBooksTaskBuilder logBookTypes(List<LogBookType> logBookTypes) {
            logBooksTask.setLogBookTypes(logBookTypes);
            return this;
        }

        @Override
        public LogBooksTask add() {
            ComTaskImpl.this.addProtocolTask(logBooksTask);
            return logBooksTask;
        }
    }

    class MessagesTaskBuilderImpl implements MessagesTask.MessagesTaskBuilder {
        private MessagesTaskImpl messagesTask = messagesTaskProvider.get();

        MessagesTaskBuilderImpl(ComTask comTask) {
            messagesTask.ownedBy(comTask);
        }

        @Override
        public MessagesTask.MessagesTaskBuilder setMessageTaskType(MessagesTask.MessageTaskType messageTaskType) {
            messagesTask.setMessageTaskType(messageTaskType);
            return this;
        }

        @Override
        public MessagesTask.MessagesTaskBuilder deviceMessageCategories(List<DeviceMessageCategory> deviceMessageCategories) {
            messagesTask.setDeviceMessageCategories(deviceMessageCategories);
            return this;
        }

        @Override
        public MessagesTask add() {
            ComTaskImpl.this.addProtocolTask(messagesTask);
            return messagesTask;
        }
    }

    class RegistersTaskBuilderImpl implements RegistersTask.RegistersTaskBuilder {
        private RegistersTaskImpl registersTask = registersTaskProvider.get();

        RegistersTaskBuilderImpl(ComTask comTask) {
            registersTask.ownedBy(comTask);
        }

        @Override
        public RegistersTask.RegistersTaskBuilder registerGroups(Collection<RegisterGroup> registerGroups) {
            registersTask.setRegisterGroups(registerGroups);
            return this;
        }

        @Override
        public RegistersTask add() {
            ComTaskImpl.this.addProtocolTask(registersTask);
            return registersTask;
        }
    }
}
