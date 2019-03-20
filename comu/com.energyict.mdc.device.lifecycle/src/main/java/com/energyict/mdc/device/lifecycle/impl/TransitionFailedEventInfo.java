/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl;

import com.elster.jupiter.fsm.StateTransition;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;

import java.time.Instant;

public class TransitionFailedEventInfo {

    private long device;
    private long lifecycle;
    private long transition;
    private long from;
    private long to;
    private String cause;
    private long modTime;
    private static final String COLON_DELIMITER = ":";
    private static final String NA = "N/A";

    public long getDevice() {
        return device;
    }

    public void setDevice(long device) {
        this.device = device;
    }

    public long getLifecycle() {
        return lifecycle;
    }

    public void setLifecycle(long lifecycle) {
        this.lifecycle = lifecycle;
    }

    public long getTransition() {
        return transition;
    }

    public void setTransition(long transition) {
        this.transition = transition;
    }

    public long getFrom() {
        return from;
    }

    public void setFrom(long from) {
        this.from = from;
    }

    public long getTo() {
        return to;
    }

    public void setTo(long to) {
        this.to = to;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public long getModTime() {
        return modTime;
    }

    public void setModTime(long modTime) {
        this.modTime = modTime;
    }

    public static TransitionFailedEventInfo forFailure(AuthorizedAction action, Device device, String cause, Instant modTime) {
        TransitionFailedEventInfo eventInfo = new TransitionFailedEventInfo();
        if (action == null || device == null) {
            throw new IllegalArgumentException();
        }
        eventInfo.setDevice(device.getId());
        eventInfo.setLifecycle(device.getDeviceType().getDeviceLifeCycle().getId());
        if (action instanceof AuthorizedTransitionAction) {
            StateTransition stateTransition = ((AuthorizedTransitionAction) action).getStateTransition();
            eventInfo.setTransition(stateTransition.getId());
            eventInfo.setFrom(stateTransition.getFrom().getId());
            eventInfo.setTo(stateTransition.getTo().getId());
        } else {
            eventInfo.setTransition(-1);
            eventInfo.setFrom(-1);
            eventInfo.setTo(-1);
        }
        eventInfo.setCause(cause);
        eventInfo.setModTime(modTime.toEpochMilli());

        return eventInfo;
    }

    private static String formatStateChange(long from, long to){
        return from + COLON_DELIMITER + to;
    }



}
