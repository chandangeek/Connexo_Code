package com.energyict.mdc.tasks.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
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
            MAX_CLOCK_SHIFT = "maximumclockshift",
            READ_CLOCK_DIFFERENCE = "readclockdifference",
            VERIFY_SERIAL_NUMBER = "verifyserialnumber";

    public Long id;
    public String name;
    public boolean inUse;
    public List<ProtocolTaskInfo> commands;
    public List<MessageCategoryInfo> messages;
    public long version;

    public static ComTaskInfo from(ComTask comTask) {
        ComTaskInfo comTaskInfo = new ComTaskInfo();
        comTaskInfo.id = comTask.getId();
        comTaskInfo.name = comTask.getName();
        comTaskInfo.inUse = false; //TODO: Real Implementation
        comTaskInfo.version = comTask.getVersion();
        return comTaskInfo;
    }

    public static List<ComTaskInfo> from(List<ComTask> comTasks) {
        List<ComTaskInfo> comTaskInfos = new ArrayList<>(comTasks.size());
        for (ComTask comTask : comTasks) {
            comTaskInfos.add(ComTaskInfo.from(comTask));
        }
        return comTaskInfos;
    }

    public static ComTaskInfo fullFrom(ComTask comTask, Thesaurus thesaurus) {
        ComTaskInfo comTaskInfo = ComTaskInfo.from(comTask);
        comTaskInfo.commands = new ArrayList<>();
        comTaskInfo.commands.addAll(ProtocolTaskInfo.from(comTask.getProtocolTasks(), thesaurus));
        comTaskInfo.messages = MessageCategoryInfo.fromTasks(comTask.getProtocolTasks());
        return comTaskInfo;
    }
}