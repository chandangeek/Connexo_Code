/*
 * UnitOfMeasure.java
 *
 * Created on 10 juli 2006, 9:43
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4.protocol.ansi.tables;

import com.energyict.cbo.Unit;

/**
 *
 * @author Koen
 */
public class UnitOfMeasure {

    private int id;
    private Unit unit;
    private int quadrantInfo;
    private String description;
    private int obisCField;
    private int quadrantInfoLP;
    private String descriptionLP;
    private int obisCFieldLP;
    private int multiplierMethod;


    private final int NO_MULTIPLIER=0;
    private final int POWER_MULTIPLIER=1;
    private final int VOLT_MULTIPLIER=2;
    private final int CURRENT_MULTIPLIER=3;


    /** Creates a new instance of UnitOfMeasure */
    public UnitOfMeasure(int id, Unit unit, int quadrantInfo, String description, int obisCField, int multiplierMethod) {
        this(id, unit, quadrantInfo, description, obisCField, multiplierMethod, quadrantInfo, description, obisCField);

    }
    public UnitOfMeasure(int id, Unit unit, int quadrantInfo, String description, int obisCField, int multiplierMethod, int quadrantInfoLP, String descriptionLP, int obisCFieldLP) {
        this.setId(id);
        this.setUnit(unit);
        this.setQuadrantInfo(quadrantInfo);
        this.setDescription(description);
        this.setObisCField(obisCField);
        this.setQuadrantInfoLP(quadrantInfoLP);
        this.setDescriptionLP(descriptionLP);
        this.setObisCFieldLP(obisCFieldLP);
        this.setMultiplierMethod(multiplierMethod);

    }

    public boolean isNoMultiplier() {
        return getMultiplierMethod()==NO_MULTIPLIER;
    }
    public boolean isVOLTMultiplier() {
        return getMultiplierMethod()==VOLT_MULTIPLIER;
    }
    public boolean isCURRENTMultiplier() {
        return getMultiplierMethod()==CURRENT_MULTIPLIER;
    }
    public boolean isPOWERMultiplier() {
        return getMultiplierMethod()==POWER_MULTIPLIER;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public int getQuadrantInfo() {
        return quadrantInfo;
    }

    public void setQuadrantInfo(int quadrantInfo) {
        this.quadrantInfo = quadrantInfo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getObisCField() {
        return obisCField;
    }

    public void setObisCField(int obisCField) {
        this.obisCField = obisCField;
    }

    public int getQuadrantInfoLP() {
        return quadrantInfoLP;
    }

    public void setQuadrantInfoLP(int quadrantInfoLP) {
        this.quadrantInfoLP = quadrantInfoLP;
    }

    public String getDescriptionLP() {
        return descriptionLP;
    }

    public void setDescriptionLP(String descriptionLP) {
        this.descriptionLP = descriptionLP;
    }

    public int getObisCFieldLP() {
        return obisCFieldLP;
    }

    public void setObisCFieldLP(int obisCFieldLP) {
        this.obisCFieldLP = obisCFieldLP;
    }

    public int getMultiplierMethod() {
        return multiplierMethod;
    }

    public void setMultiplierMethod(int multiplierMethod) {
        this.multiplierMethod = multiplierMethod;
    }



}
