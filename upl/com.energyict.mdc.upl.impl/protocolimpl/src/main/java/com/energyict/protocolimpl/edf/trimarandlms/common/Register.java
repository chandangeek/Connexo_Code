/*
 * Register.java
 *
 * Created on 12 juni 2006, 11:33
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarandlms.common;

import java.io.IOException;
import java.io.Serializable;

import com.energyict.obis.ObisCode;

/**
 *
 * @author Koen
 */
public class Register implements Serializable{
    
    private VariableName variableName;
    private int index;
    private String description;
    private ObisCode obisCode;
    
    /** Creates a new instance of Register */
    public Register(VariableName variableName, int index, int obisEField) throws IOException {
        setIndex(index);
        setVariableName(variableName);
        setObisCode(new ObisCode(getVariableName().getObisAField(),1, getVariableName().getObisCField(),getVariableName().getObisDField(),obisEField,getVariableName().getObisFField()));
        setDescription(RegisterNameFactory.findObisCode(obisCode)==null?obisCode.getDescription():RegisterNameFactory.findObisCode(obisCode));
    }

    public Register(VariableName variableName, ObisCode obisCode) {
    	setVariableName(variableName);
    	setObisCode(obisCode);
    	setDescription(RegisterNameFactory.findObisCode(obisCode)==null?obisCode.getDescription():RegisterNameFactory.findObisCode(obisCode));
	}

	public String toString() {
        return getDescription()+", variable name "+getVariableName()+", tariff index "+getIndex();
    }

    public VariableName getVariableName() {
        return variableName;
    }

    public void setVariableName(VariableName variableName) {
        this.variableName = variableName;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

    public void setObisCode(ObisCode obisCode) {
        this.obisCode = obisCode;
    }
}
