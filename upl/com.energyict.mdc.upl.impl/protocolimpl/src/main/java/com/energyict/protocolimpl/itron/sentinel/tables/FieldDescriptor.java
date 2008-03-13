/*
 * FieldDescriptor.java
 *
 * Created on 30 november 2005, 11:46
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.sentinel.tables;

/**
 *
 * @author koen
 */
public class FieldDescriptor {
    
    private int id; // 0..5 = A..F
    private int value;
    private String description;
    
    /** Creates a new instance of FieldDescriptor */
    public FieldDescriptor(int id, int value, String description) {
        this.setId(id);
        this.setValue(value);
        this.setDescription(description);
    }

    public int getId() {
        return id;
    }

    private void setId(int id) {
        this.id = id;
    }

    public int getValue() {
        return value;
    }

    private void setValue(int value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    private void setDescription(String description) {
        this.description = description;
    }
    
}
