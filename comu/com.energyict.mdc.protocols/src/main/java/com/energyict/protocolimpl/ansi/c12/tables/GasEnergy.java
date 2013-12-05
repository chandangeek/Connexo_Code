/*
 * GasEnergy.java
 *
 * Created on 27 oktober 2005, 10:21
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class GasEnergy {

    static private final int SIZE=2; // 2 x NiFormat
    private Number gasEnergyZero;
    private Number gasEnergyFull;

    /** Creates a new instance of GasEnergy */
    public GasEnergy(byte[] data,int offset,int niFormat, int dataOrder) throws IOException {
        setGasEnergyZero(C12ParseUtils.getNumberFromNonInteger(data, offset, niFormat, dataOrder));
        offset += C12ParseUtils.getNonIntegerSize(niFormat);
        setGasEnergyFull(C12ParseUtils.getNumberFromNonInteger(data, offset, niFormat, dataOrder));
    }

    static public int getSize(int niFormat) throws IOException {
        return SIZE*C12ParseUtils.getNonIntegerSize(niFormat);
    }

    public Number getGasEnergyZero() {
        return gasEnergyZero;
    }

    public void setGasEnergyZero(Number gasEnergyZero) {
        this.gasEnergyZero = gasEnergyZero;
    }

    public Number getGasEnergyFull() {
        return gasEnergyFull;
    }

    public void setGasEnergyFull(Number gasEnergyFull) {
        this.gasEnergyFull = gasEnergyFull;
    }

}
