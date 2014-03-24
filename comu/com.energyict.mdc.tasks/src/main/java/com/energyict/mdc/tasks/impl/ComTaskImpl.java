package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.device.config.LogBookType;
import com.energyict.mdc.device.config.RegisterGroup;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.tasks.TopologyAction;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ClockTask;
import com.energyict.mdc.tasks.ClockTaskType;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.EventType;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.LogBooksTask;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.RegistersTask;
import com.energyict.mdc.tasks.StatusInformationTask;
import com.energyict.mdc.tasks.TopologyTask;
import com.google.common.collect.ImmutableList;
import com.google.inject.Provider;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

/**
 * An implementation for a {@link com.energyict.mdc.tasks.ComTask}
 *
 * @author gna
 * @since 2/05/12 - 16:10
 */
@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{"+Constants.DUPLICATE_COMTASK_NAME +"}")
public class ComTaskImpl implements ComTask, DataCollectionConfiguration {

    private static final int NAME_MAX_DB_LENGTH = 80;
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

    enum Fields {
        NAME("name"),
        PROTOCOL_TASKS("protocolTasks"),
        MAX_NR_OF_TRIES("maxNrOfTries");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    private long id;
    @Size(max = NAME_MAX_DB_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{"+Constants.SIZE_TOO_LONG +"}")
    private String name;

    /**
     * Indication whether to store the data which is read
     */
    private boolean storeData;

    /**
     * Holds a list of all {@link ProtocolTask ProtocolTasks} which must be performed during the execution of this kind of ComTask
     */
    @Size(min = 1, groups = {Save.Create.class, Save.Update.class}, message = "{"+Constants.COMTASK_WITHOUT_PROTOCOLTASK +"}")
    @Valid
    private final List<ProtocolTaskImpl> protocolTasks = new ArrayList<>();

    /**
     * Keeps track of the maximum number of tries a ComTask may execute before failing
     */
    @Min(value = 1, groups = {Save.Create.class, Save.Update.class}, message="{"+Constants.VALUE_TOO_SMALL +"}")
    private int maxNrOfTries;

    @Inject
    public ComTaskImpl(DataModel dataModel, Thesaurus thesaurus,
                       EventService eventService,
                       Provider<BasicCheckTaskImpl> basicCheckTaskProvider,
                       Provider<ClockTaskImpl> clockTaskProvider,
                       Provider<LoadProfilesTaskImpl> loadProfilesTaskProvider,
                       Provider<LogBooksTaskImpl> logBooksTaskProvider,
                       Provider<MessagesTaskImpl> messagesTaskProvider,
                       Provider<RegistersTaskImpl> registersTaskProvider,
                       Provider<StatusInformationTaskImpl> statusInformationTaskProvider,
                       Provider<TopologyTaskImpl> topologyTaskProvider) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.eventService = eventService;
        this.basicCheckTaskProvider = basicCheckTaskProvider;
        this.clockTaskProvider = clockTaskProvider;
        this.loadProfilesTaskProvider = loadProfilesTaskProvider;
        this.logBooksTaskProvider = logBooksTaskProvider;
        this.messagesTaskProvider = messagesTaskProvider;
        this.registersTaskProvider = registersTaskProvider;
        this.statusInformationTaskProvider = statusInformationTaskProvider;
        this.topologyTaskProvider = topologyTaskProvider;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean storeData() {
        return storeData;
    }

    public List<? extends ProtocolTask> getProtocolTasks() {
        return ImmutableList.copyOf(protocolTasks);
    }

    private void addProtocolTask(ProtocolTaskImpl protocolTask) {
        verifyUniqueProtocolTaskType(protocolTask.getClass());
        Save.CREATE.save(dataModel, this); // explicit call for validation
        ComTaskImpl.this.protocolTasks.add(protocolTask);

    }

    public int getMaxNrOfTries() {
        return maxNrOfTries;
    }

    public void setMaxNrOfTries(int maxNrOfTries) {
        this.maxNrOfTries = maxNrOfTries;
    }

    @Override
    public BasicCheckTask.BasicCheckTaskBuilder createBasicCheckTask() throws BusinessException, SQLException {
        return new BasicCheckTaskBuilderImpl(this);
    }

    @Override
    public ClockTask.ClockTaskBuilder createClockTask(ClockTaskType clockTaskType) throws BusinessException, SQLException {
        return new ClockTaskBuilderImpl(this, clockTaskType);
    }

    @Override
    public LoadProfilesTask.LoadProfilesTaskBuilder createLoadProfilesTask() throws BusinessException, SQLException {
        return new LoadProfilesTaskBuilderImpl(this);
    }

    @Override
    public LogBooksTask.LogBooksTaskBuilder createLogbooksTask() throws BusinessException, SQLException {
        return new LogBooksTaskBuilderImpl(this);
    }

    @Override
    public MessagesTask.MessagesTaskBuilder createMessagesTask() throws BusinessException, SQLException {
        return new MessagesTaskBuilderImpl(this);
    }

    @Override
    public RegistersTask.RegistersTaskBuilder createRegistersTask() throws BusinessException, SQLException {
        return new RegistersTaskBuilderImpl(this);
    }

    @Override
    public StatusInformationTask createStatusInformationTask() throws BusinessException, SQLException {
        StatusInformationTaskImpl statusInformationTask = statusInformationTaskProvider.get();
        statusInformationTask.ownedBy(this);
        addProtocolTask(statusInformationTask);
        return statusInformationTask;
    }

    @Override
    public TopologyTask createTopologyTask(TopologyAction topologyAction) throws BusinessException, SQLException {
        TopologyTaskImpl topologyTask = topologyTaskProvider.get();
        topologyTask.ownedBy(this);
        topologyTask.setTopologyAction(topologyAction);
        addProtocolTask(topologyTask);
        return topologyTask;
    }

    @Override
    public int getMaxNumberOfTries() {
        return this.maxNrOfTries;
    }

    @Override
    public String getType () {
        return ComTask.class.getName();
    }

    @Override
    public boolean isConfiguredToCollectRegisterData () {
        return this.isConfiguredToCollectDataOfClass(RegistersTask.class);
    }

    @Override
    public boolean isConfiguredToCollectLoadProfileData () {
        return this.isConfiguredToCollectDataOfClass(LoadProfilesTask.class);
    }

    @Override
    public boolean isConfiguredToRunBasicChecks () {
        return this.isConfiguredToCollectDataOfClass(BasicCheckTask.class);
    }

    @Override
    public boolean isConfiguredToCheckClock () {
        return this.isConfiguredToCollectDataOfClass(ClockTask.class);
    }

    @Override
    public boolean isConfiguredToCollectEvents () {
        return this.isConfiguredToCollectDataOfClass(LogBooksTask.class);
    }

    @Override
    public boolean isConfiguredToSendMessages () {
        return this.isConfiguredToCollectDataOfClass(MessagesTask.class);
    }

    @Override
    public boolean isConfiguredToReadStatusInformation () {
        return this.isConfiguredToCollectDataOfClass(StatusInformationTask.class);
    }

    @Override
    public boolean isConfiguredToUpdateTopology () {
        return this.isConfiguredToCollectDataOfClass(TopologyTask.class);
    }

    private <T extends ProtocolTask> boolean isConfiguredToCollectDataOfClass (Class<T> protocolTaskClass) {
        for (ProtocolTask protocolTask : this.getProtocolTasks()) {
            if (protocolTaskClass.isAssignableFrom(protocolTask.getClass())) {
                return true;
            }
        }
        return false;
    }

    private void verifyUniqueProtocolTaskType(Class<? extends ProtocolTask> taskClass) {
        for (ProtocolTask protocolTask : this.getProtocolTasks()) {
            if (protocolTask.getClass().equals(taskClass)) {
                throw new TranslatableApplicationException(thesaurus, MessageSeeds.DUPLICATE_PROTOCOL_TASK_TYPE_IN_COMTASK);
            }
        }
    }

    public void delete() {
        this.eventService.postEvent(EventType.COMTASK_DELETED.topic(), this);

        for (ProtocolTaskImpl protocolTask : protocolTasks) {
            protocolTask.deleteDependents();
        }
        protocolTasks.clear(); // delete dependents
        dataModel.remove(this);
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
        public MessagesTask.MessagesTaskBuilder deviceMessageCategories(List<DeviceMessageCategory> deviceMessageCategories) {
            messagesTask.setDeviceMessageCategories(deviceMessageCategories);
            return this;
        }

        @Override
        public MessagesTask.MessagesTaskBuilder deviceMessageSpecs(List<DeviceMessageSpec> deviceMessageSpecs) {
            messagesTask.setDeviceMessageSpecs(deviceMessageSpecs);
            return this;
        }

        @Override
        public MessagesTask.MessagesTaskBuilder allCategories() {
            messagesTask.setAllCategories(true);
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