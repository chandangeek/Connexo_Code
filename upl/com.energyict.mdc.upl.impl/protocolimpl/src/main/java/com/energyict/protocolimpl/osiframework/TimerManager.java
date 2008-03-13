/*
 * TimerManager.java
 *
 * Created on 18 juli 2006, 15:37
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.osiframework;

import java.io.*;

/**
 *
 * @author Koen
 */
public class TimerManager {
    
    public final int MAX_NR_OF_TIMERS=10;
    
    private LayerManager layerManager;
    OSIFrameworkTimer[] timers = new OSIFrameworkTimer[MAX_NR_OF_TIMERS];
    
    /** Creates a new instance of TimerManager */
    public TimerManager(LayerManager layerManager) {
        this.setLayerManager(layerManager);
        for (int id=0;id<timers.length;id++) {
            timers[id] = new OSIFrameworkTimer(id,false);
        }
    }
    
    public void timerSetup(int timerId, long timeout, String name) {
        timers[timerId].setTimeout(timeout);
        timers[timerId].setName(name);
    }
    
    public void startTimer(int timerId) throws IOException {
        
        if (timerId >= MAX_NR_OF_TIMERS)
            throw new IOException("TimerManager, startTimer, timerId "+timerId+" exceeds max nr of timers");
        if (getLayerManager().getDebug()>=1) System.out.println("TimerManager, startTimer()"); 
        timers[timerId].startTimer();
    }
    
    public void stopTimer(int timerId) throws IOException {
        if (timerId >= MAX_NR_OF_TIMERS)
            throw new IOException("TimerManager, stopTimer, timerId "+timerId+" exceeds max nr of timers");
        if (getLayerManager().getDebug()>=1) System.out.println("TimerManager, stopTimer()"); 
        timers[timerId].stopTimer();
    }
    
    public void checkTimers() throws TimerException {
        for (int id=0;id<timers.length;id++) {
            if (timers[id].isTimerExpired()) {
                if (getLayerManager().getDebug()>=1) System.out.println("TimerManager, checkTimers(), timer "+id+" expired..."); 
                timers[id].stopTimer();
                throw new TimerException(id);
            }
        }
    }

    public LayerManager getLayerManager() {
        return layerManager;
    }

    private void setLayerManager(LayerManager layerManager) {
        this.layerManager = layerManager;
    }
    
}
