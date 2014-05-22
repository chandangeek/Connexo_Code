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
import com.energyict.mdc.tasks.rest.ComTaskInfo;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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
        public String[] getActions() {
            return new String[]{"read"};
        }

        @Override
        public Map<String, Object> getProtocolTaskAsParameterEntry(ProtocolTask protocolTask) {
            LogBooksTask logBooksTask = (LogBooksTask) protocolTask;

            List<Map<String, Object>> parameters = new LinkedList<>();

            Map<String, Object> commandEntry = new LinkedHashMap<>();
            commandEntry.put(ComTaskInfo.ID, protocolTask.getId());
            commandEntry.put(ComTaskInfo.CATEGORY, this.getId());
            commandEntry.put(ComTaskInfo.ACTION, this.getActionById(1));

            Map<String, Object> parameterEntry = new LinkedHashMap<>();
            parameterEntry.put(ComTaskInfo.NAME, ComTaskInfo.LOGBOOK_TYPE_IDS);

            List<Map<String, Object>> logBookTypes = new LinkedList<>();
            Map<String, Object> logBookTypeEntry;
            for (LogBookType logBookType : logBooksTask.getLogBookTypes()) {
                logBookTypeEntry = new LinkedHashMap<>();
                logBookTypeEntry.put(ComTaskInfo.NAME, ComTaskInfo.LOGBOOK_TYPE_ID);
                logBookTypeEntry.put(ComTaskInfo.VALUE, logBookType.getId());
                logBookTypes.add(logBookTypeEntry);
            }
            parameterEntry.put(ComTaskInfo.VALUE, logBookTypes);

            parameters.add(parameterEntry);

            commandEntry.put(ComTaskInfo.PARAMETERS, parameters);

            return commandEntry;
        }

        @Override
        public void createProtocolTask(ComTask comTask, Map<String, Object> command, MasterDataService masterDataService) {
            List<LogBookType> logBookTypes = new ArrayList<>();
            List<Map<String, Object>> parameters = (List<Map<String, Object>>) command.get(ComTaskInfo.PARAMETERS);
            for (Map<String, Object> parameter : parameters) {
                List<Map<String, Object>> parameterEntryValue = (List<Map<String, Object>>) parameter.get(ComTaskInfo.VALUE);
                for (Map<String, Object> logBookTypeEntry : parameterEntryValue) {
                    if (masterDataService.findLogBookType(((Number) logBookTypeEntry.get(ComTaskInfo.VALUE)).longValue()).isPresent()) {
                        logBookTypes.add(masterDataService.findLogBookType(((Number) logBookTypeEntry.get(ComTaskInfo.VALUE)).longValue()).get());
                    }
                }
            }
            comTask.createLogbooksTask().logBookTypes(logBookTypes).add();
        }

        @Override
        public void updateProtocolTask(MasterDataService masterDataService, ProtocolTask protocolTask, Map<String, Object> command) {
            LogBooksTask logBooksTask = (LogBooksTask) protocolTask;

            Set<Long> logBookTypesIds = new HashSet<>();
            List<Map<String, Object>> parameters = (List<Map<String, Object>>) command.get(ComTaskInfo.PARAMETERS);
            for (Map<String, Object> parameter : parameters) {
                List<Map<String, Object>> parameterEntryValue = (List<Map<String, Object>>) parameter.get(ComTaskInfo.VALUE);
                for (Map<String, Object> logBookTypeEntry : parameterEntryValue) {
                    logBookTypesIds.add(((Number) logBookTypeEntry.get(ComTaskInfo.VALUE)).longValue());
                }
            }

            List<LogBookType> logBookTypes = new ArrayList<>();
            for (Long logBookTypeId : logBookTypesIds) {
                if (masterDataService.findLogBookType(logBookTypeId).isPresent()) {
                    logBookTypes.add(masterDataService.findLogBookType(logBookTypeId).get());
                }
            }

            logBooksTask.setLogBookTypes(logBookTypes);
            logBooksTask.save();
        }
    },

    REGISTERS("registers") {
        @Override
        public Class<? extends ProtocolTask> getProtocolTaskClass() {
            return RegistersTask.class;
        }

        @Override
        public String[] getActions() {
            return new String[]{"read"};
        }

        @Override
        public Map<String, Object> getProtocolTaskAsParameterEntry(ProtocolTask protocolTask) {
            RegistersTask registersTask = (RegistersTask) protocolTask;

            List<Map<String, Object>> parameters = new LinkedList<>();

            Map<String, Object> commandEntry = new LinkedHashMap<>();
            commandEntry.put(ComTaskInfo.ID, protocolTask.getId());
            commandEntry.put(ComTaskInfo.CATEGORY, this.getId());
            commandEntry.put(ComTaskInfo.ACTION, this.getActionById(1));

            Map<String, Object> parameterEntry = new LinkedHashMap<>();
            parameterEntry.put(ComTaskInfo.NAME, ComTaskInfo.REGISTER_GROUP_IDS);

            List<Map<String, Object>> registerGroups = new LinkedList<>();
            Map<String, Object> registerGroupEntry;
            for (RegisterGroup registerGroup : registersTask.getRegisterGroups()) {
                registerGroupEntry = new LinkedHashMap<>();
                registerGroupEntry.put(ComTaskInfo.NAME, ComTaskInfo.REGISTER_GROUP_ID);
                registerGroupEntry.put(ComTaskInfo.VALUE, registerGroup.getId());
                registerGroups.add(registerGroupEntry);
            }
            parameterEntry.put(ComTaskInfo.VALUE, registerGroups);

            parameters.add(parameterEntry);

            commandEntry.put(ComTaskInfo.PARAMETERS, parameters);

            return commandEntry;
        }

        @Override
        public void createProtocolTask(ComTask comTask, Map<String, Object> command, MasterDataService masterDataService) {
            List<RegisterGroup> registerGroups = new ArrayList<>();
            List<Map<String, Object>> parameters = (List<Map<String, Object>>) command.get(ComTaskInfo.PARAMETERS);
            for (Map<String, Object> parameter : parameters) {
                List<Map<String, Object>> parameterEntryValue = (List<Map<String, Object>>) parameter.get(ComTaskInfo.VALUE);
                for (Map<String, Object> registerGroupEntry : parameterEntryValue) {
                    if (masterDataService.findRegisterGroup(((Number) registerGroupEntry.get(ComTaskInfo.VALUE)).longValue()).isPresent()) {
                        registerGroups.add(masterDataService.findRegisterGroup(((Number) registerGroupEntry.get(ComTaskInfo.VALUE)).longValue()).get());
                    }
                }
            }
            comTask.createRegistersTask().registerGroups(registerGroups).add();
        }

        @Override
        public void updateProtocolTask(MasterDataService masterDataService, ProtocolTask protocolTask, Map<String, Object> command) {
            RegistersTask registersTask = (RegistersTask) protocolTask;

            Set<Long> registersGroupsIds = new HashSet<>();
            List<Map<String, Object>> parameters = (List<Map<String, Object>>) command.get(ComTaskInfo.PARAMETERS);
            for (Map<String, Object> parameter : parameters) {
                List<Map<String, Object>> parameterEntryValue = (List<Map<String, Object>>) parameter.get(ComTaskInfo.VALUE);
                for (Map<String, Object> registerGroupEntry : parameterEntryValue) {
                    registersGroupsIds.add(((Number) registerGroupEntry.get(ComTaskInfo.VALUE)).longValue());
                }
            }

            List<RegisterGroup> registerGroups = new ArrayList<>();
            for (Long registerGroupId : registersGroupsIds) {
                if (masterDataService.findRegisterGroup(registerGroupId).isPresent()) {
                    registerGroups.add(masterDataService.findRegisterGroup(registerGroupId).get());
                }
            }

            registersTask.setRegisterGroups(registerGroups);
            registersTask.save();
        }
    },

    TOPOLOGY("topology") {
        @Override
        public Class<? extends ProtocolTask> getProtocolTaskClass() {
            return TopologyTask.class;
        }

        @Override
        public String[] getActions() {
            return new String[]{"update", "verify"};
        }

        @Override
        public Map<String, Object> getProtocolTaskAsParameterEntry(ProtocolTask protocolTask) {
            TopologyTask topologyTask = (TopologyTask) protocolTask;
            Map<String, Object> commandEntry = new LinkedHashMap<>();
            commandEntry.put(ComTaskInfo.ID, protocolTask.getId());
            commandEntry.put(ComTaskInfo.CATEGORY, this.getId());
            commandEntry.put(ComTaskInfo.ACTION, this.getActionById(topologyTask.getTopologyAction().getAction()));
            commandEntry.put(ComTaskInfo.PARAMETERS, new ArrayList<>(0));
            return commandEntry;
        }

        @Override
        public void createProtocolTask(ComTask comTask, Map<String, Object> command, MasterDataService masterDataService) {
            comTask.createTopologyTask(TopologyAction.valueOf(command.get(ComTaskInfo.ACTION).toString().toUpperCase()));
        }

        @Override
        public void updateProtocolTask(MasterDataService masterDataService, ProtocolTask protocolTask, Map<String, Object> command) {
            // TopologyTasks has no protocol tasks. Do nothing
        }
    },

    LOADPROFILES("loadprofiles") {
        @Override
        public Class<? extends ProtocolTask> getProtocolTaskClass() {
            return LoadProfilesTask.class;
        }

        @Override
        public String[] getActions() {
            return new String[]{"read"};
        }

        @Override
        public Map<String, Object> getProtocolTaskAsParameterEntry(ProtocolTask protocolTask) {
            LoadProfilesTask loadProfilesTask = (LoadProfilesTask) protocolTask;

            List<Map<String, Object>> parameters = new LinkedList<>();

            Map<String, Object> commandEntry = new LinkedHashMap<>();
            commandEntry.put(ComTaskInfo.ID, protocolTask.getId());
            commandEntry.put(ComTaskInfo.CATEGORY, this.getId());
            commandEntry.put(ComTaskInfo.ACTION, this.getActionById(1));

            Map<String, Object> parameterEntry = new LinkedHashMap<>();
            parameterEntry.put(ComTaskInfo.NAME, ComTaskInfo.LOAD_PROFILE_TYPE_IDS);

            List<Map<String, Object>> loadProfilesTypes = new LinkedList<>();
            Map<String, Object> loadProfilesTypeEntry;
            for (LoadProfileType loadProfileType : loadProfilesTask.getLoadProfileTypes()) {
                loadProfilesTypeEntry = new LinkedHashMap<>();
                loadProfilesTypeEntry.put(ComTaskInfo.NAME, ComTaskInfo.LOAD_PROFILE_TYPE_ID);
                loadProfilesTypeEntry.put(ComTaskInfo.VALUE, loadProfileType.getId());
                loadProfilesTypes.add(loadProfilesTypeEntry);
            }
            parameterEntry.put(ComTaskInfo.VALUE, loadProfilesTypes);
            parameters.add(parameterEntry);

            parameterEntry = new LinkedHashMap<>();
            parameterEntry.put(ComTaskInfo.NAME, ComTaskInfo.FAIL_IF_CONFIGURATION_MISMATCH);
            parameterEntry.put(ComTaskInfo.VALUE, loadProfilesTask.failIfLoadProfileConfigurationMisMatch());
            parameters.add(parameterEntry);

            parameterEntry = new LinkedHashMap<>();
            parameterEntry.put(ComTaskInfo.NAME, ComTaskInfo.MARK_INTERVALS_AS_BAD_TIME);
            parameterEntry.put(ComTaskInfo.VALUE, loadProfilesTask.isMarkIntervalsAsBadTime());
            parameters.add(parameterEntry);

            parameterEntry = new LinkedHashMap<>();
            parameterEntry.put(ComTaskInfo.NAME, ComTaskInfo.CREATE_METER_EVENTS_FROM_FLAGS);
            parameterEntry.put(ComTaskInfo.VALUE, loadProfilesTask.createMeterEventsFromStatusFlags());
            parameters.add(parameterEntry);

            parameterEntry = new LinkedHashMap<>();
            parameterEntry.put(ComTaskInfo.NAME, ComTaskInfo.MIN_CLOCK_DIFF_BEFORE_BAD_TIME);
            Map<String, Object> minClockDiffBeforeBadTimeObj = new LinkedHashMap<>();
            minClockDiffBeforeBadTimeObj.put(ComTaskInfo.NAME, TimeDuration.getTimeUnitDescription(loadProfilesTask.getMinClockDiffBeforeBadTime().getTimeUnitCode()));
            minClockDiffBeforeBadTimeObj.put(ComTaskInfo.VALUE, loadProfilesTask.getMinClockDiffBeforeBadTime().getCount());
            parameterEntry.put(ComTaskInfo.VALUE, minClockDiffBeforeBadTimeObj);
            parameters.add(parameterEntry);

            commandEntry.put(ComTaskInfo.PARAMETERS, parameters);

            return commandEntry;
        }

        @Override
        public void createProtocolTask(ComTask comTask, Map<String, Object> command, MasterDataService masterDataService) {
            List<LoadProfileType> loadProfileTypes = new ArrayList<>();
            List<Map<String, Object>> parameters = (List<Map<String, Object>>) command.get(ComTaskInfo.PARAMETERS);
            LoadProfilesTask.LoadProfilesTaskBuilder loadProfilesTaskBuilder = comTask.createLoadProfilesTask();
            for (Map<String, Object> parameter : parameters) {
                String parameterName = parameter.get(ComTaskInfo.NAME).toString();

                switch (parameterName) {
                    case ComTaskInfo.LOAD_PROFILE_TYPE_IDS:
                        List<Map<String, Object>> parameterEntryValue = (List<Map<String, Object>>) parameter.get(ComTaskInfo.VALUE);
                        for (Map<String, Object> loadProfileTypeEntry : parameterEntryValue) {
                            if (masterDataService.findLoadProfileType(((Number) loadProfileTypeEntry.get(ComTaskInfo.VALUE)).longValue()).isPresent()) {
                                loadProfileTypes.add(masterDataService.findLoadProfileType(((Number) loadProfileTypeEntry.get(ComTaskInfo.VALUE)).longValue()).get());
                            }
                        }
                        break;

                    case ComTaskInfo.FAIL_IF_CONFIGURATION_MISMATCH:
                        loadProfilesTaskBuilder.failIfConfigurationMisMatch((Boolean) parameter.get(ComTaskInfo.VALUE));
                        break;

                    case ComTaskInfo.MARK_INTERVALS_AS_BAD_TIME:
                        loadProfilesTaskBuilder.markIntervalsAsBadTime((Boolean) parameter.get(ComTaskInfo.VALUE));
                        break;

                    case ComTaskInfo.CREATE_METER_EVENTS_FROM_FLAGS:
                        loadProfilesTaskBuilder.createMeterEventsFromFlags((Boolean) parameter.get(ComTaskInfo.VALUE));
                        break;

                    case ComTaskInfo.MIN_CLOCK_DIFF_BEFORE_BAD_TIME:
                        Map<String, Object> timeDurationObj = (Map<String, Object>) parameter.get(ComTaskInfo.VALUE);
                        String timeDurationUnits = timeDurationObj.get(ComTaskInfo.NAME).toString();
                        Integer timeDurationCount = (Integer) timeDurationObj.get(ComTaskInfo.VALUE);
                        if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.MILLISECONDS))) {
                            loadProfilesTaskBuilder.minClockDiffBeforeBadTime(TimeDuration.millis(timeDurationCount));
                        } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.SECONDS))) {
                            loadProfilesTaskBuilder.minClockDiffBeforeBadTime(TimeDuration.seconds(timeDurationCount));
                        } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.MINUTES))) {
                            loadProfilesTaskBuilder.minClockDiffBeforeBadTime(TimeDuration.minutes(timeDurationCount));
                        } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.HOURS))) {
                            loadProfilesTaskBuilder.minClockDiffBeforeBadTime(TimeDuration.hours(timeDurationCount));
                        } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.DAYS))) {
                            loadProfilesTaskBuilder.minClockDiffBeforeBadTime(TimeDuration.days(timeDurationCount));
                        } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.WEEKS))) {
                            loadProfilesTaskBuilder.minClockDiffBeforeBadTime(TimeDuration.weeks(timeDurationCount));
                        } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.MONTHS))) {
                            loadProfilesTaskBuilder.minClockDiffBeforeBadTime(TimeDuration.months(timeDurationCount));
//                        } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.YEARS))) {
//                            loadProfilesTaskBuilder.minClockDiffBeforeBadTime(TimeDuration.years((Integer) timeDurationObj.get(ComTaskInfo.VALUE)));
                        }
                }
            }
            loadProfilesTaskBuilder.add().setLoadProfileTypes(loadProfileTypes);
        }

        @Override
        public void updateProtocolTask(MasterDataService masterDataService, ProtocolTask protocolTask, Map<String, Object> command) {
            LoadProfilesTask loadProfilesTask = (LoadProfilesTask) protocolTask;

            List<LoadProfileType> loadProfileTypes = new ArrayList<>();

            List<Map<String, Object>> parameters = (List<Map<String, Object>>) command.get(ComTaskInfo.PARAMETERS);
            for (Map<String, Object> parameter : parameters) {

                String parameterName = parameter.get(ComTaskInfo.NAME).toString();

                switch (parameterName) {
                    case ComTaskInfo.LOAD_PROFILE_TYPE_IDS:
                        List<Map<String, Object>> parameterEntryValue = (List<Map<String, Object>>) parameter.get(ComTaskInfo.VALUE);
                        for (Map<String, Object> loadProfileTypeEntry : parameterEntryValue) {
                            if (masterDataService.findLoadProfileType(((Number) loadProfileTypeEntry.get(ComTaskInfo.VALUE)).longValue()).isPresent()) {
                                loadProfileTypes.add(masterDataService.findLoadProfileType(((Number) loadProfileTypeEntry.get(ComTaskInfo.VALUE)).longValue()).get());
                            }
                        }
                        loadProfilesTask.setLoadProfileTypes(loadProfileTypes);
                        break;

                    case ComTaskInfo.FAIL_IF_CONFIGURATION_MISMATCH:
                        loadProfilesTask.setFailIfConfigurationMisMatch((Boolean) parameter.get(ComTaskInfo.VALUE));
                        break;

                    case ComTaskInfo.MARK_INTERVALS_AS_BAD_TIME:
                        loadProfilesTask.setMarkIntervalsAsBadTime((Boolean) parameter.get(ComTaskInfo.VALUE));
                        break;

                    case ComTaskInfo.CREATE_METER_EVENTS_FROM_FLAGS:
                        loadProfilesTask.setCreateMeterEventsFromStatusFlags((Boolean) parameter.get(ComTaskInfo.VALUE));
                        break;

                    case ComTaskInfo.MIN_CLOCK_DIFF_BEFORE_BAD_TIME:
                        Map<String, Object> timeDurationObj = (Map<String, Object>) parameter.get(ComTaskInfo.VALUE);
                        String timeDurationUnits = timeDurationObj.get(ComTaskInfo.NAME).toString();
                        Integer timeDurationCount = (Integer) timeDurationObj.get(ComTaskInfo.VALUE);
                        if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.MILLISECONDS))) {
                            loadProfilesTask.setMinClockDiffBeforeBadTime(TimeDuration.millis(timeDurationCount));
                        } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.SECONDS))) {
                            loadProfilesTask.setMinClockDiffBeforeBadTime(TimeDuration.seconds(timeDurationCount));
                        } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.MINUTES))) {
                            loadProfilesTask.setMinClockDiffBeforeBadTime(TimeDuration.minutes(timeDurationCount));
                        } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.HOURS))) {
                            loadProfilesTask.setMinClockDiffBeforeBadTime(TimeDuration.hours(timeDurationCount));
                        } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.DAYS))) {
                            loadProfilesTask.setMinClockDiffBeforeBadTime(TimeDuration.days(timeDurationCount));
                        } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.WEEKS))) {
                            loadProfilesTask.setMinClockDiffBeforeBadTime(TimeDuration.weeks(timeDurationCount));
                        } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.MONTHS))) {
                            loadProfilesTask.setMinClockDiffBeforeBadTime(TimeDuration.months(timeDurationCount));
//                        } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.YEARS))) {
//                            loadProfilesTask.setMinClockDiffBeforeBadTime(TimeDuration.years((Integer) timeDurationObj.get(ComTaskInfo.VALUE)));
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
        public String[] getActions() {
            return new String[]{"set", "force", "synchronize"};
        }

        @Override
        public Map<String, Object> getProtocolTaskAsParameterEntry(ProtocolTask protocolTask) {
            ClockTask clockTask = (ClockTask) protocolTask;

            List<Map<String, Object>> parameters = new LinkedList<>();

            Map<String, Object> commandEntry = new LinkedHashMap<>();
            commandEntry.put(ComTaskInfo.ID, protocolTask.getId());
            commandEntry.put(ComTaskInfo.CATEGORY, this.getId());
            commandEntry.put(ComTaskInfo.ACTION, this.getActionById(clockTask.getClockTaskType().getType()));

            if (!this.getActionById(clockTask.getClockTaskType().getType()).equals(ComTaskInfo.CLOCK_FORCE_TYPE)) {
                Map<String, Object> parameterEntry = new LinkedHashMap<>();

                Map<String, Object> minClockDiffObj = new LinkedHashMap<>();
                parameterEntry.put(ComTaskInfo.NAME, ComTaskInfo.MIN_CLOCK_DIFFERENCE);
                minClockDiffObj.put(ComTaskInfo.NAME, TimeDuration.getTimeUnitDescription(clockTask.getMinimumClockDifference().getTimeUnitCode()));
                minClockDiffObj.put(ComTaskInfo.VALUE, clockTask.getMinimumClockDifference().getCount());
                parameterEntry.put(ComTaskInfo.VALUE, minClockDiffObj);
                parameters.add(parameterEntry);

                parameterEntry = new LinkedHashMap<>();
                Map<String, Object> maxClockDiffObj = new LinkedHashMap<>();
                parameterEntry.put(ComTaskInfo.NAME, ComTaskInfo.MAX_CLOCK_DIFFERENCE);
                maxClockDiffObj.put(ComTaskInfo.NAME, TimeDuration.getTimeUnitDescription(clockTask.getMaximumClockDifference().getTimeUnitCode()));
                maxClockDiffObj.put(ComTaskInfo.VALUE, clockTask.getMaximumClockDifference().getCount());
                parameterEntry.put(ComTaskInfo.VALUE, maxClockDiffObj);
                parameters.add(parameterEntry);

                if (this.getActionById(clockTask.getClockTaskType().getType()).equals(ComTaskInfo.CLOCK_SYNCHRONIZE_TYPE)) {
                    parameterEntry = new LinkedHashMap<>();
                    Map<String, Object> maxClockShiftObj = new LinkedHashMap<>();
                    parameterEntry.put(ComTaskInfo.NAME, ComTaskInfo.MAX_CLOCK_SHIFT);
                    maxClockShiftObj.put(ComTaskInfo.NAME, TimeDuration.getTimeUnitDescription(clockTask.getMaximumClockShift().getTimeUnitCode()));
                    maxClockShiftObj.put(ComTaskInfo.VALUE, clockTask.getMaximumClockShift().getCount());
                    parameterEntry.put(ComTaskInfo.VALUE, maxClockShiftObj);
                    parameters.add(parameterEntry);
                }
            }

            commandEntry.put(ComTaskInfo.PARAMETERS, parameters);

            return commandEntry;
        }

        @Override
        public void createProtocolTask(ComTask comTask, Map<String, Object> command, MasterDataService masterDataService) {
            List<Map<String, Object>> parameters = (List<Map<String, Object>>) command.get(ComTaskInfo.PARAMETERS);
            ClockTask.ClockTaskBuilder clockTaskBuilder = comTask.createClockTask(ClockTaskType.valueOf(command.get(ComTaskInfo.ACTION).toString().toUpperCase() + "CLOCK"));
            if (!command.get(ComTaskInfo.ACTION).equals(ComTaskInfo.CLOCK_FORCE_TYPE)) {
                for (Map<String, Object> parameter : parameters) {
                    String parameterName = parameter.get(ComTaskInfo.NAME).toString();
                    Map<String, Object> timeDurationObj = (Map<String, Object>) parameter.get(ComTaskInfo.VALUE);
                    String timeDurationUnits = timeDurationObj.get(ComTaskInfo.NAME).toString();
                    Integer timeDurationCount = (Integer) timeDurationObj.get(ComTaskInfo.VALUE);

                    switch (parameterName) {
                        case ComTaskInfo.MIN_CLOCK_DIFFERENCE:
                            if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.MILLISECONDS))) {
                                clockTaskBuilder.minimumClockDifference(TimeDuration.millis(timeDurationCount));
                            } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.SECONDS))) {
                                clockTaskBuilder.minimumClockDifference(TimeDuration.seconds(timeDurationCount));
                            } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.MINUTES))) {
                                clockTaskBuilder.minimumClockDifference(TimeDuration.minutes(timeDurationCount));
                            } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.HOURS))) {
                                clockTaskBuilder.minimumClockDifference(TimeDuration.hours(timeDurationCount));
                            } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.DAYS))) {
                                clockTaskBuilder.minimumClockDifference(TimeDuration.days(timeDurationCount));
                            } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.WEEKS))) {
                                clockTaskBuilder.minimumClockDifference(TimeDuration.weeks(timeDurationCount));
                            } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.MONTHS))) {
                                clockTaskBuilder.minimumClockDifference(TimeDuration.months(timeDurationCount));
//                            } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.YEARS))) {
//                                clockTaskBuilder.minimumClockDifference(TimeDuration.years(timeDurationCount));
                            }
                            break;

                        case ComTaskInfo.MAX_CLOCK_DIFFERENCE:
                            if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.MILLISECONDS))) {
                                clockTaskBuilder.maximumClockDifference(TimeDuration.millis(timeDurationCount));
                            } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.SECONDS))) {
                                clockTaskBuilder.maximumClockDifference(TimeDuration.seconds(timeDurationCount));
                            } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.MINUTES))) {
                                clockTaskBuilder.maximumClockDifference(TimeDuration.minutes(timeDurationCount));
                            } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.HOURS))) {
                                clockTaskBuilder.maximumClockDifference(TimeDuration.hours(timeDurationCount));
                            } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.DAYS))) {
                                clockTaskBuilder.maximumClockDifference(TimeDuration.days(timeDurationCount));
                            } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.WEEKS))) {
                                clockTaskBuilder.maximumClockDifference(TimeDuration.weeks(timeDurationCount));
                            } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.MONTHS))) {
                                clockTaskBuilder.maximumClockDifference(TimeDuration.months(timeDurationCount));
//                            } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.YEARS))) {
//                                clockTaskBuilder.maximumClockDifference(TimeDuration.years(timeDurationCount));
                            }
                            break;
                        case ComTaskInfo.MAX_CLOCK_SHIFT:
                            if (command.get(ComTaskInfo.ACTION).equals(ComTaskInfo.CLOCK_SYNCHRONIZE_TYPE)) {
                                if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.MILLISECONDS))) {
                                    clockTaskBuilder.maximumClockShift(TimeDuration.millis(timeDurationCount));
                                } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.SECONDS))) {
                                    clockTaskBuilder.maximumClockShift(TimeDuration.seconds(timeDurationCount));
                                } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.MINUTES))) {
                                    clockTaskBuilder.maximumClockShift(TimeDuration.minutes(timeDurationCount));
                                } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.HOURS))) {
                                    clockTaskBuilder.maximumClockShift(TimeDuration.hours(timeDurationCount));
                                } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.DAYS))) {
                                    clockTaskBuilder.maximumClockShift(TimeDuration.days(timeDurationCount));
                                } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.WEEKS))) {
                                    clockTaskBuilder.maximumClockShift(TimeDuration.weeks(timeDurationCount));
                                } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.MONTHS))) {
                                    clockTaskBuilder.maximumClockShift(TimeDuration.months(timeDurationCount));
//                                } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.YEARS))) {
//                                    clockTaskBuilder.maximumClockShift(TimeDuration.years(timeDurationCount));
                                }
                            }
                    }
                }
            }
            clockTaskBuilder.add();
        }

        @Override
        public void updateProtocolTask(MasterDataService masterDataService, ProtocolTask protocolTask, Map<String, Object> command) {
            ClockTask clockTask = (ClockTask) protocolTask;
            List<Map<String, Object>> parameters = (List<Map<String, Object>>) command.get(ComTaskInfo.PARAMETERS);
            if (!command.get(ComTaskInfo.ACTION).equals(ComTaskInfo.CLOCK_FORCE_TYPE)) {
                for (Map<String, Object> parameter : parameters) {
                    String parameterName = parameter.get(ComTaskInfo.NAME).toString();
                    Map<String, Object> timeDurationObj = (Map<String, Object>) parameter.get(ComTaskInfo.VALUE);
                    String timeDurationUnits = timeDurationObj.get(ComTaskInfo.NAME).toString();
                    Integer timeDurationCount = (Integer) timeDurationObj.get(ComTaskInfo.VALUE);

                    switch (parameterName) {
                        case ComTaskInfo.MIN_CLOCK_DIFFERENCE:
                            if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.MILLISECONDS))) {
                                clockTask.setMinimumClockDifference(TimeDuration.millis(timeDurationCount));
                            } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.SECONDS))) {
                                clockTask.setMinimumClockDifference(TimeDuration.seconds(timeDurationCount));
                            } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.MINUTES))) {
                                clockTask.setMinimumClockDifference(TimeDuration.minutes(timeDurationCount));
                            } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.HOURS))) {
                                clockTask.setMinimumClockDifference(TimeDuration.hours(timeDurationCount));
                            } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.DAYS))) {
                                clockTask.setMinimumClockDifference(TimeDuration.days(timeDurationCount));
                            } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.WEEKS))) {
                                clockTask.setMinimumClockDifference(TimeDuration.weeks(timeDurationCount));
                            } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.MONTHS))) {
                                clockTask.setMinimumClockDifference(TimeDuration.months(timeDurationCount));
//                            } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.YEARS))) {
//                                clockTask.setMinimumClockDifference(TimeDuration.years(timeDurationCount));
                            }
                            break;

                        case ComTaskInfo.MAX_CLOCK_DIFFERENCE:
                            if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.MILLISECONDS))) {
                                clockTask.setMaximumClockDifference(TimeDuration.millis(timeDurationCount));
                            } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.SECONDS))) {
                                clockTask.setMaximumClockDifference(TimeDuration.seconds(timeDurationCount));
                            } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.MINUTES))) {
                                clockTask.setMaximumClockDifference(TimeDuration.minutes(timeDurationCount));
                            } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.HOURS))) {
                                clockTask.setMaximumClockDifference(TimeDuration.hours(timeDurationCount));
                            } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.DAYS))) {
                                clockTask.setMaximumClockDifference(TimeDuration.days(timeDurationCount));
                            } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.WEEKS))) {
                                clockTask.setMaximumClockDifference(TimeDuration.weeks(timeDurationCount));
                            } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.MONTHS))) {
                                clockTask.setMaximumClockDifference(TimeDuration.months(timeDurationCount));
//                            } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.YEARS))) {
//                                clockTask.setMaximumClockDifference(TimeDuration.years(timeDurationCount));
                            }
                            break;
                        case ComTaskInfo.MAX_CLOCK_SHIFT:
                            if (command.get(ComTaskInfo.ACTION).equals(ComTaskInfo.CLOCK_SYNCHRONIZE_TYPE)) {
                                if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.MILLISECONDS))) {
                                    clockTask.setMaximumClockShift(TimeDuration.millis(timeDurationCount));
                                } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.SECONDS))) {
                                    clockTask.setMaximumClockShift(TimeDuration.seconds(timeDurationCount));
                                } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.MINUTES))) {
                                    clockTask.setMaximumClockShift(TimeDuration.minutes(timeDurationCount));
                                } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.HOURS))) {
                                    clockTask.setMaximumClockShift(TimeDuration.hours(timeDurationCount));
                                } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.DAYS))) {
                                    clockTask.setMaximumClockShift(TimeDuration.days(timeDurationCount));
                                } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.WEEKS))) {
                                    clockTask.setMaximumClockShift(TimeDuration.weeks(timeDurationCount));
                                } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.MONTHS))) {
                                    clockTask.setMaximumClockShift(TimeDuration.months(timeDurationCount));
//                                } else if (timeDurationUnits.equals(TimeDuration.getTimeUnitDescription(TimeDuration.YEARS))) {
//                                    clockTask.setMaximumClockShift(TimeDuration.years(timeDurationCount));
                                }
                            }
                    }
                }
            }

            clockTask.save();
        }
    };
    private String id;

    private Categories(String id) {
        this.id = id;
    }

    public static Response createComTask(TaskService taskService, MasterDataService masterDataService, ComTaskInfo comTaskInfo) {
        ComTask newComTask = taskService.newComTask(comTaskInfo.getName());

        for (Map<String, Object> command : comTaskInfo.getCommands()) {
            for (Categories category : Categories.values()) {
                if (category.getId().equals(command.get(ComTaskInfo.CATEGORY))) {
                    category.createProtocolTask(newComTask, command, masterDataService);
                    break;
                }
            }
        }

        newComTask.save();
        return Response.status(Response.Status.OK).build();
    }

    public static Response updateComTask(TaskService taskService, MasterDataService masterDataService, ComTaskInfo comTaskInfo, long id) {
        ComTask editedComTask = taskService.findComTask(id);

        if (editedComTask == null)
            throw new WebApplicationException(Response.Status.BAD_REQUEST);

        editedComTask.setName(comTaskInfo.getName());

        List<ProtocolTask> currentProtocolTasks = new ArrayList<>(editedComTask.getProtocolTasks());

        Set<Long> commandsIds = new HashSet<>();

        for (Map<String, Object> command : comTaskInfo.getCommands()) {
            for (Categories category : Categories.values()) {
                if (category.getId().equals(command.get(ComTaskInfo.CATEGORY))) {
                    if (command.get(ComTaskInfo.ID) != null) {
                        Long commandId = ((Number) (command.get(ComTaskInfo.ID))).longValue();
                        commandsIds.add(commandId);
                        ProtocolTask protocolTask = taskService.findProtocolTask(commandId);
                        if (protocolTask != null)
                            category.updateProtocolTask(masterDataService, protocolTask, command);
                    } else {
                        category.createProtocolTask(editedComTask, command, masterDataService);
                    }
                    break;
                }
            }
        }

        for (ProtocolTask protocolTask : currentProtocolTasks) {
            if (!commandsIds.contains(protocolTask.getId()))
                editedComTask.removeTask(protocolTask);
        }

        editedComTask.save();
        return Response.status(Response.Status.OK).build();
    }

    public static Response deleteComTask(TaskService taskService, long id) {
        ComTask comTask = taskService.findComTask(id);
        if (comTask == null)
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        comTask.delete();
        return Response.status(Response.Status.OK).build();
    }

    public abstract void createProtocolTask(ComTask comTask, Map<String, Object> command, MasterDataService masterDataService);

    public abstract void updateProtocolTask(MasterDataService masterDataService, ProtocolTask protocolTask, Map<String, Object> command);

    public abstract Map<String, Object> getProtocolTaskAsParameterEntry(ProtocolTask protocolTask);

    public abstract Class<? extends ProtocolTask> getProtocolTaskClass();

    public abstract String[] getActions();

    public String getActionById(int id) {
        return getActions()[id - 1];
    }

    public String getId() {
        return id;
    }
}