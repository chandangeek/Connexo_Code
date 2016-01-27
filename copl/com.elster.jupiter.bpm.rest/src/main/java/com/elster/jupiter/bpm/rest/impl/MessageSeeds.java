package com.elster.jupiter.bpm.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    FIELD_CAN_NOT_BE_EMPTY(1, Constants.FIELD_CAN_NOT_BE_EMPTY, "This field is required", Level.SEVERE),
    ASSIGN_USER_EXCEPTION(2, Constants.ASSIGN_USER_EXCEPTION, "Only members of \"Administrators\" role can perform this action.", Level.SEVERE),
    NO_BPM_CONNECTION(3, Constants.NO_BPM_CONNECTION, "Connection to Flow failed.", Level.SEVERE),
    PROCESS_NOT_AVAILABLE(4, Constants.PROCESS_NOT_AVAILABLE, "Process {0} not available.", Level.SEVERE),
    USER_TASK(5, Constants.USER_TASK, "User task", Level.INFO),
    ACTION_NODE(6, Constants.ACTION_NODE, "Action", Level.INFO),
    ASSIGNMENT_NODE(7, Constants.ASSIGNMENT_NODE, "Assignment", Level.INFO),
    ASYNC_EVENT_NODE(8, Constants.ASYNC_EVENT_NODE, "Async event", Level.INFO),
    BOUNDRY_EVENT_NODE(9, Constants.BOUNDRY_EVENT_NODE, "Boundary event", Level.INFO),
    CATCH_LINK_NODE(10, Constants.CATCH_LINK_NODE, "Catch link", Level.INFO),
    COMPOSITE_CONTEXT_NODE(11, Constants.COMPOSITE_CONTEXT_NODE, "Composite context", Level.INFO),
    COMPOSITE_NODE(12, Constants.COMPOSITE_NODE, "Composite", Level.INFO),
    CONSTRAINABLE_NODE(13, Constants.CONSTRAINABLE_NODE, "Constrainable", Level.INFO),
    CONSTRAINT_TRIGGER_NODE(14, Constants.CONSTRAINT_TRIGGER_NODE, "Constraint trigger", Level.INFO),
    DATA_ASSOCIATION_NODE(15, Constants.DATA_ASSOCIATION_NODE, "Data association", Level.INFO),
    DYNAMIC_NODE(16, Constants.DYNAMIC_NODE, "Dynamic", Level.INFO),
    END_NODE(17, Constants.END_NODE, "End", Level.INFO),
    EVENT_NODE(18, Constants.EVENT_NODE, "Event", Level.INFO),
    EVENT_SUBPROCESS_NODE(19, Constants.EVENT_SUBPROCESS_NODE, "Event sub process", Level.INFO),
    EVENT_TRIGGER_NODE(20, Constants.EVENT_TRIGGER_NODE, "Event trigger", Level.INFO),
    FAULT_NODE(21, Constants.FAULT_NODE, "Fault", Level.INFO),
    FOR_EACH_NODE(22, Constants.FOR_EACH_NODE, "For each", Level.INFO),
    JOIN_NODE(23, Constants.JOIN_NODE, "Join", Level.INFO),
    MILESTONE_NODE(24, Constants.MILESTONE_NODE, "Milestone", Level.INFO),
    RULE_SET_NODE(25, Constants.RULE_SET_NODE, "Rule set", Level.INFO),
    SPLIT_NODE(26, Constants.SPLIT_NODE, "Split", Level.INFO),
    START_NODE(27, Constants.START_NODE, "Start", Level.INFO),
    START_BASED_NODE(28, Constants.START_BASED_NODE, "State based", Level.INFO),
    STATE_NODE(29, Constants.STATE_NODE, "State", Level.INFO),
    SUBPROCESS_NODE(30, Constants.SUBPROCESS_NODE, "Subprocess", Level.INFO),
    THROW_LINK_NODE(31, Constants.THROW_LINK_NODE, "Throw link", Level.INFO),
    TIMER_NODE(32, Constants.TIMER_NODE, "Timer", Level.INFO),
    TRANSFORMATION_NODE(33, Constants.TRANSFORMATION_NODE, "Transformation", Level.INFO),
    TRIGGER_NODE(34, Constants.TRIGGER_NODE, "Trigger", Level.INFO),
    WORK_ITEM_NODE(35, Constants.WORK_ITEM_NODE, "Work item", Level.INFO);

    public static final String COMPONENT_NAME = "BPM";

    private final int number;
    private final String key;
    private final String format;
    private final Level level;

    MessageSeeds(int number, String key, String format, Level level) {
        this.number = number;
        this.key = key;
        this.format = format;
        this.level = level;
    }

    @Override
    public String getModule() {
        return COMPONENT_NAME;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return format;
    }

    @Override
    public Level getLevel() {
        return level;
    }

    public enum Constants {
        ;
        public static final String NO_BPM_CONNECTION= "NoBpmConnection";
        public static final String ASSIGN_USER_EXCEPTION= "BPM.AssignUserException";
        public static final String FIELD_CAN_NOT_BE_EMPTY= "BPM.FieldCanNotBeEmpty";
        public static final String PROCESS_NOT_AVAILABLE= "BPM.ProcessNotAvailable";
        public static final String ACTION_NODE = "ActionNode";
        public static final String ASSIGNMENT_NODE = "Assignment";
        public static final String ASYNC_EVENT_NODE = "AsyncEventNode";
        public static final String BOUNDRY_EVENT_NODE = "BoundaryEventNode";
        public static final String CATCH_LINK_NODE = "CatchLinkNode";
        public static final String COMPOSITE_CONTEXT_NODE = "CompositeContextNode";
        public static final String COMPOSITE_NODE = "CompositeNode";
        public static final String CONSTRAINABLE_NODE= "Constrainable";
        public static final String CONSTRAINT_TRIGGER_NODE = "ConstraintTrigger";
        public static final String DATA_ASSOCIATION_NODE = "DataAssociation";
        public static final String DYNAMIC_NODE= "DynamicNode";
        public static final String END_NODE= "EndNode";
        public static final String EVENT_NODE= "EventNode";
        public static final String EVENT_SUBPROCESS_NODE= "EventSubProcessNode";
        public static final String EVENT_TRIGGER_NODE= "EventTrigger";
        public static final String FAULT_NODE= "FaultNode";
        public static final String FOR_EACH_NODE= "ForEachNode";
        public static final String JOIN_NODE= "Join";
        public static final String MILESTONE_NODE= "MilestoneNode";
        public static final String RULE_SET_NODE= "RuleSetNode";
        public static final String SPLIT_NODE= "Split";
        public static final String START_NODE= "StartNode";
        public static final String START_BASED_NODE= "StateBasedNode";
        public static final String STATE_NODE= "StateNode";
        public static final String SUBPROCESS_NODE= "SubProcessNode";
        public static final String THROW_LINK_NODE= "ThrowLinkNode";
        public static final String TIMER_NODE= "TimerNode";
        public static final String TRANSFORMATION_NODE= "Transformation";
        public static final String TRIGGER_NODE= "Trigger";
        public static final String WORK_ITEM_NODE= "WorkItemNode";
        public static final String USER_TASK= "HumanTaskNode";

    }

}

