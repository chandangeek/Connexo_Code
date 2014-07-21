package com.energyict.mdc.device.data.rest.impl;

import java.util.ArrayList;
import java.util.List;

public class ComSchedulesBulkInfo {
    public List<ActionInfo> actions;
    private List<ActionResultInfo> generalFails;
    private ActionInfo currentAction;

    public ComSchedulesBulkInfo() {
        actions = new ArrayList<>();
    }

    public ActionInfo nextAction(String actionTitle){
        currentAction = new ActionInfo(actionTitle);
        actions.add(currentAction);
        return currentAction;
    }

    public ComSchedulesBulkInfo success(){
        currentAction.successCount++;
        return this;
    }

    public ComSchedulesBulkInfo fail(Object id, String message){
        return fail(id, null, message);
    }

    public ComSchedulesBulkInfo fail(Object id, String title, String message){
        return fail(id, title, message, null);
    }

    public ComSchedulesBulkInfo fail(Object id, String title, String message, String messageGroup){
        currentAction.failCount++;
        currentAction.fails.add(new ActionResultInfo(id, title, message, messageGroup));
        return this;
    }

    public ComSchedulesBulkInfo generalFail(Object id, String message){
        return this.generalFail(id, message, null);
    }

    public ComSchedulesBulkInfo generalFail(Object id, String message, Object messageGroup){
        if (generalFails == null){
            generalFails = new ArrayList<>();
        }
        generalFails.add(new ActionResultInfo(id, null, message, messageGroup));
        return this;
    }

    public ComSchedulesBulkInfo build(){
        if (generalFails != null) {
            for (ActionInfo action : actions) {
                action.fails.addAll(generalFails);
                action.failCount += generalFails.size();
            }
        }
        return this;
    }

    public static class ActionInfo {
        public String actionTitle;
        public int successCount;
        public int failCount;
        public List<ActionResultInfo> success;
        public List<ActionResultInfo> fails;

        private ActionInfo() {
            fails = new ArrayList<>();
        }

        private ActionInfo(String actionTitle) {
            this();
            this.actionTitle = actionTitle;
        }
    }

    public static class ActionResultInfo {
        public Object id;
        public String title;
        public String message;
        public Object messageGroup;

        //Need for test
        private ActionResultInfo() {}

        private ActionResultInfo(Object id, String title, String message) {
            this(id, title, message, null);
        }

        public ActionResultInfo(Object id, String title, String message, Object messageGroup) {
            this.id = id;
            this.title = title;
            this.message = message;
            this.messageGroup = messageGroup;
        }
    }
}
