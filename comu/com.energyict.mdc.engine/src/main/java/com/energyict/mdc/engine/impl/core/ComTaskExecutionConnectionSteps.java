/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

public class ComTaskExecutionConnectionSteps {

    public static final int FIRST_OF_CONNECTION_SERIES = 0b0000_0000_0000_0001;
    public static final int FIRST_OF_SAME_CONNECTION_BUT_NOT_FIRST_DEVICE = 0b0000_0000_0000_0010;
    public static final int LAST_OF_SAME_CONNECTION_BUT_NOT_LAST_DEVICE = 0b0000_0000_0000_0100;
    public static final int LAST_OF_CONNECTION_SERIES = 0b0000_0000_0000_1000;

    private int flags;

    public ComTaskExecutionConnectionSteps(int... flags) {
        this.flags = or(flags);
    }

    public boolean isLogOnRequired() {
        return isSet(FIRST_OF_CONNECTION_SERIES);
    }

    public boolean isDaisyChainedLogOnRequired() {
        return isSet(FIRST_OF_SAME_CONNECTION_BUT_NOT_FIRST_DEVICE) && !isSet(FIRST_OF_CONNECTION_SERIES);
    }

    public boolean isDaisyChainedLogOffRequired() {
        return isSet(LAST_OF_SAME_CONNECTION_BUT_NOT_LAST_DEVICE) && !isSet(LAST_OF_CONNECTION_SERIES);
    }

    public boolean isLogOffRequired() {
        return isSet(LAST_OF_CONNECTION_SERIES);
    }

    private void addFlag(int flag) {
        this.flags |= flag;
    }

    ComTaskExecutionConnectionSteps signOn() {
        addFlag(FIRST_OF_CONNECTION_SERIES);
        return this;
    }

    ComTaskExecutionConnectionSteps signOff() {
        addFlag(LAST_OF_CONNECTION_SERIES);
        return this;
    }

    ComTaskExecutionConnectionSteps logOn() {
        addFlag(FIRST_OF_SAME_CONNECTION_BUT_NOT_FIRST_DEVICE);
        return this;
    }

    ComTaskExecutionConnectionSteps logOff() {
        addFlag(LAST_OF_SAME_CONNECTION_BUT_NOT_LAST_DEVICE);
        return this;
    }

    private boolean isSet(int flag) {
        return (flags & flag) != 0;
    }

    private int or(int[] flags) {
        int or = 0;
        for (int flag : flags) {
            or |= flag;
        }
        return or;
    }
}
