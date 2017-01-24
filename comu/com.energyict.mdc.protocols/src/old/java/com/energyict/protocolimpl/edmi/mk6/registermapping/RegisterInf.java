/*
 * RegisterInfo.java
 *
 * Created on 23 maart 2006, 17:15
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk6.registermapping;


/**
 *
 * @author koen
 */
public class RegisterInf {
    
    private int edmiEnergyRegisterId;
    private int obisCField;
    private String description;
    
    /** Creates a new instance of RegisterInfo */
    public RegisterInf(int edmiEnergyRegisterId, int obisCField) {
        this(edmiEnergyRegisterId, obisCField,null);
    }
    public RegisterInf(int edmiEnergyRegisterId, int obisCField, String description) {
        this.setEdmiEnergyRegisterId(edmiEnergyRegisterId);
        this.setObisCField(obisCField);
        this.setDescription(description);
    }

    public String toString() {
        return "RegisterInf: obisCField="+obisCField+", edmiEnergyRegisterId=0x"+Integer.toHexString(edmiEnergyRegisterId)+", description="+description; 
    }
    
    public int getEdmiEnergyRegisterId() {
        return edmiEnergyRegisterId;
    }

    private void setEdmiEnergyRegisterId(int edmiEnergyRegisterId) {
        this.edmiEnergyRegisterId = edmiEnergyRegisterId;
    }

    public int getObisCField() {
        return obisCField;
    }

    private void setObisCField(int obisCField) {
        this.obisCField = obisCField;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
}
