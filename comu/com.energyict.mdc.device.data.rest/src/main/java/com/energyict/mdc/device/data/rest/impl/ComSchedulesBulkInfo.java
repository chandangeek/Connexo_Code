/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

    public ComSchedulesBulkInfo fail(DeviceInfo device, String message){
        return fail(device, message, null);
    }

    public ComSchedulesBulkInfo fail(DeviceInfo device, String message, String messageGroup){
        currentAction.failCount++;
        currentAction.fails.add(new ActionResultInfo(device, message, messageGroup));
        return this;
    }

    public ComSchedulesBulkInfo generalFail(DeviceInfo device, String message){
        return this.generalFail(device, message, null);
    }

    public ComSchedulesBulkInfo generalFail(DeviceInfo device, String message, Object messageGroup){
        if (generalFails == null){
            generalFails = new ArrayList<>();
        }
        generalFails.add(new ActionResultInfo(device, message, messageGroup));
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
        public DeviceInfo device;
        public String message;
        public Object messageGroup;

        //Need for test
        private ActionResultInfo() {}

        private ActionResultInfo(DeviceInfo device, String message) {
            this(device, message, null);
        }

        public ActionResultInfo(DeviceInfo device, String message, Object messageGroup) {
            this.device = device;
            this.message = message;
            this.messageGroup = messageGroup;
        }
    }
}
