package com.energyict.mdc.tasks.rest.impl.infos;

import com.energyict.mdc.tasks.ComTask;

import java.util.ArrayList;
import java.util.List;

public class ComTaskInfo {
    public static final String
            LOGBOOK_TYPE_IDS = "logbooktypeids",
            LOGBOOK_TYPE_ID = "logbooktypeid",
            REGISTER_GROUP_IDS = "registergroupids",
            REGISTER_GROUP_ID = "registergroupid",
            LOAD_PROFILE_TYPE_IDS = "loadprofiletypeids",
            LOAD_PROFILE_TYPE_ID = "loadprofiletypeid",
            FAIL_IF_CONFIGURATION_MISMATCH = "failifconfigurationmismatch",
            MARK_INTERVALS_AS_BAD_TIME = "markintervalsasbadtime",
            CREATE_METER_EVENTS_FROM_FLAGS = "createmetereventsfromflags",
            MIN_CLOCK_DIFF_BEFORE_BAD_TIME = "minclockdiffbeforebadtime",
            CLOCK_FORCE_TYPE = "force",
            CLOCK_SYNCHRONIZE_TYPE = "synchronize",
            MIN_CLOCK_DIFFERENCE = "minimumclockdifference",
            MAX_CLOCK_DIFFERENCE = "maximumclockdifference",
            MAX_CLOCK_SHIFT = "maximumclockshift";

    private Long id;
    private String name;
    private boolean inUse;
    private List<ProtocolTaskInfo> commands;

    public static ComTaskInfo from(ComTask comTask, boolean fullSpec) {
        ComTaskInfo comTaskInfo = new ComTaskInfo();
        comTaskInfo.setId(comTask.getId());
        comTaskInfo.setName(comTask.getName());
        comTaskInfo.setInUse(false); //TODO: Real Implementation
        if (fullSpec) {
            comTaskInfo.setCommands(new ArrayList<ProtocolTaskInfo>());
            comTaskInfo.getCommands().addAll(ProtocolTaskInfo.from(comTask.getProtocolTasks()));
        }
        return comTaskInfo;
    }

    public static List<ComTaskInfo> from(List<ComTask> comTasks) {
        List<ComTaskInfo> comTaskInfos = new ArrayList<>(comTasks.size());
        for (ComTask comTask : comTasks) {
            comTaskInfos.add(ComTaskInfo.from(comTask, false));
        }
        return comTaskInfos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isInUse() {
        return inUse;
    }

    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    public List<ProtocolTaskInfo> getCommands() {
        return commands;
    }

    public void setCommands(List<ProtocolTaskInfo> commands) {
        this.commands = commands;
    }
}