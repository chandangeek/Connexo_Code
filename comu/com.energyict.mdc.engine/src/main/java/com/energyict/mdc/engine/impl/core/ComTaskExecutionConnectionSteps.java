package com.energyict.mdc.engine.impl.core;

/**
 * <li>A logOn is only required when the ComTaskExecution is the FIRST_OF_CONNECTION_SERIES ComTaskExecution</li>
 * <li>A logOff is only required when the ComTaskExecution is the LAST_OF_CONNECTION_SERIES ComTaskExecution</li>
 * <li>A daisyChainedLogOn is only required for the FIRST_OF_SAME_CONNECTION_BUT_NOT_FIRST_DEVICE ComTaskExecution of another Device in a series of ComTaskExecutions</li>
 * <li>A daisyChainedLogOff is only required for the LAST_OF_SAME_CONNECTION_BUT_NOT_LAST_DEVICE ComTaskExecution of a Device, when other ComTaskExecutions of another
 * Device will follow</li>
 * </p>
 * </p>
 * Copyrights EnergyICT
 * Date: 4/04/13
 * Time: 15:35
 */
public class ComTaskExecutionConnectionSteps {

    public static final int SIGNON = 0b0000_0000_0000_0001;
    public static final int SIGNOFF = 0b0000_0000_0000_1000;
    private static final int DAISYCHAIN_LOGON = 0b0000_0000_0000_0010;
    private static final int DAISYCHAIN_LOGOFF = 0b0000_0000_0000_0100;

    private int flags;

    public ComTaskExecutionConnectionSteps(int... flags) {
        this.flags = or(flags);
    }

    public boolean isLogOnRequired() {
        return isSet(SIGNON);
    }

    public boolean isDaisyChainedLogOnRequired() {
        return isSet(DAISYCHAIN_LOGON) && !isSet(SIGNON);
    }

    public boolean isDaisyChainedLogOffRequired() {
        return isSet(DAISYCHAIN_LOGOFF) && !isSet(SIGNOFF);
    }

    public boolean isLogOffRequired() {
        return isSet(SIGNOFF);
    }

    private void addFlag(int flag) {
        this.flags |= flag;
    }

    ComTaskExecutionConnectionSteps signOn() {
        addFlag(SIGNON);
        return this;
    }

    ComTaskExecutionConnectionSteps signOff() {
        addFlag(SIGNOFF);
        return this;
    }

    ComTaskExecutionConnectionSteps logOn() {
        addFlag(DAISYCHAIN_LOGON);
        return this;
    }

    ComTaskExecutionConnectionSteps logOff() {
        addFlag(DAISYCHAIN_LOGOFF);
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
