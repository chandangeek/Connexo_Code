/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Multiplier.java
 *
 * Created on 26 september 2007, 11:41
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.eictmodbusrtu.eictveris;

import com.energyict.mdc.common.Unit;

import java.math.BigDecimal;

/**
 *
 * @author kvds
 */
public class Multiplier {

    private int address;
    private Unit unit;
    private BigDecimal mul100A;
    private BigDecimal mul300_400A;
    private BigDecimal mul800A;
    private BigDecimal mul1600A;
    private BigDecimal mul2400A;

    /** Creates a new instance of Multiplier */
    public Multiplier(int address, Unit unit, BigDecimal mul100A, BigDecimal mul300_400A, BigDecimal mul800A, BigDecimal mul1600A, BigDecimal mul2400A) {
        this.setAddress(address);
        this.setUnit(unit);
        this.setMul100A(mul100A);
        this.setMul300_400A(mul300_400A);
        this.setMul800A(mul800A);
        this.setMul1600A(mul1600A);
        this.setMul2400A(mul2400A);
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public BigDecimal getMul100A() {
        return mul100A;
    }

    public void setMul100A(BigDecimal mul100A) {
        this.mul100A = mul100A;
    }

    public BigDecimal getMul300_400A() {
        return mul300_400A;
    }

    public void setMul300_400A(BigDecimal mul300_400A) {
        this.mul300_400A = mul300_400A;
    }

    public BigDecimal getMul800A() {
        return mul800A;
    }

    public void setMul800A(BigDecimal mul800A) {
        this.mul800A = mul800A;
    }

    public BigDecimal getMul1600A() {
        return mul1600A;
    }

    public void setMul1600A(BigDecimal mul1600A) {
        this.mul1600A = mul1600A;
    }

    public BigDecimal getMul2400A() {
        return mul2400A;
    }

    public void setMul2400A(BigDecimal mul2400A) {
        this.mul2400A = mul2400A;
    }

}
