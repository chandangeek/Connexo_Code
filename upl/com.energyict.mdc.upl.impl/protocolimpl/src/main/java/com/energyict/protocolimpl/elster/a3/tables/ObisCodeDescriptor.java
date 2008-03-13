/*
 * ObisCodeDescriptor.java
 *
 * Created on 1 december 2005, 9:51
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.a3.tables;

import com.energyict.obis.ObisCode;

/**
 *
 * @author koen
 */
public class ObisCodeDescriptor {
    
    private String description;

    private int cField;

    private int bField;

    /**
     * Creates a new instance of ObisCodeDescriptor 
     */
    public ObisCodeDescriptor(int bField, int cField, String description) {
    this.setBField(bField);
    this.setCField(cField);
        this.setDescription(description);
    }

    public String getDescription() {
        return description;
    }

    private void setDescription(String description) {
        this.description = description;
    }

    public int getCField() {
        return cField;
    }

    public void setCField(int cField) {
        this.cField = cField;
    }

    public int getBField() {
        return bField;
    }

    public void setBField(int bField) {
        this.bField = bField;
    }

    
}
