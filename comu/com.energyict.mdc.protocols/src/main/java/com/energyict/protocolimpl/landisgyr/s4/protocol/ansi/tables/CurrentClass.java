/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * CurrentClass.java
 *
 * Created on 11 juli 2006, 10:37
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4.protocol.ansi.tables;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Koen
 */
public class CurrentClass {

    private int id;
    private String classDescription;
    private BigDecimal multiplier;

    static List currentClasses = new ArrayList();
    static {
        currentClasses.add(new CurrentClass(1,"20 (S4)",new BigDecimal("0.00133")));
        currentClasses.add(new CurrentClass(2,"200/320 (S4)",new BigDecimal("0.02344")));
        currentClasses.add(new CurrentClass(3,"140 (Altimus Ax)",new BigDecimal("1")));
        currentClasses.add(new CurrentClass(4,"480 (S4)",new BigDecimal("0.02344")));
        currentClasses.add(new CurrentClass(5,"100 (Altimus Ax)",new BigDecimal("1")));
        currentClasses.add(new CurrentClass(6,"200 (Altimus Ax)",new BigDecimal("1")));
        currentClasses.add(new CurrentClass(7,"320 (Altimus 320)",new BigDecimal("1")));
        currentClasses.add(new CurrentClass(8,"480 (Altimus Ax)",new BigDecimal("1")));
        currentClasses.add(new CurrentClass(9,"Reserved 9",new BigDecimal("1")));
        currentClasses.add(new CurrentClass(11,"Reserved 11",new BigDecimal("1")));

    }

    static public CurrentClass findCurrentClass(int id) throws IOException {
        for (int i=0;i<currentClasses.size();i++) {
            CurrentClass cc = (CurrentClass)currentClasses.get(i);
            if (cc.getId() == id)
                return cc;
        }
        throw new IOException("CurrentClass, invalid currentclass id "+id);
    }

    /** Creates a new instance of CurrentClass */
    private CurrentClass(int id, String classDescription, BigDecimal multiplier) {
        this.setId(id);
        this.setClassDescription(classDescription);
        this.setMultiplier(multiplier);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getClassDescription() {
        return classDescription;
    }

    public void setClassDescription(String classDescription) {
        this.classDescription = classDescription;
    }

    public BigDecimal getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(BigDecimal multiplier) {
        this.multiplier = multiplier;
    }

}
