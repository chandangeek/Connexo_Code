/*
 * EventLogCodeMapping.java
 *
 * Created on 12 augustus 2005, 11:44
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.transdata.markv.core.commands;

/**
 *
 * @author koen
 */
public class EventLogCodeMapping {
    private String description;
    private int eiCode;
    
    /** Creates a new instance of EventLogCodeMapping */
    public EventLogCodeMapping(String description, int eiCode) {
       this.setDescription(description);
       this.setEiCode(eiCode);
    }

    public String toString() {
        return "EventLogCodeMapping: description="+description+", eiCode="+eiCode;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getEiCode() {
        return eiCode;
    }

    public void setEiCode(int eiCode) {
        this.eiCode = eiCode;
    }
    
}
