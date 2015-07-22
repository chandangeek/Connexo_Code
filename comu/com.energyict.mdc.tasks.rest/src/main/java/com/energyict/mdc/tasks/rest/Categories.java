package com.energyict.mdc.tasks.rest;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.protocol.api.tasks.TopologyAction;
import com.energyict.mdc.tasks.ClockTask;
import com.energyict.mdc.tasks.ClockTaskType;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.LogBooksTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.RegistersTask;
import com.energyict.mdc.tasks.StatusInformationTask;
import com.energyict.mdc.tasks.TopologyTask;
import com.energyict.mdc.tasks.rest.impl.ComTaskInfo;
import com.energyict.mdc.tasks.rest.impl.ParameterInfo;
import com.energyict.mdc.tasks.rest.impl.ProtocolTaskInfo;
import com.energyict.mdc.tasks.rest.impl.util.RestHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public enum Categories {

    LOGBOOKS("logbooks") {
        @Override
        public Class<? extends ProtocolTask> getProtocolTaskClass() {
            return LogBooksTask.class;
        }

        @Override
        public List<String> getActions() {
            return Arrays.asList("read");
        }

        @Override
        public int getAction(ProtocolTask protocolTask) {
            return 1;
        }

        @Override
        public List<ParameterInfo> getProtocolTaskParameters(ProtocolTask protocolTask) {
            LogBooksTask logBooksTask = (LogBooksTask) protocolTask;
            List<ParameterInfo> protocolTaskParameters = new ArrayList<>();

            ParameterInfo logBookTypesIds = new ParameterInfo(ComTaskInfo.LOGBOOK_TYPE_IDS);
            List<ParameterInfo> logBookTypesIdsValues = new ArrayList<>();
            for (LogBookType logBookType : logBooksTask.getLogBookTypes()) {
                ParameterInfo logBookTypeIdValue = new ParameterInfo(ComTaskInfo.LOGBOOK_TYPE_ID);
                logBookTypeIdValue.value = logBookType.getId();
                logBookTypesIdsValues.add(logBookTypeIdValue);
            }
            logBookTypesIds.value = logBookTypesIdsValues;
            protocolTaskParameters.add(logBookTypesIds);

            return protocolTaskParameters;
        }

        @Override
        public void createProtocolTask(MasterDataService masterDataService, ComTask comTask, ProtocolTaskInfo protocolTaskInfo) {
            comTask.createLogbooksTask().logBookTypes(getLogBooksTypes(masterDataService, protocolTaskInfo)).add();
        }

        @Override
        public void updateProtocolTask(MasterDataService masterDataService, ProtocolTask protocolTask, ProtocolTaskInfo protocolTaskInfo) {
            LogBooksTask logBooksTask = (LogBooksTask) protocolTask;
            logBooksTask.setLogBookTypes(getLogBooksTypes(masterDataService, protocolTaskInfo));
            logBooksTask.save();
        }

        private List<LogBookType> getLogBooksTypes(MasterDataService masterDataService, ProtocolTaskInfo protocolTaskInfo) {
            List<LogBookType> logBookTypes = new ArrayList<>();
            for (ParameterInfo parameterInfo : protocolTaskInfo.parameters) {
                List<ParameterInfo> parameterInfos = ParameterInfo.from((List<Map<String, Object>>) parameterInfo.value);
                for (ParameterInfo parameterInfoValue : parameterInfos) {
                    Long logBookTypeId = ((Number) parameterInfoValue.value).longValue();
                    if (masterDataService.findLogBookType(logBookTypeId).isPresent()) {
                        logBookTypes.add(masterDataService.findLogBookType(logBookTypeId).get());
                    }
                }
            }
            return logBookTypes;
        }
    },

    REGISTERS("registers") {
        @Override
        public Class<? extends ProtocolTask> getProtocolTaskClass() {
            return RegistersTask.class;
        }

        @Override
        public List<String> getActions() {
            return Arrays.asList("read");
        }

        @Override
        public int getAction(ProtocolTask protocolTask) {
            return 1;
        }

        @Override
        public List<ParameterInfo> getProtocolTaskParameters(ProtocolTask protocolTask) {
            RegistersTask registersTask = (RegistersTask) protocolTask;
            List<ParameterInfo> protocolTaskParameters = new ArrayList<>();

            ParameterInfo registersGroupsIds = new ParameterInfo(ComTaskInfo.REGISTER_GROUP_IDS);
            List<ParameterInfo> registersGroupsIdsValues = new ArrayList<>();
            for (RegisterGroup registerGroup : registersTask.getRegisterGroups()) {
                ParameterInfo registerGroupIdValue = new ParameterInfo(ComTaskInfo.REGISTER_GROUP_ID);
                registerGroupIdValue.value = registerGroup.getId();
                registersGroupsIdsValues.add(registerGroupIdValue);
            }
            registersGroupsIds.value = registersGroupsIdsValues;
            protocolTaskParameters.add(registersGroupsIds);

            return protocolTaskParameters;
        }

        @Override
        public void createProtocolTask(MasterDataService masterDataService, ComTask comTask, ProtocolTaskInfo protocolTaskInfo) {
            comTask.createRegistersTask().registerGroups(getRegisterGroups(masterDataService, protocolTaskInfo)).add();
        }

        @Override
        public void updateProtocolTask(MasterDataService masterDataService, ProtocolTask protocolTask, ProtocolTaskInfo protocolTaskInfo) {
            RegistersTask registersTask = (RegistersTask) protocolTask;
            registersTask.setRegisterGroups(getRegisterGroups(masterDataService, protocolTaskInfo));
            registersTask.save();
        }

        private List<RegisterGroup> getRegisterGroups(MasterDataService masterDataService, ProtocolTaskInfo protocolTaskInfo) {
            List<RegisterGroup> registerGroups = new ArrayList<>();
            for (ParameterInfo parameterInfo : protocolTaskInfo.parameters) {
                List<ParameterInfo> parameterInfos = ParameterInfo.from((List<Map<String, Object>>) parameterInfo.value);
                for (ParameterInfo parameterInfoValue : parameterInfos) {
                    Long registerGroupId = ((Number) parameterInfoValue.value).longValue();
                    if (masterDataService.findRegisterGroup(registerGroupId).isPresent()) {
                        registerGroups.add(masterDataService.findRegisterGroup(registerGroupId).get());
                    }
                }
            }
            return registerGroups;
        }
    },

    TOPOLOGY("topology") {
        @Override
        public Class<? extends ProtocolTask> getProtocolTaskClass() {
            return TopologyTask.class;
        }

        @Override
        public List<String> getActions() {
            return Arrays.asList("update", "verify");
        }

        @Override
        public int getAction(ProtocolTask protocolTask) {
            TopologyTask topologyTask = (TopologyTask) protocolTask;
            return topologyTask.getTopologyAction().getAction();
        }

        @Override
        public List<ParameterInfo> getProtocolTaskParameters(ProtocolTask protocolTask) {
            return new ArrayList<>(0); // TopologyTasks has no protocol tasks and no parameters
        }

        @Override
        public void createProtocolTask(MasterDataService masterDataService, ComTask comTask, ProtocolTaskInfo protocolTaskInfo) {
            comTask.createTopologyTask(TopologyAction.valueOf(protocolTaskInfo.action.toUpperCase()));
        }

        @Override
        public void updateProtocolTask(MasterDataService masterDataService, ProtocolTask protocolTask, ProtocolTaskInfo protocolTaskInfo) {
            // TopologyTasks has no protocol tasks. Only ComTask name could be changed. Do nothing
        }
    },

    LOADPROFILES("loadprofiles") {
        @Override
        public Class<? extends ProtocolTask> getProtocolTaskClass() {
            return LoadProfilesTask.class;
        }

        @Override
        public List<String> getActions() {
            return Arrays.asList("read");
        }

        @Override
        public int getAction(ProtocolTask protocolTask) {
            return 1;
        }

        @Override
        public List<ParameterInfo> getProtocolTaskParameters(ProtocolTask protocolTask) {
            LoadProfilesTask loadProfilesTask = (LoadProfilesTask) protocolTask;
            List<ParameterInfo> protocolTaskParameters = new ArrayList<>();

            ParameterInfo loadProfileTypesIds = new ParameterInfo(ComTaskInfo.LOAD_PROFILE_TYPE_IDS);
            List<ParameterInfo> loadProfileTypesIdsValues = new ArrayList<>();
            for (LoadProfileType loadProfileType : loadProfilesTask.getLoadProfileTypes()) {
                ParameterInfo loadProfileTypeIdValue = new ParameterInfo(ComTaskInfo.LOAD_PROFILE_TYPE_ID);
                loadProfileTypeIdValue.value = loadProfileType.getId();
                loadProfileTypesIdsValues.add(loadProfileTypeIdValue);
            }
            loadProfileTypesIds.value = loadProfileTypesIdsValues;
            protocolTaskParameters.add(loadProfileTypesIds);

            ParameterInfo failIfConfigurationMismatch = new ParameterInfo(ComTaskInfo.FAIL_IF_CONFIGURATION_MISMATCH);
            failIfConfigurationMismatch.value = loadProfilesTask.failIfLoadProfileConfigurationMisMatch();
            protocolTaskParameters.add(failIfConfigurationMismatch);

            ParameterInfo markIntervalsAsBadTime = new ParameterInfo(ComTaskInfo.MARK_INTERVALS_AS_BAD_TIME);
            markIntervalsAsBadTime.value = loadProfilesTask.isMarkIntervalsAsBadTime();
            protocolTaskParameters.add(markIntervalsAsBadTime);

            ParameterInfo createMeterEventsFromFlags = new ParameterInfo(ComTaskInfo.CREATE_METER_EVENTS_FROM_FLAGS);
            createMeterEventsFromFlags.value = loadProfilesTask.createMeterEventsFromStatusFlags();
            protocolTaskParameters.add(createMeterEventsFromFlags);

            ParameterInfo minClockDiffBeforeBadTime = new ParameterInfo(ComTaskInfo.MIN_CLOCK_DIFF_BEFORE_BAD_TIME);
            if (loadProfilesTask.getMinClockDiffBeforeBadTime().isPresent()) {
                ParameterInfo minClockDiffBeforeBadTimeValue = new ParameterInfo(TimeDuration.getTimeUnitDescription(loadProfilesTask.getMinClockDiffBeforeBadTime().get().getTimeUnitCode()));
                minClockDiffBeforeBadTimeValue.value = loadProfilesTask.getMinClockDiffBeforeBadTime().get().getCount();
                minClockDiffBeforeBadTime.value = minClockDiffBeforeBadTimeValue;
            }
            protocolTaskParameters.add(minClockDiffBeforeBadTime);

            return protocolTaskParameters;
        }

        @Override
        public void createProtocolTask(MasterDataService masterDataService, ComTask comTask, ProtocolTaskInfo protocolTaskInfo) {
            LoadProfilesTask.LoadProfilesTaskBuilder loadProfilesTaskBuilder = comTask.createLoadProfilesTask();
            List<LoadProfileType> loadProfileTypes = new ArrayList<>();
            for (ParameterInfo parameterInfo : protocolTaskInfo.parameters) {
                switch (parameterInfo.name) {
                    case ComTaskInfo.LOAD_PROFILE_TYPE_IDS:
                        List<ParameterInfo> parameterInfos = ParameterInfo.from((List<Map<String, Object>>) parameterInfo.value);
                        for (ParameterInfo parameterInfoValue : parameterInfos) {
                            Long loadProfileTypeId = ((Number) parameterInfoValue.value).longValue();
                            if (masterDataService.findLoadProfileType(loadProfileTypeId).isPresent()) {
                                loadProfileTypes.add(masterDataService.findLoadProfileType(loadProfileTypeId).get());
                            }
                        }
                        break;
                    case ComTaskInfo.FAIL_IF_CONFIGURATION_MISMATCH:
                        loadProfilesTaskBuilder.failIfConfigurationMisMatch((Boolean) parameterInfo.value);
                        break;
                    case ComTaskInfo.MARK_INTERVALS_AS_BAD_TIME:
                        loadProfilesTaskBuilder.markIntervalsAsBadTime((Boolean) parameterInfo.value);
                        break;
                    case ComTaskInfo.CREATE_METER_EVENTS_FROM_FLAGS:
                        loadProfilesTaskBuilder.createMeterEventsFromFlags((Boolean) parameterInfo.value);
                        break;
                    case ComTaskInfo.MIN_CLOCK_DIFF_BEFORE_BAD_TIME:
                        ParameterInfo parameterInfoValue = ParameterInfo.from((Map<String, Object>) parameterInfo.value);
                        RestHelper restHelper = new RestHelper();
                        TimeDuration timeDuration = restHelper.getTimeDuration(parameterInfoValue.name, (Integer) parameterInfoValue.value);
                        loadProfilesTaskBuilder.minClockDiffBeforeBadTime(timeDuration);
                }
            }
            loadProfilesTaskBuilder.add().setLoadProfileTypes(loadProfileTypes);
        }

        @Override
        public void updateProtocolTask(MasterDataService masterDataService, ProtocolTask protocolTask, ProtocolTaskInfo protocolTaskInfo) {
            LoadProfilesTask loadProfilesTask = (LoadProfilesTask) protocolTask;
            List<LoadProfileType> loadProfileTypes = new ArrayList<>();
            for (ParameterInfo parameterInfo : protocolTaskInfo.parameters) {
                switch (parameterInfo.name) {
                    case ComTaskInfo.LOAD_PROFILE_TYPE_IDS:
                        List<ParameterInfo> parameterInfos = ParameterInfo.from((List<Map<String, Object>>) parameterInfo.value);
                        for (ParameterInfo parameterInfoValue : parameterInfos) {
                            Long loadProfileTypeId = ((Number) parameterInfoValue.value).longValue();
                            if (masterDataService.findLoadProfileType(loadProfileTypeId).isPresent()) {
                                loadProfileTypes.add(masterDataService.findLoadProfileType(loadProfileTypeId).get());
                            }
                        }
                        loadProfilesTask.setLoadProfileTypes(loadProfileTypes);
                        break;
                    case ComTaskInfo.FAIL_IF_CONFIGURATION_MISMATCH:
                        loadProfilesTask.setFailIfConfigurationMisMatch((Boolean) parameterInfo.value);
                        break;
                    case ComTaskInfo.MARK_INTERVALS_AS_BAD_TIME:
                        loadProfilesTask.setMarkIntervalsAsBadTime((Boolean) parameterInfo.value);
                        break;
                    case ComTaskInfo.CREATE_METER_EVENTS_FROM_FLAGS:
                        loadProfilesTask.setCreateMeterEventsFromStatusFlags((Boolean) parameterInfo.value);
                        break;
                    case ComTaskInfo.MIN_CLOCK_DIFF_BEFORE_BAD_TIME:
                        if (parameterInfo.value == null){
                            loadProfilesTask.setMinClockDiffBeforeBadTime(null);
                        }else{
                            ParameterInfo parameterInfoValue = ParameterInfo.from((Map<String, Object>) parameterInfo.value);
                            RestHelper restHelper = new RestHelper();
                            TimeDuration timeDuration = restHelper.getTimeDuration(parameterInfoValue.name, (Integer) parameterInfoValue.value);
                            loadProfilesTask.setMinClockDiffBeforeBadTime(timeDuration);
                        }
                }
            }
            loadProfilesTask.save();
        }
    },

    CLOCK("clock") {
        @Override
        public Class<? extends ProtocolTask> getProtocolTaskClass() {
            return ClockTask.class;
        }

        @Override
        public List<String> getActions() {
            return Arrays.asList("set", "force", "synchronize");
        }

        @Override
        public int getAction(ProtocolTask protocolTask) {
            ClockTask clockTask = (ClockTask) protocolTask;
            return clockTask.getClockTaskType().getType();
        }

        @Override
        public List<ParameterInfo> getProtocolTaskParameters(ProtocolTask protocolTask) {
            ClockTask clockTask = (ClockTask) protocolTask;
            List<ParameterInfo> protocolTaskParameters = new ArrayList<>();

            if (!this.getActionAsStr(clockTask.getClockTaskType().getType()).equals(ComTaskInfo.CLOCK_FORCE_TYPE)) {
                ParameterInfo minClockDifference = new ParameterInfo(ComTaskInfo.MIN_CLOCK_DIFFERENCE);
                if (clockTask.getMaximumClockDifference().isPresent()) {
                    ParameterInfo minClockDifferenceValue = new ParameterInfo(TimeDuration.getTimeUnitDescription(clockTask.getMinimumClockDifference().get().getTimeUnitCode()));
                    minClockDifferenceValue.value = clockTask.getMinimumClockDifference().get().getCount();
                    minClockDifference.value = minClockDifferenceValue;
                }
                protocolTaskParameters.add(minClockDifference);

                ParameterInfo maxClockDifference = new ParameterInfo(ComTaskInfo.MAX_CLOCK_DIFFERENCE);
                if (clockTask.getMaximumClockDifference().isPresent()) {
                    ParameterInfo maxClockDifferenceValue = new ParameterInfo(TimeDuration.getTimeUnitDescription(clockTask.getMaximumClockDifference().get().getTimeUnitCode()));
                    maxClockDifferenceValue.value = clockTask.getMaximumClockDifference().get().getCount();
                    maxClockDifference.value = maxClockDifferenceValue;
                }
                protocolTaskParameters.add(maxClockDifference);

                if (this.getActionAsStr(clockTask.getClockTaskType().getType()).equals(ComTaskInfo.CLOCK_SYNCHRONIZE_TYPE)) {
                    ParameterInfo maxClockShift = new ParameterInfo(ComTaskInfo.MAX_CLOCK_SHIFT);
                    if (clockTask.getMaximumClockShift().isPresent()) {
                        ParameterInfo maxClockShiftValue = new ParameterInfo(TimeDuration.getTimeUnitDescription(clockTask.getMaximumClockShift().get().getTimeUnitCode()));
                        maxClockShiftValue.value = clockTask.getMaximumClockShift().get().getCount();
                        maxClockShift.value = maxClockShiftValue;
                    }
                    protocolTaskParameters.add(maxClockShift);
                }
            }

            return protocolTaskParameters;
        }

        @Override
        public void createProtocolTask(MasterDataService masterDataService, ComTask comTask, ProtocolTaskInfo protocolTaskInfo) {
            ClockTask.ClockTaskBuilder clockTaskBuilder = comTask.createClockTask(ClockTaskType.valueOf(protocolTaskInfo.action.toUpperCase() + "CLOCK"));
            if (!protocolTaskInfo.action.equals(ComTaskInfo.CLOCK_FORCE_TYPE)) {
                for (ParameterInfo parameterInfo : protocolTaskInfo.parameters) {
                    ParameterInfo parameterInfoValue = ParameterInfo.from((Map<String, Object>) parameterInfo.value);
                    RestHelper restHelper = new RestHelper();
                    TimeDuration timeDuration = restHelper.getTimeDuration(parameterInfoValue.name, (Integer) parameterInfoValue.value);
                    switch (parameterInfo.name) {
                        case ComTaskInfo.MIN_CLOCK_DIFFERENCE:
                            clockTaskBuilder.minimumClockDifference(timeDuration);
                            break;
                        case ComTaskInfo.MAX_CLOCK_DIFFERENCE:
                            clockTaskBuilder.maximumClockDifference(timeDuration);
                            break;
                        case ComTaskInfo.MAX_CLOCK_SHIFT:
                            if (protocolTaskInfo.action.equals(ComTaskInfo.CLOCK_SYNCHRONIZE_TYPE)) {
                                clockTaskBuilder.maximumClockShift(timeDuration);
                            }
                    }
                }
            }
            clockTaskBuilder.add();
        }

        @Override
        public void updateProtocolTask(MasterDataService masterDataService, ProtocolTask protocolTask, ProtocolTaskInfo protocolTaskInfo) {
            ClockTask clockTask = (ClockTask) protocolTask;
            if (!protocolTaskInfo.action.equals(ComTaskInfo.CLOCK_FORCE_TYPE)) {
                for (ParameterInfo parameterInfo : protocolTaskInfo.parameters) {
                    ParameterInfo parameterInfoValue = ParameterInfo.from((Map<String, Object>) parameterInfo.value);
                    RestHelper restHelper = new RestHelper();
                    TimeDuration timeDuration = restHelper.getTimeDuration(parameterInfoValue.name, (Integer) parameterInfoValue.value);
                    switch (parameterInfo.name) {
                        case ComTaskInfo.MIN_CLOCK_DIFFERENCE:
                            clockTask.setMinimumClockDifference(timeDuration);
                            break;
                        case ComTaskInfo.MAX_CLOCK_DIFFERENCE:
                            clockTask.setMaximumClockDifference(timeDuration);
                            break;
                        case ComTaskInfo.MAX_CLOCK_SHIFT:
                            if (protocolTaskInfo.action.equals(ComTaskInfo.CLOCK_SYNCHRONIZE_TYPE)) {
                                clockTask.setMaximumClockShift(timeDuration);
                            }
                    }
                }
            }
            clockTask.save();
        }
    },

    STATUSINFORMATION("statusInformation") {
        @Override
        public Class<? extends ProtocolTask> getProtocolTaskClass() {
            return StatusInformationTask.class;
        }

        @Override
        public List<String> getActions() {
            return Arrays.asList("read");
        }

        @Override
        public int getAction(ProtocolTask protocolTask) {
            return 1;
        }

        @Override
        public void createProtocolTask(MasterDataService masterDataService, ComTask comTask, ProtocolTaskInfo protocolTaskInfo) {
            comTask.createStatusInformationTask();
        }

        @Override
        public void updateProtocolTask(MasterDataService masterDataService, ProtocolTask protocolTask, ProtocolTaskInfo protocolTaskInfo) {
        }

        @Override
        public List<ParameterInfo> getProtocolTaskParameters(ProtocolTask protocolTask) {
            return new ArrayList<>(0); // xxxTasks has no protocol tasks and no parameters
        }
    };

    private String id;

    private Categories(final String id) {
        this.id = id;
    }

    public abstract Class<? extends ProtocolTask> getProtocolTaskClass();

    public abstract List<ParameterInfo> getProtocolTaskParameters(ProtocolTask protocolTask);

    public abstract int getAction(ProtocolTask protocolTask);

    public abstract List<String> getActions();

    public abstract void createProtocolTask(MasterDataService masterDataService, ComTask comTask, ProtocolTaskInfo protocolTaskInfo);

    public abstract void updateProtocolTask(MasterDataService masterDataService, ProtocolTask protocolTask, ProtocolTaskInfo protocolTaskInfo);

    public String getActionAsStr(int id) {
        return getActions().get(id - 1);
    }

    public String getId() {
        return id;
    }
}