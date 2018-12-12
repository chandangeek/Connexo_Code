/*
 * S200EventMapping.java
 *
 * Created on 1 augustus 2006, 11:07
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.sentry.s200.core;

/**
 *
 * @author Koen
 */
public class S200EventMapping {
    
    private int s200EventCode;
    private int eiMeterEventCode;
    private String description;
    
    /** Creates a new instance of S200EventMapping */
    public S200EventMapping(int s200EventCode, int eiMeterEventCode) {
        this(s200EventCode, eiMeterEventCode, "");
    }
    public S200EventMapping(int s200EventCode, int eiMeterEventCode, String description) {
        this.setS200EventCode(s200EventCode);
        this.setEiMeterEventCode(eiMeterEventCode);
        this.setDescription(description);
    }

    public int getS200EventCode() {
        return s200EventCode;
    }

    public void setS200EventCode(int s200EventCode) {
        this.s200EventCode = s200EventCode;
    }

    public int getEiMeterEventCode() {
        return eiMeterEventCode;
    }

    public void setEiMeterEventCode(int eiMeterEventCode) {
        this.eiMeterEventCode = eiMeterEventCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
}
