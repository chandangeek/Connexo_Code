/*
 * TimerException.java
 *
 * Created on 18 juli 2006, 15:12
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.osiframework;

/**
 *
 * @author Koen
 */
public class TimerException extends OSIFrameworkException {
    
    private int timerId;
    
    /** Creates a new instance of TimerException */
    public TimerException(int timerId) {
        super(TIMEOUT);
        this.setTimerId(timerId);
    }

    public int getTimerId() {
        return timerId;
    }

    private void setTimerId(int timerId) {
        this.timerId = timerId;
    }
    
}
