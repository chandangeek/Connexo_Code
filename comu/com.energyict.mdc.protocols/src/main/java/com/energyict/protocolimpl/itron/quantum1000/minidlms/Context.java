/*
 * Context.java
 *
 * Created on 8 december 2006, 16:04
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

/**
 *
 * @author Koen
 */
public class Context {
    
    private int id;
    private String description;
            
    /** Creates a new instance of Context */
    public Context(int id, String description) {
        this.setId(id);
        this.setDescription(description);
    }

    public String toString() {
        return id+", "+description;
    }  
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
