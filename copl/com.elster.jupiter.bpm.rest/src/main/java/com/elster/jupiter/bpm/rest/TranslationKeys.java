/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest;


import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {

    TASK_ASSIGNEE_ME ("TaskAssigneeMe", "Me"),
    TASK_ASSIGNEE_UNASSIGNED ("TaskAssigneeUnassigned", "Unassigned"),
    USER_TASK("HumanTaskNode", "User task"),
    ACTION_NODE("ActionNode", "Action"),
    ASSIGNMENT_NODE("Assignment", "Assignment"),
    ASYNC_EVENT_NODE("AsyncEventNode", "Async event"),
    BOUNDRY_EVENT_NODE("BoundaryEventNode", "Boundary event"),
    CATCH_LINK_NODE("CatchLinkNode", "Catch link"),
    COMPOSITE_CONTEXT_NODE("CompositeContextNode", "Composite context"),
    COMPOSITE_NODE("CompositeNode", "Composite"),
    CONSTRAINABLE_NODE("Constrainable", "Constrainable"),
    CONSTRAINT_TRIGGER_NODE("ConstraintTrigger", "Constraint trigger"),
    DATA_ASSOCIATION_NODE("DataAssociation", "Data association"),
    DYNAMIC_NODE("DynamicNode", "Dynamic"),
    END_NODE("EndNode", "End"),
    EVENT_NODE("EventNode", "Event"),
    EVENT_SUBPROCESS_NODE("EventSubProcessNode", "Event sub process"),
    EVENT_TRIGGER_NODE("EventTrigger", "Event trigger"),
    FAULT_NODE("FaultNode", "Fault"),
    FOR_EACH_NODE("ForEachNode", "For each"),
    JOIN_NODE("Join", "Join"),
    MILESTONE_NODE("MilestoneNode", "Milestone"),
    RULE_SET_NODE("RuleSetNode", "Rule set"),
    SPLIT_NODE("Split", "Split"),
    START_NODE("StartNode", "Start"),
    START_BASED_NODE("StateBasedNode", "State based"),
    STATE_NODE("StateNode", "State"),
    SUBPROCESS_NODE("SubProcessNode", "Subprocess"),
    THROW_LINK_NODE("ThrowLinkNode", "Throw link"),
    TIMER_NODE("TimerNode", "Timer"),
    TRANSFORMATION_NODE("Transformation", "Transformation"),
    TRIGGER_NODE("Trigger", "Trigger"),
    WORK_ITEM_NODE("WorkItemNode", "Work item"),
    BPM_ASSIGNEE_UNASSIGNED ("BPMAssigneeUnassigned", "Unassigned")
    ;

    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    public static TranslationKeys from(String key) {
        if (key != null) {
            for (TranslationKeys translationKey : TranslationKeys.values()) {
                if (translationKey.getKey().equals(key)) {
                    return translationKey;
                }
            }
        }
        return null;
    }

}