package com.energyict.mdc.tasks.rest.impl;

import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.ProtocolTask;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ComTaskInfo {
    public static final String ID = "id";
    public static final String CATEGORY = "category";
    public static final String ACTION = "action";
    public static final String PARAMETERS = "parameters";
    public static final String NAME = "name";
    public static final String VALUE = "value";
    public static final String LOGBOOK_TYPE_IDS = "logbooktypeids";
    public static final String LOGBOOK_TYPE_ID = "logbooktypeid";
    public static final String REGISTER_GROUP_IDS = "registergroupids";
    public static final String REGISTER_GROUP_ID = "registergroupid";
    public static final String LOAD_PROFILE_TYPE_IDS = "loadprofiletypeids";
    public static final String LOAD_PROFILE_TYPE_ID = "loadprofiletypeid";
    public static final String FAIL_IF_CONFIGURATION_MISMATCH = "failifconfigurationmismatch";
    public static final String MARK_INTERVALS_AS_BAD_TIME = "markintervalsasbadtime";
    public static final String CREATE_METER_EVENTS_FROM_FLAGS = "createmetereventsfromflags";
    public static final String MIN_CLOCK_DIFF_BEFORE_BAD_TIME = "minclockdiffbeforebadtime";
    public static final String CLOCK_FORCE_TYPE = "force";
    public static final String CLOCK_SYNCHRONIZE_TYPE = "synchronize";
    public static final String MIN_CLOCK_DIFFERENCE = "minimumclockdifference";
    public static final String MAX_CLOCK_DIFFERENCE = "maximumclockdifference";
    public static final String MAX_CLOCK_SHIFT = "maximumclockshift";
    private Long id;
    private String name;
    private boolean inUse;
    private List<Map<String, Object>> commands;

    public ComTaskInfo() {
    }

    public static ComTaskInfo from(ComTask comTask, boolean fullSpec) {
        ComTaskInfo comTaskInfo = new ComTaskInfo();
        comTaskInfo.setId(comTask.getId());
        comTaskInfo.setName(comTask.getName());
        comTaskInfo.setInUse(false);
        if (fullSpec) {
            comTaskInfo.setCommands(new LinkedList<Map<String, Object>>());
            for (ProtocolTask protocolTask : comTask.getProtocolTasks()) {
                for (Categories category : Categories.values()) {
                    if (category.getProtocolTaskClass().isAssignableFrom(protocolTask.getClass())) {
                        comTaskInfo.getCommands().add(category.getProtocolTaskAsParameterEntry(protocolTask));
                        break;
                    }
                }
            }
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

    public List<Map<String, Object>> getCommands() {
        return commands;
    }

    public void setCommands(List<Map<String, Object>> commands) {
        this.commands = commands;
    }
}