/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * VariableName.java
 *
 * Created on 22 februari 2007, 11:27
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarandlms.common;

import com.energyict.mdc.common.Unit;

import java.io.Serializable;

/**
 * FIXME: Run the tests again with the serialVersionUID so you can rebuild the registers correctly with the fixed ID
 * @author Koen
 */
public class VariableName implements Serializable{


	private static final long serialVersionUID = 6576743255886418829L;

	public static final int ENERGIE = 0;
    public static final int TEMPS_FONCTIONNEMENT = 1;
    public static final int DEPASSEMENT_QUADRATIUQUE = 2;
    public static final int DUREE_DEPASSEMENT = 3;
    public static final int PMAX = 4;
    public static final int ABSTRACT = 5;
    public static final int ARRETE_JOURNALIER = 6;
    public static final int ARRETES_PROGRAMMABLES = 7;

    private int code;
    private String description;
    private Unit unit;
    private int obisAField;
    private int obisCField;
    private int obisDField;
    private int obisFField;
    private int type;

    /** Creates a new instance of VariableName */
    public VariableName(String description, int code, int type) {
        this(description, code, null,-1,-1,-1, -1, type);
    }

    public VariableName(String description, int code, Unit unit, int obisCField, int obisDField, int obisFField, int type) {
        this(description,code,unit,1,obisCField,obisDField,obisFField, type);
    }

    public VariableName(String description, int code, Unit unit, int obisAField, int obisCField, int obisDField, int obisFField, int type) {
        this.setCode(code);
        this.setDescription(description);
        this.setUnit(unit);
        setObisAField(obisAField);
        setObisCField(obisCField);
        setObisDField(obisDField);
        setObisFField(obisFField);
        setType(type);
    }

	public String toString() {
        return code+", "+description;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public int getObisAField() {
        return obisAField;
    }

    public void setObisAField(int obisAField) {
        this.obisAField = obisAField;
    }

    public int getObisCField() {
        return obisCField;
    }

    public void setObisCField(int obisCField) {
        this.obisCField = obisCField;
    }

    public int getObisDField() {
        return obisDField;
    }

    public void setObisDField(int obisDField) {
        this.obisDField = obisDField;
    }

    public int getObisFField() {
        return obisFField;
    }

    public void setObisFField(int obisFField) {
        this.obisFField = obisFField;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isENERGIE() {
        return type==ENERGIE;
    }

    public boolean isTEMPS_FONCTIONNEMENT() {
        return type==TEMPS_FONCTIONNEMENT;
    }

    public boolean isDEPASSEMENT_QUADRATIUQUE() {
        return type==DEPASSEMENT_QUADRATIUQUE;
    }

    public boolean isDUREE_DEPASSEMENT() {
        return type==DUREE_DEPASSEMENT;
    }

    public boolean isPMAX() {
        return type==PMAX;
    }

    public boolean isABSTRACT() {
        return type==ABSTRACT;
    }

    public boolean isARRETE_JOURNALIER(){
    	return type == ARRETE_JOURNALIER;
    }

    public boolean isARRETES_PROGRAMMABLES(){
    	return type == ARRETES_PROGRAMMABLES;
    }

}
