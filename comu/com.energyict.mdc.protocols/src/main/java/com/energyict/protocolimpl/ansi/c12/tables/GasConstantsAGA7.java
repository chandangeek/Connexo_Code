/*
 * GasConstantsAGA7.java
 *
 * Created on 27 oktober 2005, 10:43
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class GasConstantsAGA7 extends AbstractConstants {

    private GasAGA7Corr gasAGA7Corr;
    private GasEnergy gasEnergy;

    /** Creates a new instance of GasConstantsAGA7 */
    public GasConstantsAGA7(byte[] data,int offset, int niFormat,int dataOrder) throws IOException {
        setGasAGA7Corr(new GasAGA7Corr(data, offset,niFormat, dataOrder));
        offset+=GasAGA7Corr.getSize(niFormat);
        setGasEnergy(new GasEnergy(data,offset, niFormat,dataOrder));
    }

    static public int getSize(int niFormat) throws IOException {
        return GasAGA7Corr.getSize(niFormat)+GasEnergy.getSize(niFormat);
    }

    protected int getConstantsType() {
        return CONSTANTS_GAS_AGA7;
    }

    public GasAGA7Corr getGasAGA7Corr() {
        return gasAGA7Corr;
    }

    public void setGasAGA7Corr(GasAGA7Corr gasAGA7Corr) {
        this.gasAGA7Corr = gasAGA7Corr;
    }

    public GasEnergy getGasEnergy() {
        return gasEnergy;
    }

    public void setGasEnergy(GasEnergy gasEnergy) {
        this.gasEnergy = gasEnergy;
    }

}
