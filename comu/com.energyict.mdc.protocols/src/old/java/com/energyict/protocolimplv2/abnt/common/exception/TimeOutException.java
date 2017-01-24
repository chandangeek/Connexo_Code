package com.energyict.protocolimplv2.abnt.common.exception;

/**
 * @author sva
 * @since 23/05/2014 - 11:20
 */
public class TimeOutException extends ConnectionException {

    private final TimeOutInfo timeOutInfo;

    public TimeOutException(String s, Exception e, TimeOutInfo timeOutInfo) {
        super(s, e);
        this.timeOutInfo = timeOutInfo;
    }

    public TimeOutException(String s, TimeOutInfo timeOutInfo) {
        super(s);
        this.timeOutInfo = timeOutInfo;
    }

    public TimeOutException(Exception e, TimeOutInfo timeOutInfo) {
        super(e);
        this.timeOutInfo = timeOutInfo;
    }

    public TimeOutException(TimeOutInfo timeOutInfo) {
        this.timeOutInfo = timeOutInfo;
    }

    public TimeOutInfo getTimeOutInfo() {
        return timeOutInfo;
    }
}