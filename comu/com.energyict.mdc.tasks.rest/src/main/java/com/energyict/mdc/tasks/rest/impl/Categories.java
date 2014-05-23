package com.energyict.mdc.tasks.rest.impl;

import com.energyict.mdc.common.TimeDuration;
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
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tasks.TopologyTask;
import com.energyict.mdc.tasks.rest.impl.infos.ComTaskInfo;
import com.energyict.mdc.tasks.rest.impl.infos.ParameterInfo;
import com.energyict.mdc.tasks.rest.impl.infos.ProtocolTaskInfo;
import com.energyict.mdc.tasks.rest.util.RestHelper;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
                logBookTypeIdValue.setValue(logBookType.getId());
                logBookTypesIdsValues.add(logBookTypeIdValue);
            }
            logBookTypesIds.setValue(logBookTypesIdsValues);
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
            for (ParameterInfo parameterInfo : protocolTaskInfo.getParameters()) {
                List<ParameterInfo> parameterInfos = ParameterInfo.from((List<Map<String, Object>>) parameterInfo.getValue());
                for (ParameterInfo parameterInfoValue : parameterInfos) {
                    Long logBookTypeId = ((Number) parameterInfoValue.getValue()).longValue();
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
                registerGroupIdValue.setValue(registerGroup.getId());
                registersGroupsIdsValues.add(registerGroupIdValue);
            }
            registersGroupsIds.setValue(registersGroupsIdsValues);
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
            for (ParameterInfo parameterInfo : protocolTaskInfo.getParameters()) {
                List<ParameterInfo> parameterInfos = ParameterInfo.from((List<Map<String, Object>>) parameterInfo.getValue());
                for (ParameterInfo parameterInfoValue : parameterInfos) {
                    Long registerGroupId = ((Number) parameterInfoValue.getValue()).longValue();
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
            comTask.createTopologyTask(TopologyAction.valueOf(protocolTaskInfo.getAction().toUpperCase()));
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
                loadProfileTypeIdValue.setValue(loadProfileType.getId());
                loadProfileTypesIdsValues.add(loadProfileTypeIdValue);
            }
            loadProfileTypesIds.setValue(loadProfileTypesIdsValues);
            protocolTaskParameters.add(loadProfileTypesIds);

            ParameterInfo failIfConfigurationMismatch = new ParameterInfo(ComTaskInfo.FAIL_IF_CONFIGURATION_MISMATCH);
            failIfConfigurationMismatch.setValue(loadProfilesTask.failIfLoadProfileConfigurationMisMatch());
            protocolTaskParameters.add(failIfConfigurationMismatch);

            ParameterInfo markIntervalsAsBadTime = new ParameterInfo(ComTaskInfo.MARK_INTERVALS_AS_BAD_TIME);
            markIntervalsAsBadTime.setValue(loadProfilesTask.isMarkIntervalsAsBadTime());
            protocolTaskParameters.add(markIntervalsAsBadTime);

            ParameterInfo createMeterEventsFromFlags = new ParameterInfo(ComTaskInfo.CREATE_METER_EVENTS_FROM_FLAGS);
            createMeterEventsFromFlags.setValue(loadProfilesTask.createMeterEventsFromStatusFlags());
            protocolTaskParameters.add(createMeterEventsFromFlags);

            ParameterInfo minClockDiffBeforeBadTime = new ParameterInfo(ComTaskInfo.MIN_CLOCK_DIFF_BEFORE_BAD_TIME);
            ParameterInfo minClockDiffBeforeBadTimeValue = new ParameterInfo(TimeDuration.getTimeUnitDescription(loadProfilesTask.getMinClockDiffBeforeBadTime().getTimeUnitCode()));
            minClockDiffBeforeBadTimeValue.setValue(loadProfilesTask.getMinClockDiffBeforeBadTime().getCount());
            minClockDiffBeforeBadTime.setValue(minClockDiffBeforeBadTimeValue);
            protocolTaskParameters.add(minClockDiffBeforeBadTime);

            return protocolTaskParameters;
        }

        @Override
        public void createProtocolTask(MasterDataService masterDataService, ComTask comTask, ProtocolTaskInfo protocolTaskInfo) {
            LoadProfilesTask.LoadProfilesTaskBuilder loadProfilesTaskBuilder = comTask.createLoadProfilesTask();
            List<LoadProfileType> loadProfileTypes = new ArrayList<>();
            for (ParameterInfo parameterInfo : protocolTaskInfo.getParameters()) {
                switch (parameterInfo.getName()) {
                    case ComTaskInfo.LOAD_PROFILE_TYPE_IDS:
                        List<ParameterInfo> parameterInfos = ParameterInfo.from((List<Map<String, Object>>) parameterInfo.getValue());
                        for (ParameterInfo parameterInfoValue : parameterInfos) {
                            Long loadProfileTypeId = ((Number) parameterInfoValue.getValue()).longValue();
                            if (masterDataService.findLoadProfileType(loadProfileTypeId).isPresent()) {
                                loadProfileTypes.add(masterDataService.findLoadProfileType(loadProfileTypeId).get());
                            }
                        }
                        break;
                    case ComTaskInfo.FAIL_IF_CONFIGURATION_MISMATCH:
                        loadProfilesTaskBuilder.failIfConfigurationMisMatch((Boolean) parameterInfo.getValue());
                        break;
                    case ComTaskInfo.MARK_INTERVALS_AS_BAD_TIME:
                        loadProfilesTaskBuilder.markIntervalsAsBadTime((Boolean) parameterInfo.getValue());
                        break;
                    case ComTaskInfo.CREATE_METER_EVENTS_FROM_FLAGS:
                        loadProfilesTaskBuilder.createMeterEventsFromFlags((Boolean) parameterInfo.getValue());
                        break;
                    case ComTaskInfo.MIN_CLOCK_DIFF_BEFORE_BAD_TIME:
                        ParameterInfo parameterInfoValue = ParameterInfo.from((Map<String, Object>) parameterInfo.getValue());
                        TimeDuration timeDuration = RestHelper.getTimeDuration(parameterInfoValue.getName(), (Integer) parameterInfoValue.getValue());
                        loadProfilesTaskBuilder.minClockDiffBeforeBadTime(timeDuration);
                }
            }
            loadProfilesTaskBuilder.add().setLoadProfileTypes(loadProfileTypes);
        }

        @Override
        public void updateProtocolTask(MasterDataService masterDataService, ProtocolTask protocolTask, ProtocolTaskInfo protocolTaskInfo) {
            LoadProfilesTask loadProfilesTask = (LoadProfilesTask) protocolTask;
            List<LoadProfileType> loadProfileTypes = new ArrayList<>();
            for (ParameterInfo parameterInfo : protocolTaskInfo.getParameters()) {
                switch (parameterInfo.getName()) {
                    case ComTaskInfo.LOAD_PROFILE_TYPE_IDS:
                        List<ParameterInfo> parameterInfos = ParameterInfo.from((List<Map<String, Object>>) parameterInfo.getValue());
                        for (ParameterInfo parameterInfoValue : parameterInfos) {
                            Long loadProfileTypeId = ((Number) parameterInfoValue.getValue()).longValue();
                            if (masterDataService.findLoadProfileType(loadProfileTypeId).isPresent()) {
                                loadProfileTypes.add(masterDataService.findLoadProfileType(loadProfileTypeId).get());
                            }
                        }
                        loadProfilesTask.setLoadProfileTypes(loadProfileTypes);
                        break;
                    case ComTaskInfo.FAIL_IF_CONFIGURATION_MISMATCH:
                        loadProfilesTask.setFailIfConfigurationMisMatch((Boolean) parameterInfo.getValue());
                        break;
                    case ComTaskInfo.MARK_INTERVALS_AS_BAD_TIME:
                        loadProfilesTask.setMarkIntervalsAsBadTime((Boolean) parameterInfo.getValue());
                        break;
                    case ComTaskInfo.CREATE_METER_EVENTS_FROM_FLAGS:
                        loadProfilesTask.setCreateMeterEventsFromStatusFlags((Boolean) parameterInfo.getValue());
                        break;
                    case ComTaskInfo.MIN_CLOCK_DIFF_BEFORE_BAD_TIME:
                        ParameterInfo parameterInfoValue = ParameterInfo.from((Map<String, Object>) parameterInfo.getValue());
                        TimeDuration timeDuration = RestHelper.getTimeDuration(parameterInfoValue.getName(), (Integer) parameterInfoValue.getValue());
                        loadProfilesTask.setMinClockDiffBeforeBadTime(timeDuration);
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
                ParameterInfo minClockDifferenceValue = new ParameterInfo(TimeDuration.getTimeUnitDescription(clockTask.getMinimumClockDifference().getTimeUnitCode()));
                minClockDifferenceValue.setValue(clockTask.getMinimumClockDifference().getCount());
                minClockDifference.setValue(minClockDifferenceValue);
                protocolTaskParameters.add(minClockDifference);

                ParameterInfo maxClockDifference = new ParameterInfo(ComTaskInfo.MAX_CLOCK_DIFFERENCE);
                ParameterInfo maxClockDifferenceValue = new ParameterInfo(TimeDuration.getTimeUnitDescription(clockTask.getMaximumClockDifference().getTimeUnitCode()));
                maxClockDifferenceValue.setValue(clockTask.getMaximumClockDifference().getCount());
                maxClockDifference.setValue(maxClockDifferenceValue);
                protocolTaskParameters.add(maxClockDifference);

                if (this.getActionAsStr(clockTask.getClockTaskType().getType()).equals(ComTaskInfo.CLOCK_SYNCHRONIZE_TYPE)) {
                    ParameterInfo maxClockShift = new ParameterInfo(ComTaskInfo.MAX_CLOCK_SHIFT);
                    ParameterInfo maxClockShiftValue = new ParameterInfo(TimeDuration.getTimeUnitDescription(clockTask.getMaximumClockShift().getTimeUnitCode()));
                    maxClockShiftValue.setValue(clockTask.getMaximumClockShift().getCount());
                    maxClockShift.setValue(maxClockShiftValue);
                    protocolTaskParameters.add(maxClockShift);
                }
            }

            return protocolTaskParameters;
        }

        @Override
        public void createProtocolTask(MasterDataService masterDataService, ComTask comTask, ProtocolTaskInfo protocolTaskInfo) {
            ClockTask.ClockTaskBuilder clockTaskBuilder = comTask.createClockTask(ClockTaskType.valueOf(protocolTaskInfo.getAction().toUpperCase() + "CLOCK"));
            if (!protocolTaskInfo.getAction().equals(ComTaskInfo.CLOCK_FORCE_TYPE)) {
                for (ParameterInfo parameterInfo : protocolTaskInfo.getParameters()) {
                    ParameterInfo parameterInfoValue = ParameterInfo.from((Map<String, Object>) parameterInfo.getValue());
                    TimeDuration timeDuration = RestHelper.getTimeDuration(parameterInfoValue.getName(), (Integer) parameterInfoValue.getValue());
                    switch (parameterInfo.getName()) {
                        case ComTaskInfo.MIN_CLOCK_DIFFERENCE:
                            clockTaskBuilder.minimumClockDifference(timeDuration);
                            break;
                        case ComTaskInfo.MAX_CLOCK_DIFFERENCE:
                            clockTaskBuilder.maximumClockDifference(timeDuration);
                            break;
                        case ComTaskInfo.MAX_CLOCK_SHIFT:
                            if (protocolTaskInfo.getAction().equals(ComTaskInfo.CLOCK_SYNCHRONIZE_TYPE)) {
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
            if (!protocolTaskInfo.getAction().equals(ComTaskInfo.CLOCK_FORCE_TYPE)) {
                for (ParameterInfo parameterInfo : protocolTaskInfo.getParameters()) {
                    ParameterInfo parameterInfoValue = ParameterInfo.from((Map<String, Object>) parameterInfo.getValue());
                    TimeDuration timeDuration = RestHelper.getTimeDuration(parameterInfoValue.getName(), (Integer) parameterInfoValue.getValue());
                    switch (parameterInfo.getName()) {
                        case ComTaskInfo.MIN_CLOCK_DIFFERENCE:
                            clockTask.setMinimumClockDifference(timeDuration);
                            break;
                        case ComTaskInfo.MAX_CLOCK_DIFFERENCE:
                            clockTask.setMaximumClockDifference(timeDuration);
                            break;
                        case ComTaskInfo.MAX_CLOCK_SHIFT:
                            if (protocolTaskInfo.getAction().equals(ComTaskInfo.CLOCK_SYNCHRONIZE_TYPE)) {
                                clockTask.setMaximumClockShift(timeDuration);
                            }
                    }
                }
            }
            clockTask.save();
        }
    };

    private String id;

    private Categories(final String id) {
        this.id = id;
    }

    public static Response createComTask(TaskService taskService, MasterDataService masterDataService, ComTaskInfo comTaskInfo) {
        ComTask newComTask = taskService.newComTask(comTaskInfo.getName());
        for (ProtocolTaskInfo protocolTaskInfo : comTaskInfo.getCommands()) {
            Categories category = Categories.valueOf(protocolTaskInfo.getCategory().toUpperCase());
            category.createProtocolTask(masterDataService, newComTask, protocolTaskInfo);
        }
        newComTask.save();
        return Response.status(Response.Status.OK).build();
    }

    public static Response updateComTask(TaskService taskService, MasterDataService masterDataService, ComTaskInfo comTaskInfo, long id) {
        ComTask editedComTask = taskService.findComTask(id);
        if (editedComTask != null) {
            editedComTask.setName(comTaskInfo.getName());
            List<ProtocolTask> currentProtocolTasks = new ArrayList<>(editedComTask.getProtocolTasks());
            Set<Long> protocolTasksIds = new HashSet<>();

            for (ProtocolTaskInfo protocolTaskInfo : comTaskInfo.getCommands()) {
                Categories category = Categories.valueOf(protocolTaskInfo.getCategory().toUpperCase());
                if (protocolTaskInfo.getId() != null) {
                    protocolTasksIds.add(protocolTaskInfo.getId());
                    ProtocolTask protocolTask = taskService.findProtocolTask(protocolTaskInfo.getId());
                    if (protocolTask != null) {
                        category.updateProtocolTask(masterDataService, protocolTask, protocolTaskInfo);
                    }
                } else {
                    category.createProtocolTask(masterDataService, editedComTask, protocolTaskInfo);
                }
            }

            for (ProtocolTask protocolTask : currentProtocolTasks) {
                if (!protocolTasksIds.contains(protocolTask.getId()))
                    editedComTask.removeTask(protocolTask);
            }

            editedComTask.save();
            return Response.status(Response.Status.OK).build();
        }
        throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    public static Response deleteComTask(TaskService taskService, long id) {
        ComTask comTask = taskService.findComTask(id);
        if (comTask != null) {
            comTask.delete();
            return Response.status(Response.Status.OK).build();
        }
        throw new WebApplicationException(Response.Status.BAD_REQUEST);
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