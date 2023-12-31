/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.masterdata.LoadProfileType;
import com.energyict.mdc.common.masterdata.LogBookType;
import com.energyict.mdc.common.masterdata.RegisterGroup;
import com.energyict.mdc.common.protocol.DeviceMessageCategory;
import com.energyict.mdc.common.tasks.ClockTaskType;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskUserAction;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.upl.tasks.TopologyAction;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ComTaskBuilder extends NamedBuilder<ComTask, ComTaskBuilder> {
    private final TaskService taskService;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;

    private List<LoadProfileType> loadProfileTypes;
    private List<LogBookType> logBookTypes;
    private List<RegisterGroup> registerGroups;
    private List<TopologyAction> topologyActions;
    private List<ComTaskUserAction> comTaskUserActions;
    private List<Clock> clocks;
    private Function<DeviceMessageSpecificationService, List<DeviceMessageCategory>> commandCategoryProvider;
    private boolean statusInformationTask = false;
    private boolean basicCheckTask = false;

    @Inject
    public ComTaskBuilder(TaskService taskService, DeviceMessageSpecificationService deviceMessageSpecificationService) {
        super(ComTaskBuilder.class);
        this.taskService = taskService;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    public ComTaskBuilder withLoadProfileTypes(List<LoadProfileType> loadProfileTypes) {
        this.loadProfileTypes = loadProfileTypes;
        return this;
    }

    public ComTaskBuilder withLogBookTypes(List<LogBookType> logBookTypes) {
        this.logBookTypes = logBookTypes;
        return this;
    }

    public ComTaskBuilder withRegisterGroups(List<RegisterGroup> registerGroups) {
        this.registerGroups = registerGroups;
        return this;
    }

    public ComTaskBuilder withTopologyActions(List<TopologyAction> topologyActions) {
        this.topologyActions = topologyActions;
        return this;
    }

    public ComTaskBuilder withClocks(List<Clock> clocks) {
        this.clocks = clocks;
        return this;
    }

    public ComTaskBuilder forStatusInformationTask(boolean statusInformationTaskFlag) {
        this.statusInformationTask = statusInformationTaskFlag;
        return this;
    }

    public ComTaskBuilder forBasicCheckTask(boolean basicCheckTaskFlag) {
        this.basicCheckTask = basicCheckTaskFlag;
        return this;
    }

    public ComTaskBuilder withCommandCategoryProvider(Function<DeviceMessageSpecificationService, List<DeviceMessageCategory>> commandCategoryProvider) {
        this.commandCategoryProvider = commandCategoryProvider;
        return this;
    }

    public ComTaskBuilder withComTaskUserActions(List<ComTaskUserAction> comTaskUserActions) {
        this.comTaskUserActions = comTaskUserActions;
        return this;
    }

    @Override
    public Optional<ComTask> find() {
        return taskService.findComTasksByName(getName()).stream().findFirst();
    }

    @Override
    public ComTask create() {
        Log.write(this);
        ComTask comTask = taskService.newComTask(getName());
        if (loadProfileTypes != null) {
            comTask.createLoadProfilesTask().loadProfileTypes(loadProfileTypes).add();
        }
        if (logBookTypes != null) {
            comTask.createLogbooksTask().logBookTypes(logBookTypes).add();
        }
        if (registerGroups != null) {
            comTask.createRegistersTask().registerGroups(registerGroups).add();
        }
        if (topologyActions != null) {
            topologyActions.forEach(comTask::createTopologyTask);
        }
        if (clocks != null) {
            for (Clock clock : clocks) {
                comTask.createClockTask(clock.type).maximumClockDifference(clock.maximumClockDiff).minimumClockDifference(clock.minimumClockDiff).maximumClockShift(clock.maximumClockShift).add();
            }
        }
        if (statusInformationTask) {
            comTask.createStatusInformationTask();
        }
        if (basicCheckTask) {
            comTask.createBasicCheckTask().verifySerialNumber(true).verifyClockDifference(true).maximumClockDifference(TimeDuration.seconds(60)).add();
        }
        if (commandCategoryProvider != null) {
            comTask.createMessagesTask().deviceMessageCategories(commandCategoryProvider.apply(deviceMessageSpecificationService)).add();
        }
        if (comTaskUserActions != null) {
            comTask.setUserActions(new HashSet<>(comTaskUserActions));
        } else {
            comTask.setUserActions(new HashSet<>(Arrays.asList(ComTaskUserAction.values())));
        }
        comTask.save();
        return comTask;
    }

    public static class Clock {
        ClockTaskType type;
        TimeDuration minimumClockDiff;
        TimeDuration maximumClockDiff;
        TimeDuration maximumClockShift;

        public Clock(ClockTaskType type, TimeDuration minimumClockDiff, TimeDuration maximumClockDiff, TimeDuration maximumClockShift) {
            this.type = type;
            this.minimumClockDiff = minimumClockDiff;
            this.maximumClockDiff = maximumClockDiff;
            this.maximumClockShift = maximumClockShift;
        }
    }
}
