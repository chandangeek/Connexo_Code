/*
 * OSIFrameworkTimer.java
 *
 * Created on 18 juli 2006, 15:52
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
public class OSIFrameworkTimer {
    
    private long timeout;
    private int id;
    private String name;
    private boolean running;
    private long expires;
    
    /** Creates a new instance of OSIFrameworkTimer */
    public OSIFrameworkTimer(int id,boolean running) {
        this.id=id;
        this.running=running;
        timeout = 0;
        name = "timer id "+id;
        this.expires=0;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRunning() {
        return running;
    }

    private void setRunning(boolean running) {
        this.running = running;
    }

    public void startTimer() {
        this.expires = System.currentTimeMillis() + getTimeout(); 
        setRunning(true);
    }
    
    public void stopTimer() {
        this.expires = 0;
        setRunning(false);
    }    
    
    public boolean isTimerExpired() {
        if (isRunning())
            return (((long) (System.currentTimeMillis() - expires)) > 0);
        else
            return false;
    }
    
}
