/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * GasConstantsAGA3.java
 *
 * Created on 27 oktober 2005, 10:25
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
public class GasConstantsAGA3 extends AbstractConstants {

    private GasDP gasDPParm;
    private GasDPShutoff gasDPShutoff;
    private GasPress gasPress;
    private GasAGA3Corr gasAGA3Corr;
    private GasEnergy gasEnergy;


    /** Creates a new instance of GasConstantsAGA3 */
    public GasConstantsAGA3(byte[] data,int offset, int niFormat, int dataOrder) throws IOException {
        setGasDPParm(new GasDP(data,offset,niFormat,dataOrder));
        offset+=GasDP.getSize(niFormat);
        setGasDPShutoff(new GasDPShutoff(data,offset,niFormat, dataOrder));
        offset+=GasDPShutoff.getSize(niFormat);
        setGasPress(new GasPress(data,offset,niFormat, dataOrder));
        offset+=GasPress.getSize(niFormat);
        setGasAGA3Corr(new GasAGA3Corr(data,offset,niFormat, dataOrder));
        offset+=GasAGA3Corr.getSize(niFormat);
        setGasEnergy(new GasEnergy(data,offset,niFormat, dataOrder));
    }

    static public int getSize(int niFormat) throws IOException {
        return GasDP.getSize(niFormat)+GasDPShutoff.getSize(niFormat)+GasPress.getSize(niFormat)+GasAGA3Corr.getSize(niFormat)+GasEnergy.getSize(niFormat);
    }

    protected int getConstantsType() {
        return CONSTANTS_GAS_AGA3;
    }

    public GasDP getGasDPParm() {
        return gasDPParm;
    }

    public void setGasDPParm(GasDP gasDPParm) {
        this.gasDPParm = gasDPParm;
    }

    public GasDPShutoff getGasDPShutoff() {
        return gasDPShutoff;
    }

    public void setGasDPShutoff(GasDPShutoff gasDPShutoff) {
        this.gasDPShutoff = gasDPShutoff;
    }

    public GasPress getGasPress() {
        return gasPress;
    }

    public void setGasPress(GasPress gasPress) {
        this.gasPress = gasPress;
    }

    public GasAGA3Corr getGasAGA3Corr() {
        return gasAGA3Corr;
    }

    public void setGasAGA3Corr(GasAGA3Corr gasAGA3Corr) {
        this.gasAGA3Corr = gasAGA3Corr;
    }

    public GasEnergy getGasEnergy() {
        return gasEnergy;
    }

    public void setGasEnergy(GasEnergy gasEnergy) {
        this.gasEnergy = gasEnergy;
    }

}
