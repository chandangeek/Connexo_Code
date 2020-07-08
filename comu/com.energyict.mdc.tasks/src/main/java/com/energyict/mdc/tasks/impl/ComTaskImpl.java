/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.domain.util.HasNoBlacklistedCharacters;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.common.masterdata.LoadProfileType;
import com.energyict.mdc.common.masterdata.LogBookType;
import com.energyict.mdc.common.masterdata.RegisterGroup;
import com.energyict.mdc.common.protocol.DeviceMessageCategory;
import com.energyict.mdc.common.tasks.BasicCheckTask;
import com.energyict.mdc.common.tasks.ClockTask;
import com.energyict.mdc.common.tasks.ClockTaskType;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskUserAction;
import com.energyict.mdc.common.tasks.FirmwareManagementTask;
import com.energyict.mdc.common.tasks.LoadProfilesTask;
import com.energyict.mdc.common.tasks.LogBooksTask;
import com.energyict.mdc.common.tasks.MessagesTask;
import com.energyict.mdc.common.tasks.ProtocolTask;
import com.energyict.mdc.common.tasks.RegistersTask;
import com.energyict.mdc.common.tasks.StatusInformationTask;
import com.energyict.mdc.common.tasks.TaskServiceKeys;
import com.energyict.mdc.common.tasks.TopologyTask;
import com.energyict.mdc.upl.tasks.TopologyAction;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Provider;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An implementation for a User defined ComTask
 *
 * @author gna
 * @since 2/05/12 - 16:10
 */
@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + TaskServiceKeys.DUPLICATE_COMTASK_NAME + "}")
abstract class ComTaskImpl implements ComTask, PersistenceAware {

    static final class ComTaskUserActionRecord {
        private ComTaskUserAction userAction;
        private Reference<ComTask> comTask = ValueReference.absent();
        @SuppressWarnings("unused")
        private String userName;
        @SuppressWarnings("unused")
        private long version;
        @SuppressWarnings("unused")
        private Instant createTime;
        @SuppressWarnings("unused")
        private Instant modTime;

        ComTaskUserActionRecord() {

        }

        ComTaskUserActionRecord(ComTask comTask, ComTaskUserAction userAction) {
            this.comTask.set(comTask);
            this.userAction = userAction;
        }
    }

    protected static final String USER_DEFINED_COMTASK = "1";
    protected static final String SYSTEM_DEFINED_COMTASK = "2";

    private List<ComTaskUserActionRecord> comTaskUserActionRecords = new ArrayList<>();
    private Set<ComTaskUserAction> comTaskUserActions = new HashSet<>();

    private DataModel dataModel;
    private Thesaurus thesaurus;
    private EventService eventService;
    private Provider<BasicCheckTaskImpl> basicCheckTaskProvider;
    private Provider<ClockTaskImpl> clockTaskProvider;
    private Provider<LoadProfilesTaskImpl> loadProfilesTaskProvider;
    private Provider<LogBooksTaskImpl> logBooksTaskProvider;
    private Provider<MessagesTaskImpl> messagesTaskProvider;
    private Provider<RegistersTaskImpl> registersTaskProvider;
    private Provider<StatusInformationTaskImpl> statusInformationTaskProvider;
    private Provider<TopologyTaskImpl> topologyTaskProvider;
    private Provider<FirmwareManagementTaskImpl> firmwareManagementTaskProvider;

    static final Map<String, Class<? extends ComTask>> IMPLEMENTERS = ImmutableMap.of(USER_DEFINED_COMTASK,
            ComTaskDefinedByUserImpl.class, SYSTEM_DEFINED_COMTASK, ComTaskDefinedBySystemImpl.class);

    enum Fields {
        NAME("name"),
        PROTOCOL_TASKS("protocolTasks"),
        STORE_DATE("storeData"),
        MAX_NR_OF_TRIES("maxNrOfTries"),
        MANUAL_SYSTEM_TASK("manualSystemTask");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    /**
     * Holds a list of all {@link ProtocolTask ProtocolTasks} which must be performed during the execution of this kind of ComTask
     */
    @Valid
    private final List<ProtocolTaskImpl> protocolTasks = new ArrayList<>();
    @SuppressWarnings("unused") // Managed by ORM
    private long id;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + TaskServiceKeys.FIELD_TOO_LONG + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + TaskServiceKeys.CAN_NOT_BE_EMPTY + "}")
    @HasNoBlacklistedCharacters(blacklisted = {'<', '>'})
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
    private boolean manualSystemTask;

    /**
     * Keeps track of the maximum number of tries a ComTask may execute before failing
     */
    @Min(value = 1, groups = {Save.Create.class, Save.Update.class}, message = "{" + TaskServiceKeys.VALUE_TOO_SMALL + "}")
    private int maxNrOfTries = 3;

    public ComTaskImpl() {
        super();
    }

    @Inject
    ComTaskImpl(Provider<LogBooksTaskImpl> logBooksTaskProvider, DataModel dataModel,
            Provider<StatusInformationTaskImpl> statusInformationTaskProvider,
            Provider<MessagesTaskImpl> messagesTaskProvider, Provider<BasicCheckTaskImpl> basicCheckTaskProvider,
            Provider<RegistersTaskImpl> registersTaskProvider, EventService eventService,
            Provider<ClockTaskImpl> clockTaskProvider, Provider<TopologyTaskImpl> topologyTaskProvider,
            Thesaurus thesaurus, Provider<LoadProfilesTaskImpl> loadProfilesTaskProvider,
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
    @XmlAttribute
    public long getId() {
        return id;
    }

    @Override
    @XmlAttribute
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

    @Override
    @XmlElements( {
            @XmlElement(type = BasicCheckTaskImpl.class),
            @XmlElement(type = RegistersTaskImpl.class),
            @XmlElement(type = LoadProfilesTaskImpl.class),
            @XmlElement(type = LogBooksTaskImpl.class),
            @XmlElement(type = FirmwareManagementTaskImpl.class),
            @XmlElement(type = MessagesTaskImpl.class),
            @XmlElement(type = TopologyTaskImpl.class),
            @XmlElement(type = StatusInformationTaskImpl.class),
            @XmlElement(type = ClockTaskImpl.class),
            @XmlElement(type = ManualMeterReadingsTaskImpl.class),
    })
    public List<ProtocolTask> getProtocolTasks() {
        return Collections.unmodifiableList(protocolTasks);
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
    public void setUserComTask(boolean ignore){
    }

    @Override
    public void setSystemComTask(boolean ignore){
    }

    @Override
    public void setMaxNrOfTries(int maxNrOfTries) {
        this.maxNrOfTries = maxNrOfTries;
    }

    @Override
    public void save() {
        Save.action(getId()).save(dataModel, this);
    }

    void touch() {
        dataModel.touch(this);
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
    @XmlTransient
    public int getMaxNumberOfTries() {
        return maxNrOfTries;
    }

    private void verifyUniqueProtocolTaskType(Class<? extends ProtocolTask> taskClass) {
        for (ProtocolTask protocolTask : getProtocolTasks()) {
            if (protocolTask.getClass().equals(taskClass)) {
                throw new TranslatableApplicationException(thesaurus,
                        MessageSeeds.DUPLICATE_PROTOCOL_TASK_TYPE_IN_COMTASK);
            }
        }
    }

    @Override
    public void setManualSystemTask(boolean manualSystemTask) {
        this.manualSystemTask = manualSystemTask;
    }

    @Override
    public boolean isManualSystemTask() {
        return manualSystemTask;
    }


    @Override
    public void delete() {
        eventService.postEvent(EventType.COMTASK_VALIDATE_DELETE.topic(), this);
        protocolTasks.forEach(ProtocolTaskImpl::deleteDependents);
        protocolTasks.clear(); // delete dependents
        deletePrivileges();
        dataModel.remove(this);
        eventService.postEvent(EventType.COMTASK_DELETED.topic(), this);
    }

    protected void deletePrivileges() {
        comTaskUserActionRecords.clear();
        comTaskUserActions.clear();
    }

    @Override
    public void postLoad() {
        comTaskUserActions.addAll(comTaskUserActionRecords.stream().map(userActionRecord -> userActionRecord.userAction)
                .collect(Collectors.toList()));
    }

    @Override
    public Set<ComTaskUserAction> getUserActions() {
        return comTaskUserActions;
    }

    @Override
    public void setUserActions(Set<ComTaskUserAction> userActions) {
        if (isSystemComTask()) {
            // attempt to assign privileges for executing system tasks is ignored
            return;
        }
        comTaskUserActions.clear();
        comTaskUserActionRecords.clear();
        if (userActions == null) {
            return;
        }
        comTaskUserActions.addAll(userActions);
        comTaskUserActions.stream()
                .forEach(userAction -> comTaskUserActionRecords.add(new ComTaskUserActionRecord(this, userAction)));
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
            addProtocolTask(basicCheckTask);
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
            addProtocolTask(clockTask);
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
        public LoadProfilesTask.LoadProfilesTaskBuilder failIfConfigurationMisMatch(
                boolean failIfConfigurationMisMatch) {
            loadProfilesTask.setFailIfConfigurationMisMatch(failIfConfigurationMisMatch);
            return this;
        }

        @Override
        public LoadProfilesTask.LoadProfilesTaskBuilder markIntervalsAsBadTime(boolean markIntervalsAsBadTime) {
            loadProfilesTask.setMarkIntervalsAsBadTime(markIntervalsAsBadTime);
            return this;
        }

        @Override
        public LoadProfilesTask.LoadProfilesTaskBuilder minClockDiffBeforeBadTime(
                TimeDuration minClockDiffBeforeBadTime) {
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
            addProtocolTask(loadProfilesTask);
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
            addProtocolTask(logBooksTask);
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
        public MessagesTask.MessagesTaskBuilder deviceMessageCategories(
                List<DeviceMessageCategory> deviceMessageCategories) {
            messagesTask.setDeviceMessageCategories(deviceMessageCategories);
            return this;
        }

        @Override
        public MessagesTask add() {
            addProtocolTask(messagesTask);
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
            addProtocolTask(registersTask);
            return registersTask;
        }
    }
}
